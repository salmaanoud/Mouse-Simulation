package gui;

/**
 * Panneau principal de rendu de la grille de simulation. GameDisplay est un JPanel Swing qui dessine à chaque repaint l'état courant de la simulation : grille en damier, obstacles, sources de nourriture, souris avec leurs sprites directionnels, et flashes d'interaction (nuage de communication, cœur de reproduction). Il gère également la détection de clic sur une souris : lorsque l'utilisateur clique sur une case occupée par une souris, un callback est invoqué pour mettre à jour le JournalPanel. Les flashes d'interaction sont rendus via drawFlashes : une ligne pointillée bleue avec un nuage BD pour les interactions d'amitié/partage, et une ligne rouge avec des cœurs pour les reproductions. Une barre d'informations en bas affiche en temps réel : le nombre d'obstacles, le tour courant, le nombre de souris, la nourriture restante et l'état de la simulation (En cours / En pause / Terminé). @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma /
 *
 * @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma
 */

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import javax.swing.JPanel;

import config.Configuration;
import engine.map.Grille;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import engine.mobile.Nourriture;
import engine.mobile.Obstacle;
import engine.mobile.Souris;
import engine.mobile.TypeInteraction;
import engine.process.Environnement;
import engine.process.FlashInteraction;
import engine.process.MobileInterface;

public class GameDisplay extends JPanel {

    private static final long serialVersionUID = 1L;

    private Grille map;
    private MobileInterface manager;
    private Map<String, java.awt.Image> imageCache = new HashMap<>();
    private PaintStrategy paintStrategy = new PaintStrategy();

    private static final Color FLASH_REPRODUCTION = new Color(220, 0, 0);
    private static final Color TEXTE_REPRO        = Color.WHITE;

    // Callback appelé quand l'utilisateur clique sur une souris
    private Consumer<Souris> onSourisClicked;
    private boolean paused = true;
    private boolean modeConfig = true; // true = on affiche l'écran de config
    private Runnable onLancer;         // callback quand l'utilisateur clique Lancer

    // Valeurs éditables dans l'overlay
    private int cfgCols       = Configuration.COLUMN_COUNT;
    private int cfgLines      = Configuration.LINE_COUNT;
    private int cfgSouris     = 12;
    private int cfgNourriture = 10;
    private int cfgObstacles  = Configuration.INITIAL_OBSTACLE_COUNT;
    private int cfgMaxPop     = 40;

    // Zones cliquables de l'overlay (remplies dans drawConfigOverlay)
    private java.awt.Rectangle[] btnMoins = new java.awt.Rectangle[6];
    private java.awt.Rectangle[] btnPlus  = new java.awt.Rectangle[6];
    private java.awt.Rectangle   btnLancer;
    private java.awt.Rectangle   btnVictoire;
    private java.awt.Rectangle   btnTresorManque;

    public GameDisplay(Grille map, MobileInterface manager) {
        try {
            java.io.InputStream is = getClass().getClassLoader()
                .getResourceAsStream("resources/tresor.png");
            if (is != null)
                imageCache.put("tresor", ImageIO.read(is));
        } catch (Exception e) { /* fallback */ }
        this.map     = map;
        this.manager = manager;

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
    }

