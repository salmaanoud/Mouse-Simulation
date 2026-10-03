package engine.mobile;

import engine.map.Case;

/**
 * Représente une source de nourriture placée sur la grille. La nourriture est un élément passif : une souris qui se déplace sur sa case la consomme automatiquement, gagne de l'énergie, et la source est supprimée de la simulation par Environnement. La case d'une source de nourriture consommée est mémorisée dans la Memoire de la souris comme case connue, et peut ensuite être partagée avec d'autres souris lors d'une interaction D → R. @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma /
 *
 * @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma
 */
public class Nourriture extends Element {

    // /** Type de cette nourriture (FROMAGE par défaut). */
    private TypeNourriture type;

    // /** Valeur énergétique selon le type. */
    private int valeurEnergie;

    // /** Crée une nourriture de type FROMAGE (25 pts) par défaut. */
    public Nourriture(Case position) {
        super(position);
        this.type          = TypeNourriture.FROMAGE;
        this.valeurEnergie = this.type.getValeurEnergie();
    }

    // /** Crée une nourriture du type spécifié. */
    public Nourriture(Case position, TypeNourriture type) {
        super(position);
        this.type          = type;
        this.valeurEnergie = type.getValeurEnergie();
    }

    // /** Retourne le type de cette nourriture. */
    public TypeNourriture getType() { return type; }

    // Retourne la valeur énergétique apportée par cette source de nourriture. /
    public int getValeurEnergie() {
        return valeurEnergie;
    }

    // Marque cette source comme consommée. La suppression réelle de la grille est effectuée par Environnement. /
    public void consommer() {
        // Suppression gérée par Environnement.nextRound()
    }
}
