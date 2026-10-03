package engine.mobile;


/**
 * Représente une interaction sociale entre deux souris et calcule la probabilité d'acceptation d'un partage d'information. Une interaction est créée par Environnement lors d'une rencontre et est ensuite enregistrée dans la Memoire des deux souris. Deux types de rencontres sont possibles : Même sexe (D+R) → TypeInteraction#AMITIE ou TypeInteraction#PARTAGE_NOURRITURE selon la probabilité calculée. Sexe opposé → TypeInteraction#REPRODUCTION (gérée dans Environnement, ici on enregistre seulement). La formule probabiliste de partage est : P = pBase × kCaractere × kEnergie × kConfiance pBase      = 0.50 (base neutre) kCaractere = facteur selon le caractère du RÉCEPTEUR : SINCERE → 1.00 | SOCIABLE → 0.90 | EGOISTE → 0.50 | MECHANTE → 0.30 kEnergie   = 1 + (100 - énergie du récepteur) / 200 kConfiance = 1 + niveau de confiance envers l'émetteur / 200 Résultat borné entre 0.05 et 0.95. @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma /
 *
 * @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma
 */
public class Interaction {

    // /** Type de cette interaction (AMITIE, PARTAGE_NOURRITURE ou REPRODUCTION). */
    private TypeInteraction type;

    // /** Numéro du tour de simulation auquel cette interaction a eu lieu. */
    private int date;

    // /** Première souris impliquée dans l'interaction. */
    private Souris souris1;

    // /** Deuxième souris impliquée dans l'interaction. */
    private Souris souris2;

    // Crée une nouvelle interaction entre deux souris. /
    public Interaction(TypeInteraction type, Souris souris1, Souris souris2, int date) {
        this.type    = type;
        this.souris1 = souris1;
        this.souris2 = souris2;
        this.date    = date;
    }

    // Enregistre cette interaction dans la mémoire des deux souris participantes. Doit être appelé après la création de l'interaction. /
    public void enregistrer() {
        souris1.getMemoire().ajouterInteraction(this);
        souris2.getMemoire().ajouterInteraction(this);
    }

    // Calcule la probabilité que le RECEVEUR accepte le partage d'information proposé par l'EMETTEUR (DONNEUR). La formule est : P = pBase × kCaractere × kEnergie × kConfiance, bornée entre 0.05 et 0.95. /
    public static double calculerProbabilitePartage(Souris emetteur, Souris recepteur) {
        double pBase = 0.50;

        // kCaractere : réceptivité du RÉCEPTEUR selon son caractère
        double kCaractere;
        switch (recepteur.getCaractere()) {
            case SINCERE:  kCaractere = 1.00; break; // très réceptif
            case SOCIABLE: kCaractere = 0.90; break; // bien réceptif
            case EGOISTE:  kCaractere = 0.50; break; // peu réceptif
            case MECHANTE: kCaractere = 0.30; break; // très peu réceptif
            default:       kCaractere = 0.70;
        }

        // kEnergie : plus le récepteur est affamé, plus il accepte l'info
        double kEnergie = 1.0 + (100.0 - recepteur.getEnergie()) / 200.0;

        // kConfiance : relation préalable entre les deux souris
        double confiance  = getNiveauConfiance(recepteur, emetteur);
        double kConfiance = 1.0 + confiance / 200.0;

        double p = pBase * kCaractere * kEnergie * kConfiance;
        return Math.max(0.05, Math.min(0.95, p));
    }

    // Recherche le niveau de confiance que qui a établi envers cible dans sa mémoire sociale. Retourne 0 si aucune relation n'existe. /
    private static double getNiveauConfiance(Souris qui, Souris cible) {
        for (RelationSociale r : qui.getMemoire().getRelationSociales()) {
            if (r.getSourisCible().getId() == cible.getId())
                return r.getNiveauConfiance();
        }
        return 0.0;
    }

    // Retourne le type de cette interaction. /
    public TypeInteraction getType() { return type; }

    // Retourne le numéro du tour auquel cette interaction a eu lieu. /
    public int getDate() { return date; }

    // Retourne la première souris participante. /
    public Souris getSouris1() { return souris1; }

    // Retourne la deuxième souris participante. /
    public Souris getSouris2() { return souris2; }

    // Retourne une description textuelle de cette interaction. /
    @Override
    public String toString() {
        return "[Tour " + date + "] " + type
            + " : souris#" + souris1.getId()
            + " <-> souris#" + souris2.getId();
    }
}
