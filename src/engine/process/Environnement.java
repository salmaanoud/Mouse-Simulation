package engine.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import config.Configuration;
import engine.map.Case;
import engine.map.Grille;
import engine.mobile.Caractere;
import engine.mobile.CouleurRobe;
import engine.mobile.Interaction;
import engine.mobile.Nourriture;
import engine.mobile.Obstacle;
import engine.mobile.RelationSociale;
import engine.mobile.Sexe;
import engine.mobile.Souris;
import engine.mobile.TypeInteraction;
import engine.mobile.TypeNourriture;
import engine.mobile.TypeObstacle;
import log.LoggerUtility;
import org.apache.log4j.Logger;

/**
 * Moteur principal de la simulation.
 * Gere les deplacements, interactions, reproduction, tresor et score de colonisation.
 *
 * @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma
 */

public class Environnement implements MobileInterface {

    private static final Logger logger = LoggerUtility.getLogger(Environnement.class, "text");

    private Grille map;
    private Random random = new Random();

    private List<Souris>           mice      = new ArrayList<>();
    private List<Obstacle>         obstacles = new ArrayList<>();
    private List<Nourriture>       foods     = new ArrayList<>();
    private List<FlashInteraction> flashes   = new ArrayList<>();

    // Souris bloquées (id) pendant une interaction active
    private Set<Integer> sourisBloquees = new HashSet<>();

    // Paires ayant déjà interagi dans toute la simulation (persistant)
    private Set<String> pairesDejaSeen = new HashSet<>();

    // Compteur de tours sans manger par souris
    private Map<Integer, Integer> toursDepuisRepas = new HashMap<>();
    // Historique des 5 dernières positions par souris (pour éviter les boucles)
    private Map<Integer, java.util.Deque<String>> historiquePositions = new HashMap<>();
    private static final int TOURS_AVANT_FAIM      = 10;
    private static final int PERTE_ENERGIE_FAIM    = 5;
    private static final int PERTE_ENERGIE_OBSTACLE = 10;

    private int time = 0;
    private boolean simTerminee = false;
    private int extinctionCount = 0;
    private int generation = 1;
    private Case positionTresor = null;
    private boolean tresorActif = false;
    private static final int TOUR_APPARITION_TRESOR  = 50;
    private static final int TOUR_ACCES_TRESOR        = 100;
    private static final int TOUR_FIN_TRESOR          = 300;
    private Souris gagnante = null;
    private boolean victoireTresor = false;
    private boolean sadEnd         = false;  // toutes souris mortes sans trésor
    // Détection colonisation optimale
    private String snapshotPositionsPrecedent = "";
    private int toursStables = 0;
    private static final int SEUIL_STABILISATION = 10; // tours sans amélioration du score
    private double scorePrecedent = -1.0;
    private int toursScoreStable = 0;
    private double scoreFinal = 0.0;
    private static final int MAX_EXTINCTIONS = 5;
    private StatistiquesSimulation stats = new StatistiquesSimulation();

    public Environnement(Grille map) {
        this.map = map;
        init();
    }

    private void init() {
        mice.clear(); obstacles.clear(); foods.clear();
        flashes.clear(); toursDepuisRepas.clear(); sourisBloquees.clear();
        stats.reset();
        pairesDejaSeen.clear();
        time = 0;
        simTerminee = false;
        extinctionCount = 0;
        generation = 1;
        historiquePositions.clear();
        snapshotPositionsPrecedent = "";
        toursStables = 0;
        scorePrecedent = -1.0;
        toursScoreStable = 0;
        scoreFinal = 0.0;
        positionTresor = null;
        tresorActif = false;
        gagnante = null;
        victoireTresor = false;

        for (int i = 0; i < Configuration.INITIAL_MOUSE_COUNT; i++) {
            Souris s = new Souris(randomFreeBlock());
            mice.add(s);
            toursDepuisRepas.put(s.getId(), 0);
        }
        for (int i = 0; i < Configuration.INITIAL_OBSTACLE_COUNT; i++) {
            TypeObstacle type = TypeObstacle.values()[random.nextInt(TypeObstacle.values().length)];
            obstacles.add(new Obstacle(randomFreeBlock(), type));
        }
        for (int i = 0; i < Configuration.INITIAL_FOOD_COUNT; i++) {
            foods.add(new Nourriture(randomFreeBlock()));
        }
    }

