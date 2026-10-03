package engine.process;

import config.Configuration;
import engine.map.Grille;

/**
 * Fabrique responsable de la construction des objets principaux de la simulation. GameBuilder est une classe utilitaire (méthodes statiques uniquement) qui centralise la création de la grille et du moteur de simulation. Elle est appelée une seule fois au démarrage par MainGUI. @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma /
 *
 * @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma
 */
public class GameBuilder {

    // Construit et retourne la grille de jeu aux dimensions définies dans Configuration (LINE_COUNT × COLUMN_COUNT). /
    public static Grille buildMap() {
        return new Grille(Configuration.LINE_COUNT, Configuration.COLUMN_COUNT);
    }

    // Construit et retourne le moteur de simulation (Environnement) initialisé avec la grille donnée. Les souris, obstacles et sources de nourriture sont placés aléatoirement sur la grille lors de cette initialisation. /
    public static MobileInterface buildInitMobile(Grille map) {
        return new Environnement(map);
    }
}
