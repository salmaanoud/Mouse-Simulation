package engine.process;

import engine.map.Case;
import engine.mobile.TypeInteraction;

/**
 * Représente un flash visuel temporaire affiché sur la grille lors d'une interaction. Lors d'une interaction sociale (amitié, partage de nourriture, reproduction), un FlashInteraction est créé et stocké dans Environnement. Il reste visible pendant un nombre fixe de tours (#DUREE_TOURS), puis disparaît automatiquement, débloquant les deux souris concernées. Il mémorise la position des deux souris impliquées pour permettre à GameDisplay de tracer la ligne de communication et d'afficher le nuage ou le cœur au bon endroit. @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma /
 *
 * @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma
 */
public class FlashInteraction {

    // /** Durée d'affichage du flash en nombre de tours de simulation. */
    private static final int DUREE_TOURS = 3;

    // /** Position de la première souris (utilisée comme origine du flash). */
    private Case position;

    // /** Position de la deuxième souris (utilisée pour tracer la ligne de communication). */
    private Case position2;

    // /** Type d'interaction à représenter visuellement. */
    private TypeInteraction type;

    // /** Tour de simulation auquel ce flash a été créé. */
    private int tourCreation;

    // /** Tour courant, mis à jour à chaque appel de #setTourCourant(int). */
    private int tourCourant;

    // /** Identifiant de la première souris (pour la débloquer à l'expiration). */
    private int souris1Id;

    // /** Identifiant de la deuxième souris (pour la débloquer à l'expiration). */
    private int souris2Id;

    // Message court affiché au milieu de la ligne de communication. Exemples : "D#3 → info → R#7", "#2 ♥ #5". /
    private String messageComm;

    // Crée un nouveau flash d'interaction. /
    public FlashInteraction(Case position, Case position2, TypeInteraction type,
                            int souris1Id, int souris2Id, int tourCourant, String messageComm) {
        this.position     = position;
        this.position2    = position2;
        this.type         = type;
        this.souris1Id    = souris1Id;
        this.souris2Id    = souris2Id;
        this.tourCreation = tourCourant;
        this.tourCourant  = tourCourant;
        this.messageComm  = messageComm;
    }

    // Retourne la position de la première souris. /
    public Case getPosition()        { return position; }

    // Retourne la position de la deuxième souris. /
    public Case getPosition2()       { return position2; }

    // Retourne le type d'interaction représenté par ce flash. /
    public TypeInteraction getType() { return type; }

    // Retourne l'identifiant de la première souris. /
    public int getSouris1Id()        { return souris1Id; }

    // Retourne l'identifiant de la deuxième souris. /
    public int getSouris2Id()        { return souris2Id; }

    // Retourne le message de communication affiché dans le nuage. /
    public String getMessageComm()   { return messageComm; }

    // Met à jour le tour courant de la simulation. Appelé à chaque tour par Environnement#nextRound() pour permettre le calcul de l'expiration. /
    public void setTourCourant(int t) { this.tourCourant = t; }

    // Indique si ce flash est encore actif (visible sur la grille). Un flash expire après #DUREE_TOURS tours. /
    public boolean estActif() {
        return (tourCourant - tourCreation) < DUREE_TOURS;
    }
}