    @Override
    public void nextRound() {
        if (mice.isEmpty()) { return; } // extinction gérée en fin de tour
        logger.debug("Tour " + time + " — souris: " + mice.size() + " | nourriture: " + foods.size());

        time++;

        // Mettre à jour le tour courant de chaque flash
        for (FlashInteraction f : flashes) {
            f.setTourCourant(time);
        }

        // Nettoyer les flashes expirés et débloquer les souris concernées
        // Utiliser removeIf pour éviter ConcurrentModificationException
        flashes.removeIf(f -> {
            if (!f.estActif()) {
                sourisBloquees.remove(f.getSouris1Id());
                sourisBloquees.remove(f.getSouris2Id());
                return true;
            }
            return false;
        });

        // Sécurité : débloquer toute souris morte ou dont le flash a disparu
        sourisBloquees.removeIf(id -> {
            boolean flashActif = flashes.stream()
                .anyMatch(f -> f.estActif()
                    && (f.getSouris1Id() == id || f.getSouris2Id() == id));
            return !flashActif;
        });

        List<Souris> snapshot = new ArrayList<>(mice);

        for (Souris mouse : snapshot) {
            if (!mouse.estVivante()) continue;

            mouse.vieillir();

            // Faim
            int compteur = toursDepuisRepas.getOrDefault(mouse.getId(), 0) + 1;
            if (compteur >= TOURS_AVANT_FAIM) {
                mouse.perdreEnergie(PERTE_ENERGIE_FAIM);
                mouse.ecrireJournal("Je n'ai pas mange depuis longtemps (-" + PERTE_ENERGIE_FAIM + ").", time);
                compteur = 0;
            }
            toursDepuisRepas.put(mouse.getId(), compteur);

            if (!mouse.estVivante()) {
                mouse.ecrireJournal("Est morte de faim.", time);
                continue;
            }

            // Déplacement — BLOQUÉ si souris en interaction
            if (!sourisBloquees.contains(mouse.getId())) {
                Case next = choisirProchaineCaseAvecAttraction(mouse, snapshot);
                if (next != null && !isOccupiedByMouse(next, mouse)) {
                    if (isObstacle(next)) {
                        mouse.perdreEnergie(PERTE_ENERGIE_OBSTACLE);
                        mouse.memoriserDanger(next);
                        // Si elle fonçait vers une cible de partage (fausse info) → effacer
                        if (mouse.getCaseCiblePartage() != null &&
                            mouse.getCaseCiblePartage().getLine()   == next.getLine() &&
                            mouse.getCaseCiblePartage().getColumn() == next.getColumn()) {
                            mouse.setCaseCiblePartage(null);
                            mouse.setCaseCibleNourriture(null);
                            mouse.ecrireJournal("J'ai heurte un obstacle ! C'etait un PIEGE ! (-" + PERTE_ENERGIE_OBSTACLE + ").", time);
                            // Flash PIEGE visible sur la grille (nuage "Oh ! Un piege !")
                            flashes.add(new FlashInteraction(
                                mouse.getPosition(), next,
                                TypeInteraction.PIEGE,
                                mouse.getId(), -1, time, "Oh ! Un piege !"));
                        } else {
                            mouse.ecrireJournal("J'ai heurte un obstacle ! (-" + PERTE_ENERGIE_OBSTACLE + ").", time);
                        }
                    } else {
                        // Mettre à jour la direction selon le déplacement
                        int dl = next.getLine()   - mouse.getPosition().getLine();
                        int dc = next.getColumn() - mouse.getPosition().getColumn();
                        if      (dc > 0) mouse.setDirection(Souris.Direction.RIGHT);
                        else if (dc < 0) mouse.setDirection(Souris.Direction.LEFT);
                        else if (dl < 0) mouse.setDirection(Souris.Direction.UP);
                        else if (dl > 0) mouse.setDirection(Souris.Direction.DOWN);
                        mouse.setPosition(next);
                        // Enregistrer position dans l'historique (anti-boucle)
                        String posKey = next.getLine() + "," + next.getColumn();
                        historiquePositions.computeIfAbsent(mouse.getId(),
                            k -> new java.util.ArrayDeque<>()).addLast(posKey);
                        java.util.Deque<String> hist = historiquePositions.get(mouse.getId());
                        if (hist.size() > 6) hist.pollFirst();
                    }
                }
            }

            // Manger ou partager selon l'énergie
            Nourriture eaten = foodAt(mouse.getPosition());
            if (eaten != null) {
                mouse.memoriserCase(mouse.getPosition()); // mémoriser dans tous les cas

                if (mouse.getEnergie() >= Configuration.MAX_ENERGY) {
        // Energie pleine : ne mange pas
                    // SINCERE / SOCIABLE partagent l'info aux Receveurs proches
                    boolean peutPartager = mouse.getCaractere() == Caractere.SINCERE
                                       || mouse.getCaractere() == Caractere.SOCIABLE;
                    if (peutPartager && mouse.isDonneur()) {
                        boolean infoPartagee = false;
                        for (Souris autre : mice) {
                            if (autre == mouse || !autre.estVivante() || autre.isDonneur()) continue;
                            int dist = Math.abs(autre.getPosition().getLine()   - mouse.getPosition().getLine())
                                     + Math.abs(autre.getPosition().getColumn() - mouse.getPosition().getColumn());
                            if (dist <= 8) {
                                autre.setCaseCiblePartage(mouse.getPosition());
                                autre.setCaseCibleNourriture(mouse.getPosition());
                                autre.memoriserCase(mouse.getPosition());
                                autre.ecrireJournal("[R] Info reçue de D#" + mouse.getId()
                                    + " (" + mouse.getCaractere() + ") : nourriture ici !", time);
                                stats.enregistrerPartage(false);
                                infoPartagee = true;
                            }
                        }
                        if (infoPartagee)
                            mouse.ecrireJournal("[D] Energie pleine — partage l'emplacement aux Receveurs.", time);
                        else
                            mouse.ecrireJournal("[D] Energie pleine — personne à portée.", time);
                    } else {
                        // EGOISTE / MECHANTE → ne partage pas, reste sur place
                        mouse.ecrireJournal("[" + mouse.getCaractere() + "] Energie pleine — garde l'info pour elle.", time);
                    }
                    // Dans tous les cas : NE PAS manger, NE PAS supprimer la nourriture

                } else {
                    // Energie < 100 : mange normalement
                    mouse.gagnerEnergie(eaten.getValeurEnergie());
                    mouse.ecrireJournal("A mange (+" + eaten.getValeurEnergie() + " energie).", time);
                    foods.remove(eaten);
                    toursDepuisRepas.put(mouse.getId(), 0);
                }
            }
        }

        gererInteractions();
        supprimerSourisMortes();
        if (mice.isEmpty()) {
            if (!victoireTresor) {
                // Toutes les souris mortes sans trésor → SAD END
                // La simulation se termine et repart avec 3 nouvelles souris
                sadEnd = true;
                simTerminee = true;
                logger.fatal("SAD END — toutes les souris sont mortes au tour "
                    + time + " sans trouver le tresor.");
            }
        }

        // Apparition du tresor au tour 50
        if (time == TOUR_APPARITION_TRESOR && positionTresor == null) {
            positionTresor = choisirCaseTresor();
            if (positionTresor != null) {
                tresorActif = true;
                stats.enregistrerEvenement("Tresor apparu au tour " + time);
                logger.info("=== TRESOR apparu sur le bord en ("
                    + positionTresor.getLine() + "," + positionTresor.getColumn() + ") ===");

                // Informer toutes les souris selon leur caractère
                for (Souris s : mice) {
                    if (!s.estVivante()) continue;
                    // Le trésor est caché — aucune souris ne sait où il est exactement
                // Seule une rumeur circule parmi les SINCERE
                // Toutes les souris recoivent la rumeur
                s.ecrireJournal("Je sens quelque chose d'etrange sur la grille... un tresor se cache quelque part !", time);
                }
            }
        }

        // Désactiver le trésor après 300 tours s'il n'a pas été trouvé
        if (tresorActif && !victoireTresor && time > TOUR_FIN_TRESOR) {
            tresorActif = false;
            positionTresor = null;
            logger.warn("Le tresor a disparu au tour " + time + " — fenetre 100-300 ecoulee.");
        }

        // Vérifier si une souris a atteint le trésor
        // Conditions strictes : SINCERE uniquement, énergie = 100, âge >= 10, bonne mémoire
        if (tresorActif && positionTresor != null && !victoireTresor) {
            for (Souris s : mice) {
                if (!s.estVivante()) continue;
                if (s.getPosition().getLine()   != positionTresor.getLine()) continue;
                if (s.getPosition().getColumn() != positionTresor.getColumn()) continue;

                // Conditions d'acces au tresor : energie, age, memoire, relations sociales
                boolean accessible   = time >= TOUR_ACCES_TRESOR;            // min 100 tours
                boolean bonneSante   = s.getEnergie() >= 85;                 // bien nourrie
                boolean experiencee  = s.getAge() >= 15;                     // vieille et sage
                boolean bonneMemoire = s.getMemoire().getCasesNourritureConnues().size() >= 5; // a bien exploré
                boolean aDesAmis     = s.getMemoire().getRelationSociales().size() >= 2;      // sociale

                if (accessible && bonneSante && experiencee && bonneMemoire && aDesAmis) {
                    // ✅ Toutes les conditions → VICTOIRE !
                    gagnante = s;
                    victoireTresor = true;
                    simTerminee = true;
                    s.ecrireJournal("J'AI TROUVE LE TRESOR apres " + time + " tours d'aventure !", time);
                    logger.info("VICTOIRE ! Souris #" + s.getId()
                        + " (" + s.getCaractere() + ") trouve le tresor au tour " + time);
                } else if (!accessible) {
                    // Trésor encore scellé — pas encore 100 tours
                    s.ecrireJournal("Le tresor est encore scelle... il s'ouvrira avec le temps.", time);
                    s.setCaseCiblePartage(null);
                } else if (!bonneSante) {
                    s.ecrireJournal("Je suis trop fatiguee pour ouvrir le tresor (-5). Je dois manger.", time);
                    s.perdreEnergie(5);
                    s.setCaseCiblePartage(null);
                } else if (!experiencee) {
                    s.ecrireJournal("Le tresor requiert plus de sagesse... je dois vieillir encore.", time);
                    s.setCaseCiblePartage(null);
                } else if (!bonneMemoire) {
                    s.ecrireJournal("Je dois mieux explorer la grille avant d'obtenir le tresor.", time);
                    s.setCaseCiblePartage(null);
                } else if (!aDesAmis) {
                    s.ecrireJournal("Le tresor s'ouvre seulement a celles qui ont des alliees.", time);
                    s.setCaseCiblePartage(null);
                }
            }
        }



        // Calcul du score de colonisation et detection de stabilisation
        if (!mice.isEmpty()) {
            double score = calculerScoreColonisation();
            // Vérifier aussi la convergence des positions
            String snapshotActuel = buildSnapshotPositions();
            boolean positionsIdentiques = snapshotActuel.equals(snapshotPositionsPrecedent);
            snapshotPositionsPrecedent = snapshotActuel;

            boolean scoreStable = Math.abs(score - scorePrecedent) < 0.5;
            if (scoreStable && positionsIdentiques) {
                toursScoreStable++;
            } else {
                toursScoreStable = 0;
            }
            scorePrecedent = score;

            if (toursScoreStable >= SEUIL_STABILISATION && !simTerminee
                    && (time > TOUR_FIN_TRESOR || !tresorActif)) {
                simTerminee = true;
                scoreFinal = score;
                logger.info("Colonisation optimale atteinte au tour " + time
                    + " — score=" + String.format("%.1f", score)
                    + " — systeme stable depuis " + SEUIL_STABILISATION + " tours.");
            }
        }

        // Enregistrement des statistiques du tour
        double energieMoy = mice.isEmpty() ? 0.0
            : mice.stream().mapToInt(s -> s.getEnergie()).average().orElse(0.0);
        int nbSincere  = (int) mice.stream().filter(s -> s.getCaractere() == engine.mobile.Caractere.SINCERE).count();
        int nbSociable = (int) mice.stream().filter(s -> s.getCaractere() == engine.mobile.Caractere.SOCIABLE).count();
        int nbEgoiste  = (int) mice.stream().filter(s -> s.getCaractere() == engine.mobile.Caractere.EGOISTE).count();
        int nbMechante = (int) mice.stream().filter(s -> s.getCaractere() == engine.mobile.Caractere.MECHANTE).count();
        stats.enregistrerTour(mice.size(), energieMoy, foods.size(),
            nbSincere, nbSociable, nbEgoiste, nbMechante);
    }

