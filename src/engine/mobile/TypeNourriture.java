package engine.mobile;

/**
 * Types de nourriture disponibles dans la simulation.
 *
 * @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma
 */
public enum TypeNourriture {
    FROMAGE("Fromage", 25),
    POMME("Pomme", 35),
    GRAINES("Graines", 15);

    private final String nom;
    private final int valeurEnergie;

    TypeNourriture(String nom, int valeurEnergie) {
        this.nom = nom;
        this.valeurEnergie = valeurEnergie;
    }

    public String getNom()          { return nom; }
    public int    getValeurEnergie(){ return valeurEnergie; }
}
