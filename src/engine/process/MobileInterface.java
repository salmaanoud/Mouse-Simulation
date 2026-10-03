package engine.process;

import java.util.List;
import engine.mobile.Nourriture;
import engine.mobile.Obstacle;
import engine.mobile.Souris;

/**
 * Interface définissant le contrat du moteur de simulation. MobileInterface est implémentée par Environnement et expose toutes les opérations nécessaires à l'interface graphique (MainGUI, GameDisplay) pour piloter la simulation : avancer d'un tour, accéder aux éléments, ajouter des entités, réinitialiser. Cette interface permet de découpler la logique de simulation de la couche graphique, facilitant les évolutions futures. @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma /
 *
 * @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma
 */
public interface MobileInterface {

    // Avance la simulation d'un tour. À chaque tour, les opérations suivantes sont effectuées dans l'ordre : vieillissement, gestion de la faim, déplacement avec attraction, consommation de nourriture, gestion des interactions sociales et reproductives, suppression des souris mortes. /
    void nextRound();

    // Retourne la liste de toutes les souris vivantes dans la simulation. /
    List<Souris> getMice();

    // Retourne la liste de tous les obstacles présents sur la grille. /
    List<Obstacle> getObstacles();

    // Retourne la liste de toutes les sources de nourriture présentes. /
    List<Nourriture> getFoods();

    // Retourne le numéro du tour courant de la simulation. /
    int getTime();

    // Retourne le nombre de souris actuellement vivantes. /
    int getMouseCount();

    // Retourne le nombre de sources de nourriture restantes. /
    int getFoodCount();

    // Retourne le nombre d'obstacles présents sur la grille. /
    int getObstacleCount();

    // Ajoute une souris avec des paramètres aléatoires sur une case libre. /
    void addMouse();

    // Ajoute une source de nourriture sur une case libre aléatoire. /
    void addFood();

    // Ajoute un obstacle sur une case libre aléatoire. /
    void addObstacle();

    // Réinitialise complètement la simulation : supprime tous les éléments, remet le temps à zéro et replace les entités initiales. /
    void reset();
}

    // NOTE: getStats() est accessible via cast vers Environnement
