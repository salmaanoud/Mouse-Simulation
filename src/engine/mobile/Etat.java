package engine.mobile;

/**
 * Énumère les états vitaux possibles d'une souris au cours de la simulation. L'état d'une souris évolue dynamiquement en fonction de son niveau d'énergie : #VIVANTE  : énergie ≥ 40 — état normal, la souris se déplace librement. #FATIGUEE : énergie entre 20 et 39 — la souris est affaiblie mais toujours active. #AFFAMEE  : énergie entre 1 et 19 — la souris est en danger, elle doit manger. #MORTE    : énergie ≤ 0 — la souris est retirée de la simulation. @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma /
 *
 * @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma
 */
public enum Etat {

    // /** La souris est en bonne santé (énergie ≥ 40). */
    VIVANTE,

    // /** La souris manque d'énergie (énergie entre 20 et 39) mais reste active. */
    FATIGUEE,

    // /** La souris est en danger de mort (énergie entre 1 et 19). */
    AFFAMEE,

    // /** La souris est morte (énergie ≤ 0) et sera supprimée de la simulation. */
    MORTE;

    // Indique si la souris est dans un état stable (vivante ou fatiguée), c'est-à-dire qu'elle n'est ni affamée ni morte. /
    public boolean estStable() {
        return this == VIVANTE || this == FATIGUEE;
    }
}