    public void setOnSourisClicked(Consumer<Souris> callback) {
        this.onSourisClicked = callback;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public void setOnLancer(Runnable callback) {
        this.onLancer = callback;
    }

    // /** Met à jour la grille et le manager après reconfiguration. */
    public void updateManager(Grille newMap, MobileInterface newManager) {
        this.map     = newMap;
        this.manager = newManager;
    }

    private void handleClick(int px, int py) {
        if (btnVictoire != null && btnVictoire.contains(px, py)) {
            if (onLancer != null) onLancer.run();
            return;
        }
        if (btnTresorManque != null && btnTresorManque.contains(px, py)) {
            if (onLancer != null) onLancer.run();
            return;
        }
        if (modeConfig) {
            int[] vals = {cfgCols, cfgLines, cfgSouris, cfgNourriture, cfgObstacles, cfgMaxPop};
            int[] mins = {5, 5, 1, 1, 0, 5};
            int[] maxs = {40, 30, 30, 30, 20, 80};
            for (int i = 0; i < 6; i++) {
                if (btnMoins[i] != null && btnMoins[i].contains(px, py)) {
                    if (vals[i] > mins[i]) setVal(i, vals[i] - 1);
                    repaint(); return;
                }
                if (btnPlus[i] != null && btnPlus[i].contains(px, py)) {
                    if (vals[i] < maxs[i]) setVal(i, vals[i] + 1);
                    repaint(); return;
                }
            }
            if (btnLancer != null && btnLancer.contains(px, py)) {
                Configuration.appliquer(cfgCols, cfgLines, cfgSouris,
                                         cfgNourriture, cfgObstacles, cfgMaxPop);
                modeConfig = false;
                if (onLancer != null) onLancer.run();
                repaint();
            }
            return;
        }
        int s = Configuration.BLOCK_SIZE;
        int col = px / s;
        int row = py / s;
        for (Souris m : manager.getMice()) {
            if (m.getPosition().getLine() == row && m.getPosition().getColumn() == col) {
                if (onSourisClicked != null) {
                    onSourisClicked.accept(m);
                    repaint();
                }
                return;
            }
        }
    }

    private void setVal(int i, int v) {
        switch(i) {
            case 0: cfgCols       = v; break;
            case 1: cfgLines      = v; break;
            case 2: cfgSouris     = v; break;
            case 3: cfgNourriture = v; break;
            case 4: cfgObstacles  = v; break;
            case 5: cfgMaxPop     = v; break;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,        RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,   RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,           RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,       RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,      RenderingHints.VALUE_STROKE_NORMALIZE);
        int GW = Configuration.GRID_WIDTH;
        int GH = Configuration.GRID_HEIGHT;

        // Fond neutre si espace résiduel
        g2.setColor(new Color(30, 30, 30));
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Clip strict sur la grille
        java.awt.Shape oldClip = g2.getClip();
        g2.setClip(0, 0, GW, GH);

        paintStrategy.paint(map, g);

        // Toujours dessiner le contenu (visible derrière l'overlay)
        for (Obstacle o : manager.getObstacles())  paintStrategy.paint(o, g);
        for (Nourriture f : manager.getFoods())    paintStrategy.paint(f, g);
        for (Souris m : manager.getMice())         paintStrategy.paint(m, g);

        if (modeConfig) {
            drawConfigOverlay(g2);
            g2.setClip(oldClip);
            drawBottomInfos(g);
            return;
        }

        for (Obstacle o : manager.getObstacles())  paintStrategy.paint(o, g);
        for (Nourriture f : manager.getFoods())    paintStrategy.paint(f, g);
        for (Souris m : manager.getMice())         paintStrategy.paint(m, g);

        if (manager instanceof Environnement) {
            drawTresor(g2, (Environnement) manager);
            drawFlashes(g2, (Environnement) manager);
        }

        g2.setClip(oldClip);
        drawBottomInfos(g);

        if (manager instanceof Environnement && ((Environnement) manager).isSimTerminee()) {
            Environnement envCheck = (Environnement) manager;
            if (envCheck.isVictoireTresor() && envCheck.getGagnante() != null) {
                drawEcranVictoire(g2, envCheck, GW, GH);
            } else if (envCheck.isSadEnd()) {
                drawEcranSadEnd(g2, envCheck, GW, GH);
            } else {
                drawGameOver(g2);
            }
        }
    }

    private void drawConfigOverlay(Graphics2D g2) {
        int W = Configuration.GRID_WIDTH;
        int H = Configuration.GRID_HEIGHT;
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
        g2.setColor(new Color(15, 20, 40));
        g2.fillRect(0, 0, W, H + 70);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        // Carte centrale
        int cw = 420, ch = 360;
        int cx = (W - cw) / 2, cy = (H - ch) / 2;
        g2.setColor(new Color(25, 32, 55));
        g2.fillRoundRect(cx, cy, cw, ch, 18, 18);
        g2.setColor(new Color(60, 120, 220));
        g2.setStroke(new java.awt.BasicStroke(2f));
        g2.drawRoundRect(cx, cy, cw, ch, 18, 18);
        g2.setStroke(new java.awt.BasicStroke());

        // Titre
        g2.setColor(new Color(100, 200, 255));
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        String titre = "Paramétrage de la simulation";
        int tw = g2.getFontMetrics().stringWidth(titre);
        g2.drawString(titre, cx + (cw - tw) / 2, cy + 38);

        // Ligne séparatrice
        g2.setColor(new Color(60, 120, 220, 120));
        g2.fillRect(cx + 20, cy + 48, cw - 40, 1);

        // Paramètres
        String[] labels = {
            "Colonnes de la grille",
            "Lignes de la grille",
            "Nombre de souris",
            "Sources de nourriture",
            "Obstacles",
            "Population maximale"
        };
        int[] vals = {cfgCols, cfgLines, cfgSouris, cfgNourriture, cfgObstacles, cfgMaxPop};
        int[] mins = {5, 5, 1, 1, 0, 5};
        int[] maxs = {40, 30, 30, 30, 20, 80};

        int rowH = 38, startY = cy + 68;
        int btnSize = 24;

        for (int i = 0; i < labels.length; i++) {
            int ry = startY + i * rowH;

            // Fond de ligne alterné
            if (i % 2 == 0) {
                g2.setColor(new Color(255, 255, 255, 12));
                g2.fillRoundRect(cx + 10, ry + 2, cw - 20, rowH - 4, 6, 6);
            }

            // Label — gras et plus grand
            g2.setColor(new Color(200, 210, 230));
            g2.setFont(new Font("Arial", Font.BOLD, 15));
            g2.drawString(labels[i], cx + 22, ry + 24);

            // Bouton −
            int bx = cx + cw - 110;
            int by = ry + (rowH - btnSize) / 2;
            btnMoins[i] = new java.awt.Rectangle(bx, by, btnSize, btnSize);
            boolean canMinus = vals[i] > mins[i];
            g2.setColor(canMinus ? new Color(60, 80, 140) : new Color(40, 45, 70));
            g2.fillRoundRect(bx, by, btnSize, btnSize, 6, 6);
            g2.setColor(canMinus ? new Color(150, 180, 255) : new Color(80, 90, 110));
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.drawString("−", bx + 6, by + 18);

            // Valeur
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 15));
            String sv = String.valueOf(vals[i]);
            int svw = g2.getFontMetrics().stringWidth(sv);
            g2.drawString(sv, bx + btnSize + (38 - svw) / 2, by + 17);

            // Bouton +
            int px2 = bx + btnSize + 38;
            btnPlus[i] = new java.awt.Rectangle(px2, by, btnSize, btnSize);
            boolean canPlus = vals[i] < maxs[i];
            g2.setColor(canPlus ? new Color(60, 80, 140) : new Color(40, 45, 70));
            g2.fillRoundRect(px2, by, btnSize, btnSize, 6, 6);
            g2.setColor(canPlus ? new Color(150, 180, 255) : new Color(80, 90, 110));
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.drawString("+", px2 + 5, by + 18);
        }