    // INTERACTIONS

    private void gererInteractions() {
        List<Souris> naissances = new ArrayList<>();

        Set<Case> casesOccupees = new HashSet<>();
        for (FlashInteraction f : flashes) {
            if (f.estActif()) casesOccupees.add(f.getPosition());
        }

        // Anti-doublon reproduction ce tour uniquement
        Set<String> reproDejaFaites = new HashSet<>();

        for (int i = 0; i < mice.size(); i++) {
            for (int j = i + 1; j < mice.size(); j++) {
                Souris s1 = mice.get(i);
                Souris s2 = mice.get(j);

                if (!s1.estVivante() || !s2.estVivante()) continue;
                if (!sontProches(s1, s2)) continue;
                if (sourisBloquees.contains(s1.getId()) || sourisBloquees.contains(s2.getId())) continue;
                if (casesOccupees.contains(s1.getPosition())) continue;

                int idMin = Math.min(s1.getId(), s2.getId());
                int idMax = Math.max(s1.getId(), s2.getId());
                String paire = idMin + "-" + idMax;

                if (s1.getSexe() != s2.getSexe()) {
                    // Sexes differents : reproduction possible
                    if (reproDejaFaites.contains(paire)) continue;
                    reproDejaFaites.add(paire);

                    List<Souris> bebes = gererReproduction(s1, s2);
                    naissances.addAll(bebes);
                    if (!bebes.isEmpty()) casesOccupees.add(s1.getPosition());

                } else {
                    // Meme sexe : communication D->R uniquement, une seule fois par paire
                    if (s1.isDonneur() == s2.isDonneur()) continue;

                    // Cette paire s'est déjà rencontrée dans la simulation
                    if (pairesDejaSeen.contains(paire)) continue;
                    pairesDejaSeen.add(paire);

                    gererRelationSociale(s1, s2);
                    casesOccupees.add(s1.getPosition());
                }
            }
        }

        mice.addAll(naissances);
    }

