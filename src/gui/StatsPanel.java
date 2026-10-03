package gui;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import engine.process.StatistiquesSimulation;

/**
 * Panneau gauche remplaçant le journal : affiche des graphiques dynamiques
 * sur la simulation (population, énergie, interactions, caractères).
 * Se met à jour à chaque tour.
 *
 * @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma
 */
public class StatsPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private StatistiquesSimulation stats;

    // Couleurs — thème clair
    private static final Color BG      = Color.WHITE;
    private static final Color BG_CARD = new Color(240, 244, 255);
    private static final Color GREEN   = new Color(22, 160, 80);
    private static final Color BLUE    = new Color(30, 100, 200);
    private static final Color ORANGE  = new Color(200, 90, 10);
    private static final Color RED     = new Color(200, 40, 40);
    private static final Color PURPLE  = new Color(120, 50, 180);
    private static final Color YELLOW  = new Color(180, 140, 0);
    private static final Color TEXT    = new Color(20, 20, 40);
    private static final Color MUTED   = new Color(80, 80, 110);

    public StatsPanel(StatistiquesSimulation stats) {
        this.stats = stats;
        setPreferredSize(new Dimension(420, 600));
        setBackground(BG);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,     RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int W = getWidth();
        int pad = 10;
        int y = 8;
        int cardW = W - 2 * pad;

        //TITRE
        y = drawTitre(g2, "Statistiques", pad, y, cardW);

        //COMPTEURS RAPIDES
        y = drawCompteurs(g2, pad, y, cardW);

        //COURBE : Population + Nourriture
        @SuppressWarnings("unchecked")
        java.util.List<Integer>[] seriesPop = new java.util.List[]{
            stats.getHistPopulation(), stats.getHistNbNourriture()};
        y = drawCourbe(g2, pad, y, cardW, 130,
            "Population & Nourriture",
            seriesPop,
            new Color[]{GREEN, YELLOW},
            new String[]{"Souris", "Nourriture"});

        //COURBE : Énergie moyenne
        y = drawCourbeDouble(g2, pad, y, cardW, 100,
            "Énergie moyenne",
            stats.getHistEnergieMoyenne(), BLUE);

        //CAMEMBERT : Interactions
        y = drawCamembert(g2, pad, y, cardW, 135);

        //BARRES : Caractères
        y = drawBarresCaracteres(g2, pad, y, cardW, 100);

        //FORMULES
        drawFormules(g2, pad, y, cardW);
    }

    // ──────────────────────────────────────────────
    private int drawTitre(Graphics2D g2, String txt, int x, int y, int w) {
        g2.setColor(new Color(30, 80, 200));
        g2.fillRoundRect(x, y, w, 38, 8, 8);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(txt, x + (w - fm.stringWidth(txt)) / 2, y + 26);
        return y + 44;
    }

    private int drawCompteurs(Graphics2D g2, int x, int y, int w) {
        int h = 72;
        g2.setColor(BG_CARD);
        g2.fillRoundRect(x, y, w, h, 8, 8);

        String[] labels = {"Tour", "Souris", "Naiss.", "Morts", "Partages", "Repro."};
        int[] valeurs = {
            stats.getTourCourant(),
            stats.getHistPopulation().isEmpty() ? 0 : stats.getHistPopulation().get(stats.getHistPopulation().size()-1),
            stats.getTotalNaissances(),
            stats.getTotalMorts(),
            stats.getTotalPartages(),
            stats.getTotalReproductions()
        };
        Color[] cols = {BLUE, GREEN, YELLOW, RED, ORANGE, PURPLE};

        int cellW = w / 6;
        for (int i = 0; i < 6; i++) {
            int cx = x + i * cellW + cellW / 2;
            g2.setFont(new Font("Arial", Font.BOLD, 22));
            g2.setColor(cols[i]);
            String val = String.valueOf(valeurs[i]);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(val, cx - fm.stringWidth(val)/2, y + 32);
            g2.setFont(new Font("Arial", Font.PLAIN, 14));
            g2.setColor(MUTED);
            fm = g2.getFontMetrics();
            g2.drawString(labels[i], cx - fm.stringWidth(labels[i])/2, y + 54);
        }
        return y + h + 6;
    }

    @SuppressWarnings("unchecked")
    private int drawCourbe(Graphics2D g2, int x, int y, int w, int h,
                            String titre, List<Integer>[] series,
                            Color[] colors, String[] seriesLabels) {
        g2.setColor(BG_CARD);
        g2.fillRoundRect(x, y, w, h, 8, 8);

        // Titre graphique
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.setColor(TEXT);
        g2.drawString(titre, x + 8, y + 18);

        // Légende — placée juste après le titre, pas à droite
        int lx = x + 8;
        int ly = y + 30;
        for (int s = 0; s < series.length; s++) {
            g2.setColor(colors[s]);
            g2.fillRect(lx + s * 90, ly, 12, 12);
            g2.setFont(new Font("Arial", Font.PLAIN, 14));
            g2.setColor(MUTED);
            g2.drawString(seriesLabels[s], lx + s * 90 + 15, ly + 11);
        }

        int gx = x + 30, gy = y + 46, gw = w - 38, gh = h - 54;
        drawAxes(g2, gx, gy, gw, gh);

        // Trouver max global
        int maxVal = 1;
        for (List<Integer> s : series)
            for (int v : s) maxVal = Math.max(maxVal, v);

        for (int s = 0; s < series.length; s++) {
            List<Integer> data = series[s];
            if (data.size() < 2) continue;
            g2.setColor(colors[s]);
            g2.setStroke(new BasicStroke(1.5f));
            int n = data.size();
            for (int i = 1; i < n; i++) {
                int x1 = gx + (i-1) * gw / Math.max(n-1, 1);
                int x2 = gx + i     * gw / Math.max(n-1, 1);
                int y1 = gy + gh - data.get(i-1) * gh / maxVal;
                int y2 = gy + gh - data.get(i)   * gh / maxVal;
                g2.drawLine(x1, y1, x2, y2);
            }
        }
        g2.setStroke(new BasicStroke(1f));

        g2.setFont(new Font("Arial", Font.PLAIN, 13));
        g2.setColor(MUTED);
        g2.drawString(String.valueOf(maxVal), x + 2, gy + 8);
        g2.drawString("0", x + 2, gy + gh);

        return y + h + 6;
    }

    private int drawCourbeDouble(Graphics2D g2, int x, int y, int w, int h,
                                  String titre, List<Double> data, Color col) {
        g2.setColor(BG_CARD);
        g2.fillRoundRect(x, y, w, h, 8, 8);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.setColor(TEXT);
        g2.drawString(titre, x + 8, y + 18);

        int gx = x + 30, gy = y + 24, gw = w - 38, gh = h - 32;
        drawAxes(g2, gx, gy, gw, gh);

        if (data.size() >= 2) {
            g2.setColor(col);
            g2.setStroke(new BasicStroke(1.5f));
            int n = data.size();
            for (int i = 1; i < n; i++) {
                int x1 = gx + (i-1) * gw / Math.max(n-1, 1);
                int x2 = gx + i     * gw / Math.max(n-1, 1);
                int y1 = gy + gh - (int)(data.get(i-1) * gh / 100.0);
                int y2 = gy + gh - (int)(data.get(i)   * gh / 100.0);
                g2.drawLine(x1, y1, x2, y2);
            }
            g2.setStroke(new BasicStroke(1f));
        }
        g2.setFont(new Font("Arial", Font.PLAIN, 13));
        g2.setColor(MUTED);
        g2.drawString("100", x + 2, gy + 8);
        g2.drawString("0", x + 2, gy + gh);
        return y + h + 6;
    }

    private int drawCamembert(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(BG_CARD);
        g2.fillRoundRect(x, y, w, h, 8, 8);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.setColor(TEXT);
        g2.drawString("Répartition des interactions", x + 8, y + 18);

        int total = stats.getTotalInteractions();

        if (total == 0) {
            g2.setColor(MUTED);
            g2.setFont(new Font("Arial", Font.ITALIC, 14));
            g2.drawString("En attente de données...", x + 20, y + h/2);
            return y + h + 6;
        }

        int[] vals = {
            stats.getTotalPartages(),
            stats.getTotalAmities(),
            stats.getTotalReproductions(),
            stats.getTotalMensonges()
        };
        Color[] cols = {GREEN, BLUE, PURPLE, RED};
        String[] lbls = {"Partages", "Amitiés", "Repros", "Mensonges"};

        int cx = x + 65, cy = y + h/2 + 12, r = 46;
        double angle = -90.0;
        for (int i = 0; i < vals.length; i++) {
            if (vals[i] == 0) continue;
            double sweep = 360.0 * vals[i] / total;
            g2.setColor(cols[i]);
            g2.fillArc(cx - r, cy - r, r*2, r*2, (int)angle, (int)sweep);
            angle += sweep;
        }
        g2.setColor(BG_CARD);
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(cx - r, cy - r, r*2, r*2);
        g2.setStroke(new BasicStroke(1f));

        int lx = x + 128, ly = y + 28;
        g2.setFont(new Font("Arial", Font.PLAIN, 14));
        for (int i = 0; i < vals.length; i++) {
            int pct = total > 0 ? (int)(100.0 * vals[i] / total) : 0;
            g2.setColor(cols[i]);
            g2.fillRect(lx, ly + i * 26, 12, 12);
            g2.setColor(TEXT);
            g2.drawString(lbls[i] + " " + pct + "%", lx + 18, ly + i * 26 + 12);
            g2.setColor(MUTED);
            g2.setFont(new Font("Arial", Font.PLAIN, 12));
            g2.drawString("(" + vals[i] + ")", lx + 18, ly + i * 26 + 24);
            g2.setFont(new Font("Arial", Font.PLAIN, 14));
        }

        return y + h + 6;
    }

    private int drawBarresCaracteres(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(BG_CARD);
        g2.fillRoundRect(x, y, w, h, 8, 8);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.setColor(TEXT);
        g2.drawString("Caractères actuels", x + 8, y + 18);

        int[] vals = {stats.getNbSincere(), stats.getNbSociable(), stats.getNbEgoiste(), stats.getNbMechante()};
        Color[] cols = {GREEN, BLUE, ORANGE, RED};
        String[] lbls = {"Sincère", "Sociable", "Égoïste", "Méchante"};
        int total = vals[0] + vals[1] + vals[2] + vals[3];
        if (total == 0) total = 1;

        int gx = x + 8, bh = 15, gap = 20;
        int barMaxW = w - 100;
        for (int i = 0; i < 4; i++) {
            int by = y + 26 + i * gap;
            int bw = Math.max(4, vals[i] * barMaxW / total);
            g2.setColor(cols[i]);
            g2.fillRoundRect(gx, by, bw, bh, 4, 4);
            g2.setFont(new Font("Arial", Font.PLAIN, 14));
            g2.setColor(TEXT);
            g2.drawString(lbls[i] + " (" + vals[i] + ")", gx + bw + 8, by + bh - 1);
        }
        return y + h + 6;
    }

    private void drawFormules(Graphics2D g2, int x, int y, int w) {
        if (y + 10 >= getHeight()) return;
        int avail = getHeight() - y - 8;
        if (avail < 50) return;

        g2.setColor(BG_CARD);
        g2.fillRoundRect(x, y, w, avail, 8, 8);

        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.setColor(BLUE);
        g2.drawString("Formules de calcul", x + 8, y + 20);

        g2.setFont(new Font("Arial", Font.PLAIN, 14));
        String[] lines = {
            "P(partage) = 0.5 × kC × kE × kConf",
            "  kC: Sincère=1.0 · Sociable=0.9",
            "  kC: Égoïste=0.5 · Méchante=0.3",
            "  kE = 1 + (100-énergie_R)/200",
            "  kConf = 1 + confiance/200",
            "",
            "P(repro) = 0.20 + E_moy/200",
            "  +0.10 si Sincère · -0.10 si Méchante",
            "  Cond: âge≥3 · énergie≥50 · sexe≠",
        };
        Color[] lc = {ORANGE, MUTED, MUTED, MUTED, MUTED, MUTED, PURPLE, MUTED, MUTED};
        for (int i = 0; i < lines.length; i++) {
            int ly = y + 38 + i * 18;
            if (ly + 14 > y + avail) break;
            g2.setColor(lc[i]);
            g2.drawString(lines[i], x + 6, ly);
        }
    }

    private void drawAxes(Graphics2D g2, int gx, int gy, int gw, int gh) {
        g2.setColor(new Color(60, 55, 90));
        g2.drawLine(gx, gy, gx, gy + gh);
        g2.drawLine(gx, gy + gh, gx + gw, gy + gh);
        // Grille légère
        g2.setColor(new Color(40, 35, 65));
        for (int i = 1; i <= 4; i++) {
            int gy2 = gy + gh * i / 4;
            g2.drawLine(gx, gy2, gx + gw, gy2);
        }
    }
}
