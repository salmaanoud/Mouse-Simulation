package engine.mobile;

/**
 * Représente une relation sociale mémorisée entre deux souris. Une RelationSociale est créée et stockée dans la Memoire d'une souris lors d'une interaction sociale (amitié ou reproduction). Elle mémorise la cible de la relation, son type et un niveau de confiance qui influence les interactions futures (notamment la probabilité de partage). Exemple : après une interaction d'amitié entre S1 et S2, S1 mémorise une relation "Amitie" vers S2 avec un niveau de confiance de 10, et inversement. @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma /
 *
 * @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma
 */
public class RelationSociale {

    // /** La souris avec laquelle cette relation est établie. */
    private Souris sourisCible;

    // /** Type de relation : "Amitie" ou "partenaire" (reproduction). */
    private String typeRelation;

    // Niveau de confiance envers la souris cible (entre 0 et 100). Un niveau élevé augmente la probabilité d'accepter un partage d'information. /
    private int niveauConfiance;

    // Crée une nouvelle relation sociale. /
    public RelationSociale(Souris sourisCible, String typeRelation, int niveauConfiance) {
        this.sourisCible    = sourisCible;
        this.typeRelation   = typeRelation;
        this.niveauConfiance = niveauConfiance;
    }

    // Retourne la souris cible de cette relation. /
    public Souris getSourisCible() { return sourisCible; }

    // Retourne le type de cette relation. /
    public String getTypeRelation() { return typeRelation; }

    // Retourne le niveau de confiance actuel envers la souris cible. /
    public int getNiveauConfiance() { return niveauConfiance; }

    // Modifie le niveau de confiance en ajoutant la valeur donnée. Le résultat est borné entre 0 et 100. /
    public void modifierConfiance(int valeur) {
        this.niveauConfiance += valeur;
        if (this.niveauConfiance > 100) this.niveauConfiance = 100;
        if (this.niveauConfiance < 0)   this.niveauConfiance = 0;
    }
}