        // Bouton LANCER
        int lw = 200, lh = 42;
        int lx = cx + (cw - lw) / 2;
        int ly = cy + ch - 58;
        btnLancer = new java.awt.Rectangle(lx, ly, lw, lh);
        g2.setColor(new Color(0, 160, 80));
        g2.fillRoundRect(lx, ly, lw, lh, 12, 12);
        g2.setColor(new Color(0, 220, 110, 80));
        g2.setStroke(new java.awt.BasicStroke(1.5f));
        g2.drawRoundRect(lx, ly, lw, lh, 12, 12);
        g2.setStroke(new java.awt.BasicStroke());
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 15));
        String lblancer = "▶  Lancer la simulation";
        int lbw = g2.getFontMetrics().stringWidth(lblancer);
        g2.drawString(lblancer, lx + (lw - lbw) / 2, ly + 27);
    }

    private void drawFlashes(Graphics2D g2, Environnement env) {
        int s = Configuration.BLOCK_SIZE;

        for (FlashInteraction flash : env.getFlashes()) {
            if (!flash.estActif()) continue;

            int x1 = flash.getPosition().getColumn()  * s + s / 2;
            int y1 = flash.getPosition().getLine()     * s + s / 2;
            int x2 = flash.getPosition2().getColumn()  * s + s / 2;
            int y2 = flash.getPosition2().getLine()    * s + s / 2;
            int mx = (x1 + x2) / 2;
            int my = (y1 + y2) / 2;

            if (flash.getType() == TypeInteraction.AMITIE
                    || flash.getType() == TypeInteraction.PARTAGE_NOURRITURE) {

                // Bloc coloré sur chaque souris (jaune = amitié, vert = partage)
                Color blocColor = flash.getType() == TypeInteraction.PARTAGE_NOURRITURE
                    ? new Color(80, 200, 120)   // vert pour partage
                    : new Color(255, 220, 50);   // jaune pour amitié
                Color texteColor = flash.getType() == TypeInteraction.PARTAGE_NOURRITURE
                    ? new Color(10, 80, 30)
                    : new Color(100, 60, 0);
                String symbole = flash.getType() == TypeInteraction.PARTAGE_NOURRITURE ? "D→R" : "ami";

                drawBulle(g2, flash.getPosition().getColumn()  * s,
                              flash.getPosition().getLine()    * s, s, symbole, blocColor, texteColor);
                drawBulle(g2, flash.getPosition2().getColumn() * s,
                              flash.getPosition2().getLine()   * s, s, symbole, blocColor, texteColor);

                // Ligne pointillée entre D et R
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.80f));
                g2.setColor(new Color(80, 160, 255));
                g2.setStroke(new java.awt.BasicStroke(2f,
                    java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND,
                    1f, new float[]{5f, 4f}, 0f));
                g2.drawLine(x1, y1, x2, y2);
                g2.setStroke(new java.awt.BasicStroke());

                // Nuage de communication au milieu
                drawNuageCommunication(g2, mx, my,
                    flash.getType() == TypeInteraction.PARTAGE_NOURRITURE);

            } else if (flash.getType() == TypeInteraction.REPRODUCTION) {

                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
                g2.setColor(new Color(220, 0, 80));
                g2.setStroke(new java.awt.BasicStroke(2.5f));
                g2.drawLine(x1, y1, x2, y2);
                g2.setStroke(new java.awt.BasicStroke());

                drawBulle(g2, flash.getPosition().getColumn()  * s,
                              flash.getPosition().getLine()    * s, s, "♥", FLASH_REPRODUCTION, TEXTE_REPRO);
                drawBulle(g2, flash.getPosition2().getColumn() * s,
                              flash.getPosition2().getLine()   * s, s, "♥", FLASH_REPRODUCTION, TEXTE_REPRO);

            } else if (flash.getType() == TypeInteraction.PIEGE) {
                // Nuage rouge "Oh ! Un piège !" sur la souris piégée
                drawNuagePiege(g2, x1, y1, flash.getMessageComm());
            }
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }

    // /** Dessine un nuage "Oh ! Un piège !" en rouge sur la souris */
    private void drawNuagePiege(Graphics2D g2, int cx, int cy, String msg) {
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.95f));
        int w = 56, h = 26;
        int nx = cx - w / 2, ny = cy - h - 18;

        // Corps du nuage rouge
        g2.setColor(new Color(255, 60, 60));
        g2.fillOval(nx,           ny,           w,       h);
        g2.fillOval(nx - 7,       ny + 5,       w / 2,   h / 2);
        g2.fillOval(nx + w - 10,  ny + 5,       w / 2,   h / 2);
        g2.fillOval(nx + 6,       ny - 8,       w / 2,   h / 2 + 3);
        g2.fillOval(nx + w / 2,   ny - 7,       w / 2,   h / 2 + 3);
        // Queue
        g2.fillOval(cx - 5, ny + h - 2, 7, 7);
        g2.fillOval(cx - 10, ny + h + 3, 5, 5);
        g2.fillOval(cx - 13, ny + h + 7, 4, 4);

        // Contour blanc
        g2.setColor(Color.WHITE);
        g2.setStroke(new java.awt.BasicStroke(1.5f));
        g2.drawOval(nx, ny, w, h);
        g2.setStroke(new java.awt.BasicStroke());

        // Texte
        g2.setFont(new Font("Arial", Font.BOLD, 8));
        g2.setColor(Color.WHITE);
        String txt = msg != null ? msg : "Piege !";
        int tw = g2.getFontMetrics().stringWidth(txt);
        g2.drawString(txt, cx - tw / 2, ny + h / 2 + 4);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    // /** Dessine un nuage de communication BD au point (cx, cy) */
    private void drawNuageCommunication(Graphics2D g2, int cx, int cy, boolean partage) {
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.92f));

        // Corps du nuage : plusieurs ovales superposés
        Color cloudColor = new Color(255, 255, 255);
        Color borderColor = new Color(100, 150, 255);

        int w = 38, h = 22;
        // Fond blanc
        g2.setColor(cloudColor);
        g2.fillOval(cx - w/2,     cy - h/2,     w,    h);
        g2.fillOval(cx - w/2 - 6, cy - h/2 + 4, w/2,  h/2);
        g2.fillOval(cx + w/2 - 8, cy - h/2 + 4, w/2,  h/2);
        g2.fillOval(cx - w/2 + 4, cy - h/2 - 7, w/2,  h/2 + 2);
        g2.fillOval(cx,            cy - h/2 - 6, w/2,  h/2 + 2);
        // Petites boules de queue
        g2.fillOval(cx - 6, cy + h/2 - 2, 7, 7);
        g2.fillOval(cx - 12, cy + h/2 + 3, 5, 5);
        g2.fillOval(cx - 16, cy + h/2 + 7, 4, 4);

        // Contour
        g2.setColor(borderColor);
        g2.setStroke(new java.awt.BasicStroke(1.3f));
        g2.drawOval(cx - w/2,     cy - h/2,     w,    h);
        g2.drawOval(cx - w/2 - 6, cy - h/2 + 4, w/2,  h/2);
        g2.drawOval(cx + w/2 - 8, cy - h/2 + 4, w/2,  h/2);
        g2.drawOval(cx - w/2 + 4, cy - h/2 - 7, w/2,  h/2 + 2);
        g2.drawOval(cx,            cy - h/2 - 6, w/2,  h/2 + 2);
        g2.setStroke(new java.awt.BasicStroke());

        // Texte dans le nuage
        g2.setFont(new Font("Arial", Font.BOLD, 9));
        String txt = partage ? "info !" : "amis !";
        g2.setColor(new Color(50, 80, 200));
        int tw = g2.getFontMetrics().stringWidth(txt);
        g2.drawString(txt, cx - tw / 2, cy + 4);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    private void drawBulle(Graphics2D g2, int cellX, int cellY, int s,
                           String symbole, Color bgColor, Color fgColor) {
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f));
        g2.setColor(bgColor);
        g2.fillRoundRect(cellX + 2, cellY + 2, s - 4, s - 4, 10, 10);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g2.setColor(fgColor);
        // Police adaptée : emoji pour ♥, texte normal pour les labels courts
        int fontSize = symbole.length() <= 3 ? 9 : 13;
        String fontName = (symbole.equals("♥")) ? "Segoe UI Emoji" : "Arial";
        g2.setFont(new Font(fontName, Font.BOLD, fontSize));
        int sw = g2.getFontMetrics().stringWidth(symbole);
        g2.drawString(symbole, cellX + (s - sw) / 2, cellY + s / 2 + 4);
    }

    private void drawEcranSadEnd(Graphics2D g2, Environnement env, int W, int H) {
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
        g2.setColor(new Color(10, 5, 5));
        g2.fillRect(0, 0, W, H);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        int cw=460, ch=240, cx=(W-cw)/2, cy=(H-ch)/2;
        g2.setColor(new Color(30, 10, 10));
        g2.fillRoundRect(cx, cy, cw, ch, 20, 20);
        g2.setColor(new Color(180, 40, 40));
        g2.setStroke(new java.awt.BasicStroke(2.5f));
        g2.drawRoundRect(cx, cy, cw, ch, 20, 20);
        g2.setStroke(new java.awt.BasicStroke());

        // Titre
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        g2.setColor(new Color(220, 60, 60));
        String t = "La colonie s'est eteinte...";
        g2.drawString(t, cx+(cw-g2.getFontMetrics().stringWidth(t))/2, cy+55);

        // Sous-titre
        g2.setFont(new Font("Arial", Font.PLAIN, 13));
        g2.setColor(new Color(200, 150, 150));
        String s1 = "Toutes les souris sont mortes sans trouver le tresor.";
        g2.drawString(s1, cx+(cw-g2.getFontMetrics().stringWidth(s1))/2, cy+90);

        // Stats
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.setColor(new Color(160, 120, 120));
        String s2 = "Tour " + env.getTime() + "  |  Generation " + env.getGeneration();
        g2.drawString(s2, cx+(cw-g2.getFontMetrics().stringWidth(s2))/2, cy+120);

        // Message poetique
        g2.setFont(new Font("Arial", Font.ITALIC, 12));
        g2.setColor(new Color(160, 100, 100));
        String msg = "Le tresor reste cache dans l'ombre, pour la prochaine generation...";
        g2.drawString(msg, cx+(cw-g2.getFontMetrics().stringWidth(msg))/2, cy+155);

        // Bouton reset avec 3 souris
        int bw=240, bh=38, bx=cx+(cw-bw)/2, by=cy+192;
        g2.setColor(new Color(120, 40, 40));
        g2.fillRoundRect(bx, by, bw, bh, 10, 10);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 13));
        String btn = "Nouvelle colonie (3 souris)";
        g2.drawString(btn, bx+(bw-g2.getFontMetrics().stringWidth(btn))/2, by+25);
        btnTresorManque = new java.awt.Rectangle(bx, by, bw, bh);
    }

    private void drawGameOver(Graphics2D g2) {
        int W = Configuration.GRID_WIDTH, H = Configuration.GRID_HEIGHT;
        Environnement env = (Environnement) manager;
        double score = env.getScoreActuel();
        String note = score >= 80 ? "S+" : score >= 65 ? "A" : score >= 50 ? "B"
                    : score >= 35 ? "C" : "D";
        Color noteColor = score >= 80 ? new Color(255, 215, 0)
                        : score >= 65 ? new Color(100, 220, 120)
                        : score >= 50 ? new Color(100, 180, 255)
                        : new Color(220, 120, 80);

        // Fond
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.80f));
        g2.setColor(new Color(5, 10, 30));
        g2.fillRect(0, 0, W, H);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        int cw = 480, ch = 270;
        int cx = (W - cw) / 2, cy = (H - ch) / 2;

        // Halo couleur note
        g2.setColor(new Color(noteColor.getRed(), noteColor.getGreen(), noteColor.getBlue(), 35));
        g2.fillOval(cx - 25, cy - 25, cw + 50, ch + 50);

        // Carte
        g2.setColor(new Color(12, 20, 55));
        g2.fillRoundRect(cx, cy, cw, ch, 22, 22);
        g2.setColor(noteColor);
        g2.setStroke(new java.awt.BasicStroke(2.5f));
        g2.drawRoundRect(cx, cy, cw, ch, 22, 22);
        g2.setStroke(new java.awt.BasicStroke());

        // Titre
        g2.setFont(new Font("Arial", Font.BOLD, 21));
        g2.setColor(noteColor);
        String titre = "Colonisation optimale atteinte !";
        g2.drawString(titre, cx + (cw - g2.getFontMetrics().stringWidth(titre)) / 2, cy + 42);

        // Note geante
        g2.setFont(new Font("Arial", Font.BOLD, 52));
        g2.setColor(noteColor);
        g2.drawString(note, cx + cw - 72, cy + 72);

        // Separateur
        g2.setColor(new Color(60, 80, 140));
        g2.fillRect(cx + 20, cy + 55, cw - 40, 1);

        // Barre de score
        int barW = cw - 80, barH = 14;
        int barX = cx + 40, barY = cy + 75;
        g2.setColor(new Color(30, 40, 80));
        g2.fillRoundRect(barX, barY, barW, barH, 7, 7);
        int fill = (int)(barW * Math.min(score, 100.0) / 100.0);
        g2.setColor(noteColor);
        g2.fillRoundRect(barX, barY, fill, barH, 7, 7);
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.setColor(Color.WHITE);
        String scoreStr = String.format("Score colonisation : %.1f / 100", Math.min(score, 100.0));
        g2.drawString(scoreStr, barX + barW / 2 - g2.getFontMetrics().stringWidth(scoreStr) / 2, barY + 11);

        // Stats
        g2.setFont(new Font("Arial", Font.PLAIN, 13));
        g2.setColor(new Color(190, 210, 245));
        String s1 = "Generation : " + env.getGeneration()
                  + "   |   Tours : " + env.getTime()
                  + "   |   Souris : " + env.getMice().size();
        g2.drawString(s1, cx + (cw - g2.getFontMetrics().stringWidth(s1)) / 2, cy + 115);

        // Message selon note
        g2.setFont(new Font("Arial", Font.ITALIC, 12));
        g2.setColor(new Color(140, 200, 180));
        String msg = score >= 80 ? "Colonisation parfaite — toutes les zones explorees !"
                   : score >= 65 ? "Excellente organisation — la colonie prospere."
                   : score >= 50 ? "Bonne colonisation — quelques zones inexplorees."
                   : score >= 35 ? "Colonisation partielle — la population a survecu."
                   : "La colonie n'a pas reussi a s'organiser.";
        int mw = g2.getFontMetrics().stringWidth(msg);
        g2.drawString(msg, cx + (cw - mw) / 2, cy + 155);

        // Bouton CLIQUABLE
        int bw = 220, bh = 40;
        int bx = cx + (cw - bw) / 2, by = cy + 215;
        btnTresorManque = new java.awt.Rectangle(bx, by, bw, bh);
        g2.setColor(new Color(0, 130, 70));
        g2.fillRoundRect(bx, by, bw, bh, 12, 12);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 13));
        String btnTxt = "Nouvelle simulation";
        g2.drawString(btnTxt, bx + (bw - g2.getFontMetrics().stringWidth(btnTxt)) / 2, by + 26);
    }

    private void drawBottomInfos(Graphics g) {
        int totalW = Configuration.GRID_WIDTH;
        // Barre collée juste sous la grille, hauteur fixe STATUS_HEIGHT
        int y = Configuration.GRID_HEIGHT;
        int h = Configuration.STATUS_HEIGHT;
        int gap = 4;
        int nbBoxes = 5;
        int w = (totalW - gap * (nbBoxes - 1)) / nbBoxes;

        Font oldFont = g.getFont();
        g.setFont(new Font("Arial", Font.BOLD, Math.max(10, Configuration.BLOCK_SIZE / 4)));

        boolean termine = (manager instanceof Environnement)
            && ((Environnement) manager).isSimTerminee();
        int gen = (manager instanceof Environnement) ? ((Environnement)manager).getGeneration() : 1;
        double score = (manager instanceof Environnement) ? ((Environnement)manager).getScoreActuel() : 0;
        String etat = termine ? "Terminé"
            : (paused ? "Gén." + gen + " — En pause"
                      : String.format("Gén.%d — Score : %.0f/100", gen, Math.min(score, 100.0)));

        String[] textes = {
            "Obstacles : " + manager.getObstacleCount(),
            "Temps : "     + manager.getTime(),
            "Souris : "    + manager.getMouseCount(),
            "Nourriture : "+ manager.getFoodCount(),
            "Etat : "      + etat
        };

        for (int i = 0; i < nbBoxes; i++) {
            int x = i * (w + gap);
            drawInfoBox(g, x, y, w, h, textes[i]);
        }

        g.setFont(oldFont);
    }

    private void drawInfoBox(Graphics g, int x, int y, int w, int h, String texte) {
        g.setColor(new Color(235, 170, 170));
        g.fillRect(x, y, w, h);
        g.setColor(Color.BLACK);
        g.drawRect(x, y, w, h);
        g.drawString(texte, x + 8, y + h / 2 + 5);
    }
    private void drawTresor(Graphics2D g2, Environnement env) {
        if (!env.isTresorActif() || env.getPositionTresor() == null) return;
        // Cacher le trésor après le tour 300 s'il n'a pas été trouvé
        if (env.getTime() > env.getTourFinTresor() && !env.isVictoireTresor()) return;
        int s = Configuration.BLOCK_SIZE;
        int x = env.getPositionTresor().getColumn() * s;
        int y = env.getPositionTresor().getLine()   * s;
        boolean scelle = env.isTresorScelle();

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        if (scelle) {
            g2.setColor(new Color(100, 90, 70));
            g2.fillRect(x, y, s, s);
            java.awt.Image img = imageCache.get("tresor");
            if (img != null) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
                g2.drawImage(img, x+2, y+2, s-4, s-4, null);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
            // Cadenas
            g2.setColor(new Color(220, 190, 60));
            g2.setStroke(new java.awt.BasicStroke(2f));
            int lx = x + s/2 - 5, ly = y + s/2 - 6;
            g2.drawArc(lx+1, ly-2, 8, 8, 0, 180);
            g2.setStroke(new java.awt.BasicStroke());
            g2.fillRoundRect(lx, ly+4, 10, 8, 3, 3);
            // Compte a rebours
            if (s >= 32) {
                g2.setFont(new Font("Arial", Font.BOLD, Math.max(7, s/6)));
                g2.setColor(new Color(255, 220, 100));
                String txt = "T-" + (env.getTourAccesTresor() - env.getTime());
                int tw = g2.getFontMetrics().stringWidth(txt);
                g2.drawString(txt, x + (s-tw)/2, y + s - 3);
            }
        } else {
            // Accessible
            g2.setColor(new Color(255, 230, 50));
            g2.fillRect(x, y, s, s);
            int pulse = (int)(Math.abs(Math.sin(System.currentTimeMillis()/350.0)) * 10);
            g2.setColor(new Color(255, 160, 0, 100));
            g2.fillOval(x-pulse, y-pulse, s+pulse*2, s+pulse*2);
            java.awt.Image img = imageCache.get("tresor");
            if (img != null) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                g2.drawImage(img, x+1, y+1, s-2, s-2, null);
            }
            if (s >= 30) {
                g2.setFont(new Font("Arial", Font.BOLD, Math.max(7, s/5)));
                g2.setColor(new Color(120, 60, 0));
                String lbl = "TRESOR";
                int lw = g2.getFontMetrics().stringWidth(lbl);
                g2.drawString(lbl, x+(s-lw)/2, y+s-2);
            }
        }
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }
    private void drawEcranVictoire(Graphics2D g2, Environnement env, int W, int H) {
        Souris g = env.getGagnante();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.80f));
        g2.setColor(new Color(30, 20, 5));
        g2.fillRect(0, 0, W, H);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        int cw = 480, ch = 280, cx = (W-cw)/2, cy = (H-ch)/2;
        g2.setColor(new Color(255, 200, 0, 50));
        g2.fillOval(W/2-200, H/2-150, 400, 300);
        g2.setColor(new Color(20, 15, 5));
        g2.fillRoundRect(cx, cy, cw, ch, 22, 22);
        g2.setColor(new Color(255, 200, 0));
        g2.setStroke(new java.awt.BasicStroke(3f));
        g2.drawRoundRect(cx, cy, cw, ch, 22, 22);
        g2.setStroke(new java.awt.BasicStroke());

        java.awt.Image tresor = imageCache.get("tresor");
        if (tresor != null)
            g2.drawImage(tresor, cx+cw/2-35, cy+10, 70, 70, null);

        g2.setFont(new Font("Arial", Font.BOLD, 24));
        g2.setColor(new Color(255, 220, 50));
        String t = "TRESOR TROUVE !";
        g2.drawString(t, cx+(cw-g2.getFontMetrics().stringWidth(t))/2, cy+100);

        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.setColor(new Color(200, 230, 255));
        String s1 = "Souris #" + g.getId() + "  |  " + g.getCaractere()
                  + "  |  " + g.getSexe() + "  |  " + g.getCouleur();
        g2.drawString(s1, cx+(cw-g2.getFontMetrics().stringWidth(s1))/2, cy+132);

        g2.setFont(new Font("Arial", Font.PLAIN, 11));
        g2.setColor(new Color(150, 255, 150));
        String cond = "Energie " + g.getEnergie() + "/100  |  Age " + g.getAge()
                    + "  |  Memoire " + g.getMemoire().getCasesNourritureConnues().size() + " cases";
        g2.drawString(cond, cx+(cw-g2.getFontMetrics().stringWidth(cond))/2, cy+155);

        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.setColor(new Color(180, 200, 180));
        String s2 = "Tour " + env.getTime() + "  |  " + env.getMice().size() + " survivantes";
        g2.drawString(s2, cx+(cw-g2.getFontMetrics().stringWidth(s2))/2, cy+177);

        g2.setFont(new Font("Arial", Font.ITALIC, 12));
        g2.setColor(new Color(255, 220, 100));
        String msg = "Elle restera dans les memoires de la colonie pour toujours !";
        g2.drawString(msg, cx+(cw-g2.getFontMetrics().stringWidth(msg))/2, cy+200);

        int bw=220, bh=40, bx=cx+(cw-bw)/2, by=cy+228;
        btnVictoire = new java.awt.Rectangle(bx, by, bw, bh);
        g2.setColor(new Color(180, 130, 0));
        g2.fillRoundRect(bx, by, bw, bh, 12, 12);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 13));
        String btn = "Nouvelle aventure";
        g2.drawString(btn, bx+(bw-g2.getFontMetrics().stringWidth(btn))/2, by+26);
    }
    private void drawEcranTresorManque(Graphics2D g2, Environnement env, int W, int H) {
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.78f));
        g2.setColor(new Color(20, 10, 5));
        g2.fillRect(0, 0, W, H);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        int cw=460, ch=240, cx=(W-cw)/2, cy=(H-ch)/2;
        g2.setColor(new Color(40, 30, 20));
        g2.fillRoundRect(cx, cy, cw, ch, 20, 20);
        g2.setColor(new Color(150, 100, 50));
        g2.setStroke(new java.awt.BasicStroke(2f));
        g2.drawRoundRect(cx, cy, cw, ch, 20, 20);
        g2.setStroke(new java.awt.BasicStroke());

        java.awt.Image img = imageCache.get("tresor");
        if (img != null) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.30f));
            g2.drawImage(img, cx+cw/2-30, cy+12, 60, 60, null);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.setColor(new Color(200, 150, 80));
        String t = "Le tresor est reste introuvable...";
        g2.drawString(t, cx+(cw-g2.getFontMetrics().stringWidth(t))/2, cy+90);

        g2.setFont(new Font("Arial", Font.PLAIN, 13));
        g2.setColor(new Color(180, 160, 130));
        String s1 = "La fenetre de recherche (tours 100-300) est ecoulee.";
        g2.drawString(s1, cx+(cw-g2.getFontMetrics().stringWidth(s1))/2, cy+120);

        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.setColor(new Color(150, 140, 120));
        String s2 = env.getTime() + " tours  |  " + env.getMice().size() + " souris survivantes";
        g2.drawString(s2, cx+(cw-g2.getFontMetrics().stringWidth(s2))/2, cy+148);

        g2.setFont(new Font("Arial", Font.ITALIC, 12));
        g2.setColor(new Color(180, 140, 80));
        String msg = "Le tresor disparait dans l'ombre... la legende vivra encore.";
        g2.drawString(msg, cx+(cw-g2.getFontMetrics().stringWidth(msg))/2, cy+175);

        int bw=200, bh=38, bx=cx+(cw-bw)/2, by=cy+192;
        btnTresorManque = new java.awt.Rectangle(bx, by, bw, bh);
        g2.setColor(new Color(100, 70, 30));
        g2.fillRoundRect(bx, by, bw, bh, 10, 10);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 13));
        String btn = "Nouvelle simulation";
        g2.drawString(btn, bx+(bw-g2.getFontMetrics().stringWidth(btn))/2, by+25);
    }

}