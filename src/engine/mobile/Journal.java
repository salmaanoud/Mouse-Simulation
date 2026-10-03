package engine.mobile;

import java.util.ArrayList;
import java.util.List;

/**
 * Journal intime d'une souris, composé d'une liste d'entrées textuelles. Chaque souris possède son propre journal dans lequel sont enregistrés tous les événements significatifs de sa vie : déplacements, repas, interactions sociales, naissances, faim, mort... Le journal est consultable dans le panneau latéral de l'interface graphique en cliquant sur une souris. Les entrées sont affichées en ordre anti-chronologique (la plus récente en premier). @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma /
 *
 * @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma
 */
public class Journal {

    // /** Liste ordonnée des entrées du journal, du plus ancien au plus récent. */
    private List<EntreeJournal> entrees = new ArrayList<>();

    // Ajoute une nouvelle entrée dans le journal. /
    public void ajouterEntree(String texte, int temps) {
        entrees.add(new EntreeJournal(temps, texte));
    }

    // Retourne la liste complète des entrées du journal, dans l'ordre chronologique (la plus ancienne en premier). /
    public List<EntreeJournal> getEntrees() {
        return entrees;
    }

    // Ajoute une entrée dans le journal (alias de #ajouterEntree). /
    public void modifierContenu(String texte, int temps) {
        ajouterEntree(texte, temps);
    }
}