    // Gestion de la relation sociale (partage d'info ou amitie) entre un Donneur et un Receveur

    private void gererRelationSociale(Souris s1, Souris s2) {
        Souris donneur  = s1.isDonneur() ? s1 : s2;
        Souris receveur = s1.isDonneur() ? s2 : s1;

        double p = Interaction.calculerProbabilitePartage(donneur, receveur);
        boolean partage = random.nextDouble() < p;

        TypeInteraction typeInter = partage ? TypeInteraction.PARTAGE_NOURRITURE : TypeInteraction.AMITIE;
        new Interaction(typeInter, s1, s2, time).enregistrer();

        s1.getMemoire().ajouterRelation(new RelationSociale(s2, "Amitie", 10));
        s2.getMemoire().ajouterRelation(new RelationSociale(s1, "Amitie", 10));

        String msg;
        if (partage) {
            // Fiabilité de l'info selon le caractère du donneur
            boolean infoFiable;
            String fiabiliteLog;
            switch (donneur.getCaractere()) {
                case SINCERE:
                    infoFiable = true;
                    fiabiliteLog = "info VRAIE";
                    break;
                case MECHANTE:
                    // La méchante ment toujours
                    infoFiable = false;
                    fiabiliteLog = "info FAUSSE (mensonge)";
                    break;
                case EGOISTE:
                    // L'égoïste ment 70% du temps
                    infoFiable = random.nextDouble() > 0.70;
                    fiabiliteLog = infoFiable ? "info vraie (égoïste)" : "info FAUSSE (égoïste)";
                    break;
                case SOCIABLE:
                default:
                    // La sociable dit toujours la vérité
                    infoFiable = true;
                    fiabiliteLog = "info vraie";
                    break;
            }

            if (infoFiable) {
                // Partage les vraies cases nourriture connues du donneur
                List<Case> casesConnues = new ArrayList<>(donneur.getMemoire().getCasesNourritureConnues());

                // Si le donneur n'a aucune case mémorisée, il cherche la nourriture
                // la plus proche et la partage comme info (info vraie = position réelle)
                if (casesConnues.isEmpty()) {
                    Nourriture plusProche = null;
                    int distMin = Integer.MAX_VALUE;
                    for (Nourriture f : foods) {
                        int d = Math.abs(f.getPosition().getLine()   - donneur.getPosition().getLine())
                              + Math.abs(f.getPosition().getColumn() - donneur.getPosition().getColumn());
                        if (d < distMin) { distMin = d; plusProche = f; }
                    }
                    if (plusProche != null) casesConnues.add(plusProche.getPosition());
                }

                for (Case c : casesConnues) receveur.memoriserCase(c);

                // Le receveur se dirige vers la case indiquée (info vraie)
                if (!casesConnues.isEmpty()) {
                    Case cible = casesConnues.get(0);
                    receveur.setCaseCiblePartage(cible);  // suit aveuglément
                    receveur.setCaseCibleNourriture(cible);
                }

                msg = "D#" + donneur.getId() + " → info → R#" + receveur.getId();
                stats.enregistrerPartage(false);
                logger.info("Partage VRAI D#" + donneur.getId() + "("+donneur.getCaractere()+") → R#" + receveur.getId());
            } else {
                // Le donneur ment : donne l'adresse d'un obstacle comme si c'était de la nourriture
                Case fausseCible = null;

                // Priorité : obstacle le plus proche (pour maximiser les dégâts)
                if (!obstacles.isEmpty()) {
                    int distMin = Integer.MAX_VALUE;
                    for (Obstacle o : obstacles) {
                        int d = Math.abs(o.getPosition().getLine()   - receveur.getPosition().getLine())
                              + Math.abs(o.getPosition().getColumn() - receveur.getPosition().getColumn());
                        if (d < distMin && d > 0) { distMin = d; fausseCible = o.getPosition(); }
                    }
                }
                // Sinon fallback sur cases danger mémorisées
                if (fausseCible == null) {
                    List<Case> casesDanger = donneur.getMemoire().getCasesDangereuses();
                    if (!casesDanger.isEmpty()) fausseCible = casesDanger.get(0);
                }

                if (fausseCible != null) {
                    // Le receveur croit que c'est de la nourriture — il fonce AVEUGLÉMENT
                    receveur.setCaseCiblePartage(fausseCible);
                    receveur.setCaseCibleNourriture(fausseCible);
                }

                msg = "D#" + donneur.getId() + " → info → R#" + receveur.getId() + " (MENSONGE!)";
                stats.enregistrerPartage(true);
                logger.warn("MENSONGE D#" + donneur.getId() + "("+donneur.getCaractere()+") → R#" + receveur.getId());
            }

            donneur.ecrireJournal("[D] Partage info avec souris#" + receveur.getId()
                + " — " + fiabiliteLog, time);
            receveur.ecrireJournal("[R] Reçu info de souris#" + donneur.getId()
                + " (caractère: " + donneur.getCaractere() + ")", time);

        } else {
            // Pas de partage — juste amitié
            if (donneur.getCaractere() == Caractere.SINCERE && receveur.getEnergie() < 40)
                receveur.gagnerEnergie(5);
            msg = "D#" + donneur.getId() + " ↔ R#" + receveur.getId();
            donneur.ecrireJournal("[D] Amitié avec souris#" + receveur.getId() + " — partage refusé", time);
            stats.enregistrerAmitie();
            receveur.ecrireJournal("[R] Amitié avec souris#" + donneur.getId(), time);
        }

        FlashInteraction flash = new FlashInteraction(s1.getPosition(), s2.getPosition(),
            typeInter, s1.getId(), s2.getId(), time, msg);
        flashes.add(flash);
        sourisBloquees.add(s1.getId());
        sourisBloquees.add(s2.getId());
    }

