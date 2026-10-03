package engine.mobile;

/**
 * Représente une entrée individuelle dans le journal intime d'une souris. Chaque entrée associe un message textuel (rédigé à la première personne) au numéro du tour de simulation auquel il a été écrit. L'ensemble des entrées forme le journal de vie de la souris, consultable en cliquant sur elle dans l'interface graphique. @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma /
 *
 * @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma
 */
public class EntreeJournal {

    // /** Numéro du tour de simulation auquel cette entrée a été créée. */
    private int temps;

    // /** Texte de l'entrée, rédigé à la première personne. */
    private String contenu;

    // Crée une nouvelle entrée de journal. /
    public EntreeJournal(int temps, String contenu) {
        this.temps   = temps;
        this.contenu = contenu;
    }

    // Retourne le numéro du tour auquel cette entrée a été écrite. /
    public int getTemps() { return temps; }

    // Retourne le contenu textuel de cette entrée. /
    public String getContenu() { return contenu; }

    // Modifie le contenu de cette entrée. /
    public void modifierContenu(String nouveauTexte) { this.contenu = nouveauTexte; }

    // Retourne une représentation lisible de l'entrée. /
    @Override
    public String toString() {
        return "[t=" + temps + "] " + contenu;
    }
}
