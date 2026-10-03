package engine.mobile;

/**
 * Énumère les deux sexes possibles d'une souris dans la simulation. Le sexe d'une souris est attribué aléatoirement à sa création et ne change jamais au cours de sa vie. Il joue un rôle central dans la gestion des interactions : Deux souris de même sexe (M+M ou F+F) peuvent échanger des informations sur la nourriture si l'un est DONNEUR et l'autre RECEVEUR. Deux souris de sexe opposé (M+F) peuvent se reproduire, quel que soit leur rôle D/R. /
 *
 * @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma
 */
public enum Sexe {

    // /** Souris mâle — représentée par une image sans oreilles roses. */
    MALE,

    // /** Souris femelle — représentée par une image avec oreilles roses. */
    FEMELLE;

    // Indique si ce sexe est compatible pour une reproduction avec un autre sexe. La reproduction nécessite que les deux sexes soient différents. /
    public boolean estCompatible(Sexe autre) {
        return this != autre;
    }
}
