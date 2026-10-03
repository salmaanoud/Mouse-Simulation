package engine.mobile;

import engine.map.Case;

/**
 * Représente un obstacle sur la grille de simulation.
 * <p>
 * Un obstacle est un élément fixe et infranchissable qui bloque le déplacement
 * des souris. Lorsqu'une souris tente d'entrer dans la case d'un obstacle,
 * elle reste sur place et perd de l'énergie (pénalité définie par
 * {@link TypeObstacle#getCoutEnergie()}).
 * </p>
 * <p>
 * La case de l'obstacle est mémorisée comme dangereuse dans la {@link Memoire}
 * de la souris qui l'a percuté, et peut être utilisée par un DONNEUR menteur
 * pour tromper un RECEVEUR lors d'un partage.
 * </p>
 *
 * @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma
 * @version 5.1
 * @see TypeObstacle
 * @see Element
 */
public class Obstacle extends Element {

    /** Type de cet obstacle (ROCHER, TRONC ou LAC). */
    private TypeObstacle type;

    /**
     * Indique si cet obstacle est franchissable.
     * Dans la version actuelle, tous les obstacles sont infranchissables.
     */
    private boolean franchissable;

    /**
     * Crée un obstacle de type ROCHER à la position donnée.
     *
     * @param position la case de la grille où l'obstacle est placé
     */
    public Obstacle(Case position) {
        super(position);
        this.type         = TypeObstacle.ROCHER;
        this.franchissable = false;
    }

    /**
     * Crée un obstacle du type spécifié à la position donnée.
     *
     * @param position la case de la grille où l'obstacle est placé
     * @param type     le type de l'obstacle ({@link TypeObstacle})
     */
    public Obstacle(Case position, TypeObstacle type) {
        super(position);
        this.type         = type;
        this.franchissable = false;
    }

    /**
     * Retourne le type de cet obstacle.
     *
     * @return type de l'obstacle (ROCHER, TRONC ou LAC)
     */
    public TypeObstacle getType() { return type; }

    /**
     * Indique si cet obstacle peut être traversé par une souris.
     * Toujours {@code false} dans la version actuelle.
     *
     * @return {@code false} — les obstacles sont infranchissables
     */
    public boolean estFranchissable() { return franchissable; }

    /**
     * Retourne le niveau de difficulté de cet obstacle.
     * Délègue à {@link TypeObstacle#getDifficulte()}.
     *
     * @return niveau de difficulté (1 à 3)
     */
    public int getDifficulte() { return type.getDifficulte(); }
}
