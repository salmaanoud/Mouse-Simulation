package engine.mobile;

import java.util.ArrayList;
import java.util.List;
import engine.map.Case;

/**
 * Mémoire interne d'une souris, stockant ses expériences passées.
 * <p>
 * La mémoire joue un rôle central dans le comportement des souris :
 * elle mémorise les cases où de la nourriture a été trouvée, les cases
 * dangereuses (obstacles), les relations sociales établies et les
 * interactions passées. Ces données influencent les décisions futures.
 * </p>
 * <p>
 * En particulier, lors d'un partage d'information (D → R), le DONNEUR
 * transmet ses {@code casesNourritureConnues} au RECEVEUR (si sincère)
 * ou ses {@code casesDangereuses} (si menteur).
 * </p>
 *
 * @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma
 * @version 5.1
 * @see Souris
 * @see RelationSociale
 * @see Interaction
 */
public class Memoire {

    /** Cases où de la nourriture a été trouvée ou signalée par un DONNEUR fiable. */
    private List<Case> casesNourritureConnues = new ArrayList<>();

    /** Cases mémorisées comme dangereuses (obstacles percutés). */
    private List<Case> casesDangereuses = new ArrayList<>();

    /** Relations sociales établies avec d'autres souris. */
    private List<RelationSociale> relationSociales = new ArrayList<>();

    /** Historique de toutes les interactions sociales vécues. */
    private List<Interaction> interactionsPassees = new ArrayList<>();

    /**
     * Mémorise une case comme source de nourriture connue.
     * Les doublons sont ignorés.
     *
     * @param c la case à mémoriser comme source de nourriture
     */
    public void ajouterSouvenir(Case c) {
        if (c != null && !casesNourritureConnues.contains(c))
            casesNourritureConnues.add(c);
    }

    /**
     * Mémorise une case comme dangereuse (obstacle).
     * Les doublons sont ignorés.
     *
     * @param c la case à mémoriser comme dangereuse
     */
    public void ajouterDanger(Case c) {
        if (c != null && !casesDangereuses.contains(c))
            casesDangereuses.add(c);
    }

    /**
     * Enregistre une nouvelle relation sociale dans la mémoire.
     *
     * @param relation la relation à ajouter
     */
    public void ajouterRelation(RelationSociale relation) {
        if (relation != null)
            relationSociales.add(relation);
    }

    /**
     * Enregistre une interaction passée dans l'historique.
     *
     * @param interaction l'interaction à mémoriser
     */
    public void ajouterInteraction(Interaction interaction) {
        if (interaction != null)
            interactionsPassees.add(interaction);
    }

    /**
     * Retourne la liste des cases connues comme sources de nourriture.
     *
     * @return liste des cases nourriture mémorisées
     */
    public List<Case> getCasesNourritureConnues() { return casesNourritureConnues; }

    /**
     * Retourne la liste des cases mémorisées comme dangereuses.
     *
     * @return liste des cases dangereuses mémorisées
     */
    public List<Case> getCasesDangereuses() { return casesDangereuses; }

    /**
     * Retourne la liste des relations sociales mémorisées.
     *
     * @return liste des relations sociales
     */
    public List<RelationSociale> getRelationSociales() { return relationSociales; }

    /**
     * Retourne l'historique de toutes les interactions sociales passées.
     *
     * @return liste des interactions passées
     */
    public List<Interaction> getInteractionsPassees() { return interactionsPassees; }
}
