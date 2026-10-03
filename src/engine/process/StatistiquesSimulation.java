package engine.process;

import java.util.ArrayList;
import java.util.List;

/**
 * Collecte les statistiques de la simulation tour par tour. Utilisé par StatsPanel pour les graphiques dynamiques. @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma /
 *
 * @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma
 */
public class StatistiquesSimulation {
    private final List<Integer> histPopulation      = new ArrayList<>();
    private final List<Double>  histEnergieMoyenne  = new ArrayList<>();
    private final List<Integer> histNbNourriture     = new ArrayList<>();
    private int totalPartages      = 0;
    private int totalAmities       = 0;
    private int totalReproductions = 0;
    private int totalNaissances    = 0;
    private int totalMorts         = 0;
    private int totalMensonges     = 0;
    private int partagesCeTour      = 0;
    private int amitiesCeTour       = 0;
    private int reproductionsCeTour = 0;
    private final List<Integer> histPartages      = new ArrayList<>();
    private final List<Integer> histAmities       = new ArrayList<>();
    private final List<Integer> histReproductions = new ArrayList<>();
    private int nbSincere  = 0;
    private int nbSociable = 0;
    private int nbEgoiste  = 0;
    private int nbMechante = 0;
    private int tourCourant = 0;

    // /** Enregistre un snapshot de l'état de la simulation à chaque tour. */
    public void enregistrerTour(int population, double energieMoyenne,
                                 int nbNourriture,
                                 int sincere, int sociable, int egoiste, int mechante) {
        this.tourCourant++;
        histPopulation.add(population);
        histEnergieMoyenne.add(energieMoyenne);
        histNbNourriture.add(nbNourriture);
        histPartages.add(partagesCeTour);
        histAmities.add(amitiesCeTour);
        histReproductions.add(reproductionsCeTour);

        this.nbSincere  = sincere;
        this.nbSociable = sociable;
        this.nbEgoiste  = egoiste;
        this.nbMechante = mechante;

        // Remettre à zéro les compteurs par tour
        partagesCeTour      = 0;
        amitiesCeTour       = 0;
        reproductionsCeTour = 0;
    }

    public void enregistrerPartage(boolean mensonge) {
        totalPartages++;
        partagesCeTour++;
        if (mensonge) totalMensonges++;
    }

    public void enregistrerAmitie() {
        totalAmities++;
        amitiesCeTour++;
    }

    public void enregistrerReproduction(int nbBebes) {
        totalReproductions++;
        reproductionsCeTour++;
        totalNaissances += nbBebes;
    }

    public void enregistrerMort() {
        totalMorts++;
    }

    // /** Enregistre un événement textuel (ex: apparition trésor) */
    public void enregistrerEvenement(String msg) {
        // Méthode d'extensibilité — peut être loggée ou stockée
        org.apache.log4j.Logger.getLogger(StatistiquesSimulation.class.getName()).info("[EVENT] " + msg);
    }

    public void reset() {
        histPopulation.clear();
        histEnergieMoyenne.clear();
        histNbNourriture.clear();
        histPartages.clear();
        histAmities.clear();
        histReproductions.clear();
        totalPartages = totalAmities = totalReproductions = 0;
        totalNaissances = totalMorts = totalMensonges = 0;
        partagesCeTour = amitiesCeTour = reproductionsCeTour = 0;
        nbSincere = nbSociable = nbEgoiste = nbMechante = 0;
        tourCourant = 0;
    }
    public List<Integer> getHistPopulation()      { return histPopulation; }
    public List<Double>  getHistEnergieMoyenne()  { return histEnergieMoyenne; }
    public List<Integer> getHistNbNourriture()    { return histNbNourriture; }
    public List<Integer> getHistPartages()        { return histPartages; }
    public List<Integer> getHistAmities()         { return histAmities; }
    public List<Integer> getHistReproductions()   { return histReproductions; }

    public int getTotalPartages()      { return totalPartages; }
    public int getTotalAmities()       { return totalAmities; }
    public int getTotalReproductions() { return totalReproductions; }
    public int getTotalNaissances()    { return totalNaissances; }
    public int getTotalMorts()         { return totalMorts; }
    public int getTotalMensonges()     { return totalMensonges; }
    public int getTourCourant()        { return tourCourant; }

    public int getNbSincere()  { return nbSincere; }
    public int getNbSociable() { return nbSociable; }
    public int getNbEgoiste()  { return nbEgoiste; }
    public int getNbMechante() { return nbMechante; }

    // /** Total interactions pour le camembert */
    public int getTotalInteractions() {
        return totalPartages + totalAmities + totalReproductions;
    }
}
