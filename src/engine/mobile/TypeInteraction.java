package engine.mobile;

/**
 * Énumère les types d'interactions possibles entre deux souris.
 * <p>
 * Le type d'interaction est déterminé lors de chaque rencontre entre
 * deux souris selon leurs sexes et leurs rôles (DONNEUR / RECEVEUR) :
 * </p>
 * <ul>
 *   <li><strong>Même sexe, D+R</strong> : {@link #PARTAGE_NOURRITURE} si le partage
 *       est accepté, {@link #AMITIE} sinon.</li>
 *   <li><strong>Sexe opposé</strong> : {@link #REPRODUCTION} si les conditions
 *       sont remplies (âge, énergie, population).</li>
 *   <li><strong>Même sexe, D+D ou R+R</strong> : {@link #INDIFFERENCE} — aucune
 *       interaction n'a lieu.</li>
 * </ul>
 *
 * @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma
 * @version 5.1
 * @see Interaction
 */
public enum TypeInteraction {

    /**
     * Interaction d'amitié entre deux souris de même sexe (D+R).
     * Le DONNEUR tente de partager une information mais le partage est refusé.
     * Un bonus d'énergie peut être accordé si l'un des deux est SINCERE.
     */
    AMITIE,

    /**
     * Interaction de reproduction entre un mâle et une femelle.
     * Donne naissance à 1 ou 2 souriceaux dans les cases adjacentes.
     * Les souriceaux héritent du caractère et de la couleur des parents.
     */
    REPRODUCTION,

    /**
     * Partage effectif d'information sur la nourriture entre un DONNEUR et un RECEVEUR.
     * La fiabilité de l'information dépend du caractère du DONNEUR :
     * SINCERE/SOCIABLE → vraie info, EGOISTE → 70% de mensonge, MECHANTE → toujours fausse.
     */
    PARTAGE_NOURRITURE,

    /**
     * Aucune interaction sociale (D+D ou R+R de même sexe).
     * Les deux souris se croisent sans échanger.
     */
    INDIFFERENCE,

    /**
     * Piège : le Receveur a suivi une fausse information et a heurté un obstacle.
     * Affiche un nuage "Oh ! Un piège !" sur la souris piégée.
     */
    PIEGE;

    /**
     * Indique si cette interaction est une interaction sociale visible
     * (amitié, reproduction ou partage), par opposition à l'indifférence.
     *
     * @return {@code true} si l'interaction génère un flash visuel sur la grille
     */
    public boolean estSociale() {
        return this == AMITIE || this == REPRODUCTION || this == PARTAGE_NOURRITURE || this == PIEGE;
    }
}
