package gui;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import engine.mobile.EntreeJournal;
import engine.mobile.Souris;

/**
 * Fenêtre popup affichant le journal intime d'une souris.
 * S'ouvre au centre de l'écran sur clic d'une souris dans la grille.
 *
 * @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma
 */
public class JournalFenetre extends JDialog {

    private static final long serialVersionUID = 1L;

    private static JournalFenetre instance = null;

    private JLabel      titreLabel;
    private JTextArea   textArea;
    private JLabel      statsLabel;
    private Souris      sourisCourante;

    private JournalFenetre(Frame owner) {
        super(owner, "Journal intime", false); // non-modal pour ne pas bloquer
        setSize(420, 520);
        setResizable(false);

        Container cp = getContentPane();
        cp.setLayout(new BorderLayout(6, 6));
        cp.setBackground(new Color(235, 170, 170));

        //En-tête
        JPanel header = new JPanel(new BorderLayout(4, 4));
        header.setBackground(new Color(220, 140, 140));
        header.setBorder(BorderFactory.createEmptyBorder(10, 14, 6, 14));

        titreLabel = new JLabel("Sélectionnez une souris", SwingConstants.CENTER);
        titreLabel.setFont(new Font("Arial", Font.BOLD, 15));
        titreLabel.setForeground(new Color(30, 0, 0));
        header.add(titreLabel, BorderLayout.NORTH);

        statsLabel = new JLabel(" ", SwingConstants.CENTER);
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        statsLabel.setForeground(new Color(30, 0, 0));
        header.add(statsLabel, BorderLayout.SOUTH);

        cp.add(header, BorderLayout.NORTH);

        //Zone texte
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Arial", Font.BOLD, 14));
        textArea.setBackground(new Color(255, 210, 210));
        textArea.setForeground(new Color(40, 0, 0));
        textArea.setCaretColor(Color.BLACK);
        textArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 120, 120)));
        cp.add(scroll, BorderLayout.CENTER);

        // ── Bouton fermer ──
        JButton fermer = new JButton("Fermer");
        fermer.setBackground(new Color(200, 80, 80));
        fermer.setForeground(Color.WHITE);
        fermer.setFocusPainted(false);
        fermer.addActionListener(e -> setVisible(false));
        JPanel bottom = new JPanel();
        bottom.setBackground(new Color(220, 140, 140));
        bottom.add(fermer);
        cp.add(bottom, BorderLayout.SOUTH);

        setLocationRelativeTo(owner);
    }

    /** Singleton — une seule fenêtre journal ouverte à la fois. */
    public static JournalFenetre getInstance(Frame owner) {
        if (instance == null) instance = new JournalFenetre(owner);
        return instance;
    }

    /** Affiche le journal d'une souris et rend la fenêtre visible. */
    public void afficher(Souris s) {
        this.sourisCourante = s;
        rafraichir();
        if (!isVisible()) setVisible(true);
        toFront();
    }

    /** Rafraîchit le contenu sans changer la souris sélectionnée. */
    public void rafraichir() {
        if (sourisCourante == null) return;

        Souris s = sourisCourante;
        String sexe  = s.getSexe().toString();
        String role  = s.isDonneur() ? "DONNEUR" : "RECEVEUR";
        String carac = s.getCaractere().toString();
        String etat  = s.estVivante() ? "vivante" : "DÉCÉDÉE";

        titreLabel.setText("Souris #" + s.getId()
            + "  (" + sexe + " · " + carac + " · " + role + ")");
        titreLabel.setForeground(s.estVivante()
            ? new Color(60, 10, 10) : new Color(180, 0, 0));

        statsLabel.setText("Age: " + s.getAge()
            + "  ·  Énergie: " + s.getEnergie()
            + "  ·  " + etat
            + "  ·  Mémoire: " + s.getMemoire().getCasesNourritureConnues().size() + " case(s)");

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

    public void reset() {
        sourisCourante = null;
        titreLabel.setText("Sélectionnez une souris");
        statsLabel.setText(" ");
        textArea.setText("");
    }

    public Souris getSourisCourante() { return sourisCourante; }
}