    // Gestion de la reproduction entre deux souris de sexe oppose

    private List<Souris> gererReproduction(Souris s1, Souris s2) {
        List<Souris> bebes = new ArrayList<>();
        if (!peutSeReproduire(s1, s2)) return bebes;

        // Limite de population
        if (mice.size() >= Configuration.MAX_POPULATION) return bebes;

        double p = probabiliteReproduction(s1, s2);
        if (random.nextDouble() >= p) {
            // Pas de reproduction ce tour (probabilité non atteinte)
            s1.ecrireJournal("Rencontre avec souris#" + s2.getId() + " — pas de reproduction.", time);
            s2.ecrireJournal("Rencontre avec souris#" + s1.getId() + " — pas de reproduction.", time);
            return bebes;
        }

        s1.perdreEnergie(Configuration.REPRODUCTION_ENERGY_COST);
        s2.perdreEnergie(Configuration.REPRODUCTION_ENERGY_COST);

        int nbBebes = 1 + random.nextInt(2); // 1 ou 2
        int places = Configuration.MAX_POPULATION - mice.size();
        nbBebes = Math.min(nbBebes, places); // ne pas dépasser la limite

        for (int i = 0; i < nbBebes; i++) {
            // Le souriceau naît dans une case libre adjacente aux parents
            Case libre = caseLibreAdjacente(s1.getPosition(), s2.getPosition());
            if (libre == null) libre = randomFreeBlock(); // fallback si pas de case adjacente libre
            if (libre == null) break;

            Sexe sexeBebe = random.nextBoolean() ? Sexe.MALE : Sexe.FEMELLE;
            double tirage = random.nextDouble();
            Caractere caractereBebe;
            if (tirage < 0.45)      caractereBebe = s1.getCaractere();
            else if (tirage < 0.90) caractereBebe = s2.getCaractere();
            else {
                Caractere[] tous = Caractere.values();
                caractereBebe = tous[random.nextInt(tous.length)];
            }

            // Héritage couleur : 45% parent1, 45% parent2, 10% mutation aléatoire
            double tirCouleur = random.nextDouble();
            CouleurRobe couleurBebe;
            if (tirCouleur < 0.45)      couleurBebe = s1.getCouleur();
            else if (tirCouleur < 0.90) couleurBebe = s2.getCouleur();
            else {
                CouleurRobe[] couleurs = CouleurRobe.values();
                couleurBebe = couleurs[random.nextInt(couleurs.length)];
            }

            Souris bebe = new Souris(libre, sexeBebe, caractereBebe, couleurBebe);
            bebe.ecrireJournal("Je suis ne(e) !", time);
            toursDepuisRepas.put(bebe.getId(), 0);
            bebes.add(bebe);
        }

        if (!bebes.isEmpty()) {
            stats.enregistrerReproduction(bebes.size());
            logger.info("S#" + s1.getId() + " ♥ S#" + s2.getId() + " → " + bebes.size() + " bébé(s)");
            Interaction interaction = new Interaction(TypeInteraction.REPRODUCTION, s1, s2, time);
            interaction.enregistrer();

            s1.getMemoire().ajouterRelation(new RelationSociale(s2, "partenaire", 30));
            s2.getMemoire().ajouterRelation(new RelationSociale(s1, "partenaire", 30));

            // Flash + bloquer les deux souris
            FlashInteraction flash = new FlashInteraction(s1.getPosition(), s2.getPosition(),
                TypeInteraction.REPRODUCTION, s1.getId(), s2.getId(), time,
                "#" + s1.getId() + " ♥ #" + s2.getId());
            flashes.add(flash);
            sourisBloquees.add(s1.getId());
            sourisBloquees.add(s2.getId());

            s1.ecrireJournal("Reproduction avec souris#" + s2.getId()
                + " -> " + bebes.size() + " bebe(s)", time);
            s2.ecrireJournal("Reproduction avec souris#" + s1.getId()
                + " -> " + bebes.size() + " bebe(s)", time);
        }

        return bebes;
    }

    private boolean peutSeReproduire(Souris s1, Souris s2) {
        // D/R ne bloque PAS la reproduction — seul le sexe opposé compte
        return s1.estVivante() && s2.estVivante()
            && s1.getSexe() != s2.getSexe()
            && s1.getAge() >= 3 && s2.getAge() >= 3
            && s1.getEnergie() >= 50 && s2.getEnergie() >= 50;
    }

    private double probabiliteReproduction(Souris s1, Souris s2) {
        double p = 0.20;
        p += ((s1.getEnergie() + s2.getEnergie()) / 2.0) / 200.0;
        if (s1.getCaractere() == Caractere.SINCERE)  p += 0.10;
        if (s2.getCaractere() == Caractere.SINCERE)  p += 0.10;
        if (s1.getCaractere() == Caractere.MECHANTE) p -= 0.10;
        if (s2.getCaractere() == Caractere.MECHANTE) p -= 0.10;
        if (s1.getCaractere() == Caractere.EGOISTE)  p -= 0.05;
        if (s2.getCaractere() == Caractere.EGOISTE)  p -= 0.05;
        return Math.max(0.0, Math.min(0.95, p));
    }

    // Getters specifiques a la simulation

    public List<FlashInteraction> getFlashes()       { return flashes; }
    public boolean isSimTerminee()                   { return simTerminee; }
    public int getGeneration()                        { return generation; }
    public int getExtinctionCount()                   { return extinctionCount; }
    public double getScoreFinal()                      { return scoreFinal; }
    public double getScoreActuel()                     { return calculerScoreColonisation(); }
    public Case getPositionTresor()                    { return positionTresor; }
    public boolean isTresorScelle()                    { return time < TOUR_ACCES_TRESOR; }
    public boolean isSadEnd()                          { return sadEnd; }
    public int getTourFinTresor()                      { return TOUR_FIN_TRESOR; }
    public int getTourFinSimulation()                  { return 500; }
    public int getTourAccesTresor()                    { return TOUR_ACCES_TRESOR; }
    public boolean isTresorActif()                     { return tresorActif; }
    public boolean isVictoireTresor()                  { return victoireTresor; }
    public Souris getGagnante()                        { return gagnante; }
    public StatistiquesSimulation getStats()         { return stats; }

