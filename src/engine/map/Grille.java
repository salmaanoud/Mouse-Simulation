package engine.map;

/**
 * Représente la grille bidimensionnelle de la simulation.
 * <p>
 * La grille est le conteneur spatial principal de l'environnement.
 * Elle est composée d'un tableau de {@link Case} organisées en lignes
 * et en colonnes. Sa taille est définie à la construction à partir
 * des constantes de {@code Configuration}.
 * </p>
 * <p>
 * La grille ne contient que des références spatiales (les cases).
 * Les éléments qui occupent ces cases (souris, obstacles, nourriture)
 * sont gérés par {@code Environnement}.
 * </p>
 *
 * @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma
 * @version 5.1
 * @see Case
 */
public class Grille {

    /** Tableau 2D contenant toutes les cases de la grille. */
    private Case[][] blocks;

    /** Nombre total de lignes de la grille. */
    private int lineCount;

    /** Nombre total de colonnes de la grille. */
    private int columnCount;

    /**
     * Construit une grille de dimensions {@code lineCount × columnCount}.
     * <p>
     * Toutes les cases sont créées et initialisées avec leurs coordonnées
     * respectives lors de la construction.
     * </p>
     *
     * @param lineCount   nombre de lignes
     * @param columnCount nombre de colonnes
     */
    public Grille(int lineCount, int columnCount) {
        init(lineCount, columnCount);
        for (int lineIndex = 0; lineIndex < lineCount; lineIndex++) {
            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                blocks[lineIndex][columnIndex] = new Case(lineIndex, columnIndex);
            }
        }
    }

    /**
     * Initialise les dimensions de la grille et alloue le tableau de cases.
     *
     * @param lineCount   nombre de lignes
     * @param columnCount nombre de colonnes
     */
    private void init(int lineCount, int columnCount) {
        this.lineCount   = lineCount;
        this.columnCount = columnCount;
        blocks = new Case[lineCount][columnCount];
    }

    /**
     * Retourne l'ensemble du tableau 2D de cases.
     *
     * @return tableau de cases {@code Case[ligne][colonne]}
     */
    public Case[][] getBlocks() {
        return blocks;
    }

    /**
     * Retourne le nombre de lignes de la grille.
     *
     * @return nombre de lignes
     */
    public int getLineCount() {
        return lineCount;
    }

    /**
     * Retourne le nombre de colonnes de la grille.
     *
     * @return nombre de colonnes
     */
    public int getColumnCount() {
        return columnCount;
    }

    /**
     * Retourne la case située à la position {@code (line, column)}.
     *
     * @param line   numéro de ligne
     * @param column numéro de colonne
     * @return la case correspondante
     */
    public Case getBlock(int line, int column) {
        return blocks[line][column];
    }

    /**
     * Indique si la case est sur la première ligne de la grille.
     *
     * @param block la case à tester
     * @return {@code true} si la case est en haut
     */
    public boolean isOnTop(Case block) {
        return block.getLine() == 0;
    }

    /**
     * Indique si la case est sur la dernière ligne de la grille.
     *
     * @param block la case à tester
     * @return {@code true} si la case est en bas
     */
    public boolean isOnBottom(Case block) {
        return block.getLine() == lineCount - 1;
    }

    /**
     * Indique si la case est sur la première colonne de la grille.
     *
     * @param block la case à tester
     * @return {@code true} si la case est sur le bord gauche
     */
    public boolean isOnLeftBorder(Case block) {
        return block.getColumn() == 0;
    }

    /**
     * Indique si la case est sur la dernière colonne de la grille.
     *
     * @param block la case à tester
     * @return {@code true} si la case est sur le bord droit
     */
    public boolean isOnRightBorder(Case block) {
        return block.getColumn() == columnCount - 1;
    }

    /**
     * Indique si la case se trouve sur l'un des quatre bords de la grille.
     *
     * @param block la case à tester
     * @return {@code true} si la case est en bordure de grille
     */
    public boolean isOnBorder(Case block) {
        return isOnTop(block) || isOnBottom(block)
            || isOnLeftBorder(block) || isOnRightBorder(block);
    }
}
