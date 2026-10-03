package engine.mobile;

/**
 * Énumère les caractères (personnalités) possibles d'une souris. Le caractère est attribué aléatoirement à la création et peut être hérité partiellement des parents lors d'une reproduction (45% parent 1, 45% parent 2, 10% mutation aléatoire). Il influence deux comportements principaux : Fiabilité de l'information partagée (rôle DONNEUR) : un SINCERE ou SOCIABLE dit toujours la vérité ; un EGOISTE ment 70% du temps ; un MECHANTE ment toujours. Probabilité d'accepter un partage (rôle RECEVEUR) : un SINCERE est le plus réceptif (facteur 1.0), un MECHANTE est le moins réceptif (facteur 0.3). /
 *
 * @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma
 */
public enum Caractere {

    // Souris honnête et bienveillante. En tant que DONNEUR : partage toujours une information vraie. En tant que RECEVEUR : très réceptive aux informations (facteur 1.0). Bonus : donne 5 points d'énergie à une souris amie affaiblie. /
    SINCERE,

    // Souris malveillante. En tant que DONNEUR : ment toujours (partage de fausses informations). En tant que RECEVEUR : très peu réceptive (facteur 0.3). Pénalise les probabilités de reproduction. /
    MECHANTE,

    // Souris centrée sur ses propres intérêts. En tant que DONNEUR : ment 70% du temps. En tant que RECEVEUR : moyennement réceptive (facteur 0.5). Pénalise légèrement les probabilités de reproduction. /
    EGOISTE,

    // Souris ouverte et communicative. En tant que DONNEUR : partage toujours une information vraie. En tant que RECEVEUR : bonne réceptivité (facteur 0.9). /
    SOCIABLE
}