    // Methodes utilitaires privees

    private boolean sontProches(Souris s1, Souris s2) {
        int dl = Math.abs(s1.getPosition().getLine()   - s2.getPosition().getLine());
        int dc = Math.abs(s1.getPosition().getColumn() - s2.getPosition().getColumn());
        return dl + dc <= 1;
    }

    private void supprimerSourisMortes() {
        long avant = mice.size();
        mice.removeIf(s -> !s.estVivante());
        long morts = avant - mice.size();
        for (int i = 0; i < morts; i++) stats.enregistrerMort();
        if (morts > 0) logger.info(morts + " souris décédée(s) au tour " + time);
    }

    // Choisit la prochaine case selon 8 niveaux de priorite decroissante
    private Case choisirProchaineCaseAvecAttraction(Souris mouse, List<Souris> toutesLesSouris) {
        int l = mouse.getPosition().getLine(), c = mouse.getPosition().getColumn();

        List<Case> voisins = new ArrayList<>();
        if (l > 0)                     voisins.add(map.getBlock(l - 1, c));
        if (l < map.getLineCount()-1)  voisins.add(map.getBlock(l + 1, c));
        if (c > 0)                     voisins.add(map.getBlock(l, c - 1));
        if (c < map.getColumnCount()-1)voisins.add(map.getBlock(l, c + 1));
        if (voisins.isEmpty()) return null;

        List<Case> libres = new ArrayList<>();
        for (Case v : voisins) {
            if (!isObstacle(v) && !isOccupiedByMouse(v, mouse)) libres.add(v);
        }

        // Si Donneur à énergie pleine : retire les cases nourriture des candidats
        // (elle ne veut pas manger — elle cherche à partager)
        boolean eviterNourriture = mouse.isDonneur() && mouse.getEnergie() >= Configuration.MAX_ENERGY;
        if (eviterNourriture) {
            List<Case> sansFoods = new ArrayList<>();
            for (Case v : libres) {
                if (foodAt(v) == null) sansFoods.add(v);
            }
            if (!sansFoods.isEmpty()) libres = sansFoods;
        }

        // Anti-boucle : exclure les cases visitées récemment (sauf si pas le choix)
        java.util.Deque<String> recents = historiquePositions.getOrDefault(
            mouse.getId(), new java.util.ArrayDeque<>());
        List<Case> libresNouveaux = new ArrayList<>();
        for (Case v : libres) {
            String k = v.getLine() + "," + v.getColumn();
            if (!recents.contains(k)) libresNouveaux.add(v);
        }
        // Utiliser les cases non-visitées si disponibles, sinon fallback sur libres
        List<Case> candidats = !libresNouveaux.isEmpty() ? libresNouveaux
                             : (!libres.isEmpty() ? libres : voisins);

        // Priorite 0 : tresor — accessible seulement apres 100 tours
        if (tresorActif && positionTresor != null && time >= TOUR_ACCES_TRESOR
                && mouse.getEnergie() >= 85
                && mouse.getAge() >= 15) {
            int tl = positionTresor.getLine(), tc = positionTresor.getColumn();
            if (l != tl || c != tc) {
                Case vers = versCible(candidats, tl, tc);
                if (vers != null) return vers;
            }
        }

        // Priorite 1 : cible recue par partage D->R (suivie aveuglement)
        Case ciblePartage = mouse.getCaseCiblePartage();
        if (ciblePartage != null) {
            if (ciblePartage.getLine() == l && ciblePartage.getColumn() == c) {
                // Arrivée sur la case — oublier la cible
                mouse.setCaseCiblePartage(null);
                mouse.setCaseCibleNourriture(null);
            } else {
                Case m = versCible(candidats, ciblePartage.getLine(), ciblePartage.getColumn());
                if (m != null) return m;
            }
        }

        // Priorite 2 : cible nourriture memorisee dans la propre memoire
        Case cibleNourr = mouse.getCaseCibleNourriture();
        if (cibleNourr != null && mouse.getCaseCiblePartage() == null) {
            if (foodAt(cibleNourr) == null || (cibleNourr.getLine() == l && cibleNourr.getColumn() == c)) {
                mouse.setCaseCibleNourriture(null);
            } else {
                Case m = versCible(candidats, cibleNourr.getLine(), cibleNourr.getColumn());
                if (m != null) return m;
            }
        }

        // Priorite 3 : nourriture memorisee dans la propre memoire
        List<Case> memNourriture = mouse.getMemoire().getCasesNourritureConnues();
        if (!memNourriture.isEmpty()) {
            Case meilleureCase = null;
            int distMin = Integer.MAX_VALUE;
            for (Case cn : memNourriture) {
                if (foodAt(cn) != null) {
                    int d = Math.abs(cn.getLine() - l) + Math.abs(cn.getColumn() - c);
                    if (d < distMin && d > 0) { distMin = d; meilleureCase = cn; }
                }
            }
            if (meilleureCase != null) {
                Case m = versCible(candidats, meilleureCase.getLine(), meilleureCase.getColumn());
                if (m != null) return m;
            }
        }

        // Priorite 3.5 : tresor visible dans un rayon de 3 cases
        if (tresorActif && positionTresor != null
                && time >= TOUR_ACCES_TRESOR
                && mouse.getEnergie() >= 85
                && mouse.getAge() >= 15) {
            int dt = Math.abs(positionTresor.getLine()   - l)
                   + Math.abs(positionTresor.getColumn() - c);
            if (dt <= 2) { // rayon très court — doit vraiment explorer
                mouse.setCaseCiblePartage(positionTresor);
                mouse.ecrireJournal("J'aperçois quelque chose de brillant tout pres !", time);
                Case vers = versCible(candidats, positionTresor.getLine(), positionTresor.getColumn());
                if (vers != null) return vers;
            }
        }

        // Priorite 4 : nourriture visible dans un rayon de 5 cases
        Nourriture nourritureVisible = null;
        int distNourMin = Integer.MAX_VALUE;
        for (Nourriture f : foods) {
            int d = Math.abs(f.getPosition().getLine()   - l)
                  + Math.abs(f.getPosition().getColumn() - c);
            if (d <= 5 && d < distNourMin) { distNourMin = d; nourritureVisible = f; }
        }
        if (nourritureVisible != null) {
            mouse.memoriserCase(nourritureVisible.getPosition());
            Case m = versCible(candidats, nourritureVisible.getPosition().getLine(),
                                          nourritureVisible.getPosition().getColumn());
            if (m != null) return m;
        }

        // Priorite 5 : repulsion si trop de voisins proches
        int voisinsProches = 0;
        for (Souris autre : toutesLesSouris) {
            if (autre == mouse || !autre.estVivante()) continue;
            int dist = Math.abs(autre.getPosition().getLine() - l)
                     + Math.abs(autre.getPosition().getColumn() - c);
            if (dist <= 2) voisinsProches++;
        }
        if (voisinsProches >= 3) {
            Case meilleur = null; int maxDist = -1;
            for (Case v : candidats) {
                int distTotale = 0;
                for (Souris autre : toutesLesSouris) {
                    if (autre == mouse || !autre.estVivante()) continue;
                    distTotale += Math.abs(autre.getPosition().getLine()   - v.getLine())
                               +  Math.abs(autre.getPosition().getColumn() - v.getColumn());
                }
                if (distTotale > maxDist) { maxDist = distTotale; meilleur = v; }
            }
            if (meilleur != null) return meilleur;
        }

        // Priorite 6 : attraction vers sexe oppose (35% de probabilite)
        Souris cibleSexe = null; int distMin2 = Integer.MAX_VALUE;
        for (Souris autre : toutesLesSouris) {
            if (autre == mouse || !autre.estVivante() || autre.getSexe() == mouse.getSexe()) continue;
            int dist = Math.abs(autre.getPosition().getLine()   - l)
                     + Math.abs(autre.getPosition().getColumn() - c);
            if (dist < distMin2 && dist <= 10) { distMin2 = dist; cibleSexe = autre; }
        }
        if (cibleSexe != null && random.nextDouble() < 0.35) {
            Case m = versCible(candidats, cibleSexe.getPosition().getLine(),
                                          cibleSexe.getPosition().getColumn());
            if (m != null) return m;
        }

        // Priorite 7 : eviter les cases dangereuses memorisees
        List<Case> sures = new ArrayList<>();
        List<Case> dangers = mouse.getMemoire().getCasesDangereuses();
        for (Case v : libres) {
            if (!dangers.contains(v)) sures.add(v);
        }
        if (!sures.isEmpty()) return sures.get(random.nextInt(sures.size()));

        // Priorite 8 : deplacement aleatoire (dernier recours)
        if (!libres.isEmpty()) return libres.get(random.nextInt(libres.size()));
        return voisins.get(random.nextInt(voisins.size()));
    }

