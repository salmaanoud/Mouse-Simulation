package gui;

/**
 * Panneau latéral affichant le journal intime de la souris sélectionnée.
 * JournalPanel est positionné à gauche de la grille dans l'interface
 * principale. Il se met à jour automatiquement à chaque tour et lorsqu'une
 * souris est cliquée dans GameDisplay.
 * Le journal est affiché en ordre anti-chronologique (entrée la plus récente
 * en haut). L'en-tête affiche l'ID, le sexe, le caractère et le rôle
 * (DONNEUR/RECEVEUR) de la souris sélectionnée.
 * Le panneau est automatiquement vidé lors d'une réinitialisation de la
 * simulation (#reset()) et signale si la souris sélectionnée
 * est décédée.
 *
 * @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import engine.mobile.EntreeJournal;
import engine.mobile.Souris;

/**
 * Panneau latéral affichant le journal intime d'une souris sélectionnée.
 */
public class JournalPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private JLabel titreLabel;
    private JTextArea textArea;
    private Souris sourisCourante = null;

    public JournalPanel() {
        setLayout(new BorderLayout(5, 5));
        setPreferredSize(new Dimension(240, 400));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        setBackground(new Color(255, 248, 230));

        titreLabel = new JLabel("Journal — cliquez sur une souris", SwingConstants.CENTER);
        titreLabel.setFont(new Font("Arial", Font.BOLD, 12));
        titreLabel.setForeground(new Color(100, 60, 0));
        add(titreLabel, BorderLayout.NORTH);

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        textArea.setBackground(new Color(255, 252, 240));
        textArea.setForeground(new Color(50, 30, 0));

        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 160, 80)));
        add(scroll, BorderLayout.CENTER);
    }

    public void afficherJournal(Souris s) {
        this.sourisCourante = s;
        String sexe = s.getSexe().toString();
        String role = s.isDonneur() ? "Donneu·se" : "Receveu·se";
        titreLabel.setText("Souris#" + s.getId() + " (" + sexe + " | " + s.getCaractere() + " | " + role + ")");

        StringBuilder sb = new StringBuilder();
        List<EntreeJournal> entrees = s.getJournal().getEntrees();
        if (entrees.isEmpty()) {
            sb.append("Aucune entrée pour l'instant...");
        } else {
            for (int i = entrees.size() - 1; i >= 0; i--) {
                EntreeJournal e = entrees.get(i);
                sb.append("[Tour ").append(e.getTemps()).append("] ")
                  .append(e.getContenu()).append("\n");
            }
        }
        textArea.setText(sb.toString());
        textArea.setCaretPosition(0);
    }

    public void rafraichir() {
        if (sourisCourante != null && sourisCourante.estVivante()) {
            afficherJournal(sourisCourante);
        } else if (sourisCourante != null) {
            titreLabel.setText("Souris#" + sourisCourante.getId() + " — DÉCÉDÉE");
        }
    }

    public void reset() {
        sourisCourante = null;
        titreLabel.setText("Journal — cliquez sur une souris");
        textArea.setText("");
    }

    public Souris getSourisCourante() {
        return sourisCourante;
    }
}
