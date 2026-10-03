package engine.mobile;

/**
 * Énumère les types d'obstacles pouvant apparaître sur la grille. Chaque type d'obstacle est caractérisé par un nom, un niveau de difficulté et un coût en énergie infligé à la souris qui le percute. La souris ne peut pas traverser un obstacle — elle reste sur place et perd de l'énergie. @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma /
 *
 * @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma
 */
public enum TypeObstacle {

    // /** Rocher — obstacle de difficulté moyenne (coût : 10 points d'énergie). */
    ROCHER("Rocher", 2),

    // /** Tronc d'arbre — obstacle facile à contourner (coût : 5 points d'énergie). */
    TRONC("Tronc", 1),

    // /** Lac — obstacle le plus pénalisant (coût : 15 points d'énergie). */
    LAC("Lac", 3),

    // /** Bûche — tronc tombé (coût : 10 points d'énergie). */
    BUCHE("Bûche", 2),

    // /** Rivière — cours d'eau difficile (coût : 15 points d'énergie). */
    RIVIERE("Rivière", 3);

    // /** Nom lisible de l'obstacle. */
    private String nom;

    // /** Niveau de difficulté (1 = faible, 3 = élevé). */
    private int difficulte;

    // /** Coût en énergie calculé depuis la difficulté (difficulte × 5). */
    private int coutEnergie;

    // Construit un type d'obstacle. /
    TypeObstacle(String nom, int difficulte) {
        this.nom        = nom;
        this.difficulte = difficulte;
        this.coutEnergie = difficulte * 5;
    }

    // Retourne le nom de l'obstacle. /
    public String getNom() { return nom; }

    // Retourne le niveau de difficulté de l'obstacle. /
    public int getDifficulte() { return difficulte; }

    // Retourne le coût en énergie de cet obstacle. /
    public int getCoutEnergie() { return coutEnergie; }

    // Calcule la pénalité énergétique infligée à une souris percutant cet obstacle. Équivalent à #getCoutEnergie(). /
    public int calculerPenalite() { return coutEnergie; }
}