    // Retourne la case candidate la plus proche de la cible (tl, tc)
    private Case versCible(List<Case> candidats, int tl, int tc) {
        Case meilleur = null; int meilleurDist = Integer.MAX_VALUE;
        for (Case v : candidats) {
            int d = Math.abs(v.getLine() - tl) + Math.abs(v.getColumn() - tc);
            if (d < meilleurDist) { meilleurDist = d; meilleur = v; }
        }
        return meilleur;
    }

    private boolean isObstacle(Case b) {
        for (Obstacle o : obstacles) if (o.getPosition().equals(b)) return true;
        return false;
    }

    private Nourriture foodAt(Case b) {
        for (Nourriture f : foods) if (f.getPosition().equals(b)) return f;
        return null;
    }

    private boolean isMouse(Case b) {
        for (Souris s : mice) if (s.getPosition().equals(b)) return true;
        return false;
    }

    private boolean isOccupiedByMouse(Case b, Souris current) {
        for (Souris s : mice) if (s != current && s.getPosition().equals(b)) return true;
        return false;
    }

    private Case randomFreeBlock() {
        int maxTries = 300;
        for (int i = 0; i < maxTries; i++) {
            int l = random.nextInt(map.getLineCount());
            int c = random.nextInt(map.getColumnCount());
            Case b = map.getBlock(l, c);
            if (!isObstacle(b) && foodAt(b) == null && !isMouse(b)) return b;
        }
        return null;
    }

    // Cherche une case libre adjacente aux deux parents pour y placer le souriceau
    private Case caseLibreAdjacente(Case pos1, Case pos2) {
        List<Case> candidats = new ArrayList<>();
        int[] dLines = {-1, 1, 0, 0};
        int[] dCols  = {0, 0, -1, 1};
        for (Case pos : new Case[]{pos1, pos2}) {
            for (int k = 0; k < 4; k++) {
                int nl = pos.getLine()   + dLines[k];
                int nc = pos.getColumn() + dCols[k];
                if (nl >= 0 && nl < map.getLineCount() && nc >= 0 && nc < map.getColumnCount()) {
                    Case c = map.getBlock(nl, nc);
                    if (!isObstacle(c) && foodAt(c) == null && !isMouse(c))
                        candidats.add(c);
                }
            }
        }
        // Si pas de case libre adjacente → chercher plus loin (rayon 4)
        if (candidats.isEmpty()) {
            for (int dl = -4; dl <= 4; dl++) {
                for (int dc = -4; dc <= 4; dc++) {
                    int nl = pos1.getLine()   + dl;
                    int nc = pos1.getColumn() + dc;
                    if (nl >= 0 && nl < map.getLineCount() && nc >= 0 && nc < map.getColumnCount()) {
                        Case c = map.getBlock(nl, nc);
                        if (!isObstacle(c) && foodAt(c) == null && !isMouse(c))
                            candidats.add(c);
                    }
                }
            }
        }
        if (candidats.isEmpty()) return null;
        return candidats.get(random.nextInt(candidats.size()));
    }

