package engine.mobile;

import engine.map.Case;

/**
 * Classe abstraite de base pour tous les éléments pouvant être placés sur la grille. Un élément possède un identifiant unique auto-incrémenté à la création, ainsi qu'une position sur la grille représentée par une Case. Les classes concrètes qui héritent de Element sont : Souris, Obstacle et Nourriture. @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma /
 *
 * @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma
 */
public abstract class Element {

    // /** Compteur statique partagé par tous les éléments pour générer des IDs uniques. */
    private static int compteur = 0;

    // /** Identifiant unique de cet élément, assigné automatiquement à la création. */
    protected int id;

    // /** Position actuelle de cet élément sur la grille. */
    protected Case position;

    // Construit un élément à la position donnée et lui assigne un ID unique. /
    public Element(Case position) {
        this.id       = ++compteur;
        this.position = position;
    }

    // Retourne l'identifiant unique de cet élément. /
    public int getId() {
        return id;
    }

    // Retourne la position actuelle de cet élément sur la grille. /
    public Case getPosition() {
        return position;
    }

    // Déplace cet élément vers la case spécifiée. /
    public void setPosition(Case position) {
        this.position = position;
    }
}
