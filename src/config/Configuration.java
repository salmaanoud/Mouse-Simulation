package config;

/**
 * Configuration globale — grille calculée dynamiquement selon l'écran.
 *
 * @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma
 * @version 5.4
 */
public class Configuration {

    public static final int GAME_SPEED = 400;

    /** Largeur du panneau statistiques (gauche). */
    public static final int STATS_WIDTH   = 420;
    /** Largeur du panneau boutons (droite). */
    public static final int CONTROL_WIDTH = 210;
    /** Hauteur de la barre d'infos (bas). */
    public static final int STATUS_HEIGHT = 50;

    // Conservé pour compatibilité avec l'ancien code
    public static final int JOURNAL_WIDTH = STATS_WIDTH;
    public static final int TITLE_BAR_H   = 30;

    /** Taille d'une case (recalculée). */
    public static int BLOCK_SIZE = 40;

    /** Dimensions RÉELLES de la zone grille (recalculées selon l'écran). */
    public static int GRID_WIDTH  = 900;
    public static int GRID_HEIGHT = 680;

    // Énergies
    public static final int INITIAL_ENERGY           = 80;
    public static final int MAX_ENERGY               = 100;
    public static final int FOOD_ENERGY_GAIN         = 25;
    public static final int MOVE_ENERGY_COST         = 2;
    public static final int CONFLICT_ENERGY_COST     = 5;
    public static final int REPRODUCTION_ENERGY_COST = 20;

    // Paramètres modifiables
    public static int COLUMN_COUNT           = 25;
    public static int LINE_COUNT             = 17;
    public static int INITIAL_MOUSE_COUNT    = 20;
    public static int INITIAL_OBSTACLE_COUNT = 5;
    public static int INITIAL_FOOD_COUNT     = 18;
    public static int MAX_POPULATION         = 60;

    /**
     * Applique les paramètres et calcule BLOCK_SIZE + GRID_WIDTH/HEIGHT
     * pour que la grille remplisse exactement l'espace disponible à l'écran
     * (entre le panel stats à gauche, les boutons à droite, et la barre en bas).
     */
    public static void appliquer(int cols, int lines, int mice,
                                  int food, int obstacles, int maxPop) {
        COLUMN_COUNT           = cols;
        LINE_COUNT             = lines;
        INITIAL_MOUSE_COUNT    = mice;
        INITIAL_FOOD_COUNT     = food;
        INITIAL_OBSTACLE_COUNT = obstacles;
        MAX_POPULATION         = maxPop;

        try {
            java.awt.Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
            // Espace disponible = écran - stats - boutons - marges
            int availW = screen.width  - STATS_WIDTH - CONTROL_WIDTH - 16;
            // Hauteur = écran - barre titre Windows (~30) - barre titre JFrame (~25) - status
            int availH = screen.height - 30 - 25 - STATUS_HEIGHT - 8;

            // BLOCK_SIZE = plus petite valeur qui fait rentrer toute la grille
            int bw = availW / cols;
            int bh = availH / lines;
            BLOCK_SIZE = Math.max(4, Math.min(bw, bh));

            // Grille = exactement cols*BLOCK x lines*BLOCK (pas de pixel perdu)
            GRID_WIDTH  = cols  * BLOCK_SIZE;
            GRID_HEIGHT = lines * BLOCK_SIZE;

        } catch (Exception e) {
            BLOCK_SIZE  = 40;
            GRID_WIDTH  = cols  * BLOCK_SIZE;
            GRID_HEIGHT = lines * BLOCK_SIZE;
        }
    }

    public static int fontSize() {
        return Math.max(9, BLOCK_SIZE / 4);
    }

    private Configuration() {}
}