    // Implementation de MobileInterface

    // Calcule le score de colonisation : couverture * 40 + energie * 30 + diversite * 20 + population * 10
    private double calculerScoreColonisation() {
        if (mice.isEmpty()) return 0.0;

        int cols = map.getColumnCount(), rows = map.getLineCount();
        int totalCases = cols * rows;

        // 1. Couverture : cases distinctes occupées sur total accessible
        java.util.Set<String> casesOccupees = new java.util.HashSet<>();
        for (Souris s : mice) {
            if (s.estVivante())
                casesOccupees.add(s.getPosition().getLine() + "," + s.getPosition().getColumn());
        }
        double couverture = (double) casesOccupees.size() / Math.max(1, totalCases - obstacles.size());

        // 2. Energie moyenne normalisée
        double energieMoy = 0;
        int vivantes = 0;
        for (Souris s : mice) {
            if (s.estVivante()) { energieMoy += s.getEnergie(); vivantes++; }
        }
        energieMoy = vivantes > 0 ? energieMoy / vivantes / Configuration.MAX_ENERGY : 0;

        // 3. Diversité des zones : répartition dans les 4 quadrants
        int[] quadrants = {0, 0, 0, 0};
        int midC = cols / 2, midR = rows / 2;
        for (Souris s : mice) {
            if (!s.estVivante()) continue;
            int l = s.getPosition().getLine(), c = s.getPosition().getColumn();
            int q = (l < midR ? 0 : 2) + (c < midC ? 0 : 1);
            quadrants[q]++;
        }
        int presents = 0;
        for (int q : quadrants) if (q > 0) presents++;
        double diversite = presents / 4.0;

        // 4. Population normalisée
        double population = Math.min(1.0, (double) vivantes / Configuration.MAX_POPULATION);

        return couverture * 40.0 + energieMoy * 30.0 + diversite * 20.0 + population * 10.0;
    }

    // Choisit une case sur le bord de la grille pour le tresor, la plus eloignee des souris
    private Case choisirCaseTresor() {
        int rows = map.getLineCount(), cols = map.getColumnCount();

        // 1. Cases des bords (première/dernière ligne et colonne)
        List<Case> bords = new ArrayList<>();
        for (int c = 0; c < cols; c++) {
            bords.add(map.getBlock(0, c));
            bords.add(map.getBlock(rows - 1, c));
        }
        for (int l = 1; l < rows - 1; l++) {
            bords.add(map.getBlock(l, 0));
            bords.add(map.getBlock(l, cols - 1));
        }

        // 2. Garder les cases libres uniquement
        List<Case> candidates = new ArrayList<>();
        for (Case c : bords) {
            if (!isObstacle(c) && !isMouse(c) && foodAt(c) == null) {
                candidates.add(c);
            }
        }
        if (candidates.isEmpty()) return null;

        // 3. Choisir la case la plus éloignée de TOUTES les souris (difficulté max)
        Case meilleur = null;
        int maxDistMin = -1;
        for (Case c : candidates) {
            int distMin = Integer.MAX_VALUE;
            for (Souris s : mice) {
                if (!s.estVivante()) continue;
                int d = Math.abs(s.getPosition().getLine()   - c.getLine())
                      + Math.abs(s.getPosition().getColumn() - c.getColumn());
                if (d < distMin) distMin = d;
            }
            if (distMin > maxDistMin) { maxDistMin = distMin; meilleur = c; }
        }

        return meilleur;
    }

    // Distance Manhattan entre une case de depart et le tresor
    public int bfsDistanceTresor(Case depart) {
        if (positionTresor == null) return Integer.MAX_VALUE;
        int tl = positionTresor.getLine(), tc = positionTresor.getColumn();
        int dl = depart.getLine(),          dc = depart.getColumn();
        // Heuristique Manhattan (rapide, suffisante pour la grille)
        return Math.abs(tl - dl) + Math.abs(tc - dc);
    }

    private String buildSnapshotPositions() {
        java.util.TreeMap<Integer, String> pos = new java.util.TreeMap<>();
        for (Souris s : mice) {
            if (s.estVivante())
                pos.put(s.getId(), s.getPosition().getLine() + "," + s.getPosition().getColumn());
        }
        return pos.toString();
    }

    @Override public List<Souris>     getMice()          { return mice; }
    @Override public List<Obstacle>   getObstacles()     { return obstacles; }
    @Override public List<Nourriture> getFoods()         { return foods; }
    @Override public int              getTime()          { return time; }
    @Override public int              getMouseCount()    { return mice.size(); }
    @Override public int              getFoodCount()     { return foods.size(); }
    @Override public int              getObstacleCount() { return obstacles.size(); }
    @Override public void             reset()            { init(); }

    @Override
    public void addMouse() {
        Case c = randomFreeBlock();
        if (c != null) { Souris s = new Souris(c); mice.add(s); toursDepuisRepas.put(s.getId(), 0); }
    }

    public void addMouse(Sexe sexe) {
        Case c = randomFreeBlock();
        if (c != null) {
            Caractere car = Caractere.values()[random.nextInt(Caractere.values().length)];
            Souris s = new Souris(c, sexe, car);
            mice.add(s);
            toursDepuisRepas.put(s.getId(), 0);
        }
    }

    @Override
    public void addFood() {
        Case c = randomFreeBlock();
        if (c != null) {
            TypeNourriture[] types = TypeNourriture.values();
            TypeNourriture type = types[random.nextInt(types.length)];
            foods.add(new Nourriture(c, type));
        }
    }

    public void addFood(TypeNourriture type) {
        Case c = randomFreeBlock();
        if (c != null) foods.add(new Nourriture(c, type));
    }

    @Override
    public void addObstacle() {
        Case c = randomFreeBlock();
        if (c != null) {
            TypeObstacle type = TypeObstacle.values()[random.nextInt(TypeObstacle.values().length)];
            obstacles.add(new Obstacle(c, type));
        }
    }

    public void addObstacle(TypeObstacle type) {
        Case c = randomFreeBlock();
        if (c != null) obstacles.add(new Obstacle(c, type));
    }
}
