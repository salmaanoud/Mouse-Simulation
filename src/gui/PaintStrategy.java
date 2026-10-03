package gui;

/**
 * Stratégie de rendu graphique des éléments de la simulation.
 * PaintStrategy est responsable du dessin de tous les éléments
 * visibles sur la grille : cases du damier, obstacles (rocher), sources
 * de nourriture (fromage) et souris (avec leur sprite selon le sexe,
 * la couleur de robe, la direction de déplacement et l'état souriceau).
 * Les sprites sont chargés depuis src/resources/ au démarrage
 * et mis en cache pour optimiser les performances. Chaque souris dispose
 * de 4 sprites directionnels (UP, DOWN, LEFT, RIGHT) selon son sexe
 * et sa engine.mobile.CouleurRobe. Les souriceaux utilisent
 * une version réduite du sprite mâle.
 * Un système de fallback (formes géométriques colorées) est prévu si
 * une image ne peut pas être chargée.
 *
 * @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma
 */

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

import config.Configuration;
import engine.map.Case;
import engine.map.Grille;
import engine.mobile.*;

public class PaintStrategy {

    private static final Color CELL_A = new Color(245, 240, 220);
    private static final Color CELL_B = new Color(225, 218, 195);

    private Map<String, Image> cache = new HashMap<>();

    public PaintStrategy() {
        int s  = Configuration.BLOCK_SIZE;
        int sb = 22; // taille souriceau

        String[] couleurs  = {"gris", "blanc", "marron"};
        String[] sexes     = {"male", "femelle"};
        String[] dirs      = {"up", "down", "left", "right"};

        // 24 sprites adultes
        for (String c : couleurs)
            for (String sx : sexes)
                for (String d : dirs)
                    load("souris_" + c + "_" + sx + "_" + d,
                         "resources/souris_" + c + "_" + sx + "_" + d + ".png", s, s);

        // 3 souriceaux (couleur héritée, taille réduite)
        for (String c : couleurs)
            load("souriceau_" + c, "resources/souriceau_" + c + ".png", sb, sb);

        // Nourriture : fromage (image existante) + pomme + graines
        load("nourriture", "resources/nourriture.png",         s, s);
        load("nourriture_fromage", "resources/nourriture.png",         s, s);
        load("nourriture_pomme", "resources/nourriture_pomme.png",   s, s);
        load("nourriture_graines", "resources/nourriture_graines.png", s, s);
        // Obstacle : existant + buche + riviere
        load("obstacle", "resources/obstacle.png",          s, s);
        load("obstacle_rocher", "resources/obstacle.png",          s, s);
        load("obstacle_tronc", "resources/obstacle.png",          s, s);
        load("obstacle_lac", "resources/obstacle.png",          s, s);
        load("obstacle_buche", "resources/obstacle_buche.png",    s, s);
        load("obstacle_riviere", "resources/obstacle_riviere.png",  s, s);
    }

    private void load(String key, String path, int w, int h) {
        try {
            InputStream is = PaintStrategy.class.getClassLoader().getResourceAsStream(path);
            if (is != null) {
                BufferedImage img = ImageIO.read(is);
                cache.put(key, img.getScaledInstance(w, h, Image.SCALE_SMOOTH));
            } else {
                System.err.println("Image non trouvée : " + path);
            }
        } catch (IOException e) {
            System.err.println("Erreur : " + path);
        }
    }

    //Grille

    public void paint(Grille map, Graphics g) {
        int s = Configuration.BLOCK_SIZE;
        Case[][] blocks = map.getBlocks();
        for (int i = 0; i < map.getLineCount(); i++)
            for (int j = 0; j < map.getColumnCount(); j++) {
                Case b = blocks[i][j];
                g.setColor(((i + j) % 2 == 0) ? CELL_A : CELL_B);
                g.fillRect(b.getColumn() * s, b.getLine() * s, s, s);
            }
    }

    //Obstacle

    public void paint(Obstacle o, Graphics g) {
        Case p = o.getPosition();
        int s = Configuration.BLOCK_SIZE;
        int x = p.getColumn() * s, y = p.getLine() * s;
        String key = "obstacle_" + o.getType().name().toLowerCase();
        Image img = cache.get(key);
        if (img == null) img = cache.get("obstacle");
        if (img != null) g.drawImage(img, x, y, s, s, null);
        else { g.setColor(new Color(90, 80, 70)); g.fillRect(x+4, y+4, s-8, s-8); }
    }

    //Nourriture

    public void paint(Nourriture f, Graphics g) {
        Case p = f.getPosition();
        int s = Configuration.BLOCK_SIZE;
        int x = p.getColumn() * s, y = p.getLine() * s;
        String key = "nourriture_" + f.getType().name().toLowerCase();
        Image img = cache.get(key);
        if (img == null) img = cache.get("nourriture");
        if (img != null) g.drawImage(img, x+2, y+2, s-4, s-4, null);
        else { g.setColor(new Color(255, 210, 40)); g.fillOval(x+s/4, y+s/4, s/2, s/2); }
    }

    //Souris

    public void paint(Souris mouse, Graphics g) {
        Case p = mouse.getPosition();
        int s  = Configuration.BLOCK_SIZE;
        int x  = p.getColumn() * s, y = p.getLine() * s;
        String couleur = mouse.getCouleur().name().toLowerCase();
        String dir     = mouse.getDirection().name().toLowerCase();

        if (mouse.isSouriceau()) {
            Image img = cache.get("souriceau_" + couleur);
            int sb = 22;
            if (img != null)
                g.drawImage(img, x + (s - sb) / 2, y + (s - sb) / 2, sb, sb, null);
            else {
                g.setColor(Color.LIGHT_GRAY);
                g.fillOval(x + s/4, y + s/4, s/2, s/2);
            }
        } else {
            String sexe = mouse.getSexe() == Sexe.MALE ? "male" : "femelle";
            Image img = cache.get("souris_" + couleur + "_" + sexe + "_" + dir);
            if (img != null) g.drawImage(img, x, y, s, s, null);
            else {
                g.setColor(mouse.getSexe() == Sexe.MALE
                    ? new Color(110, 110, 120) : new Color(230, 160, 175));
                g.fillOval(x+5, y+5, s-10, s-10);
            }
        }

        // Énergie — police adaptive + fond blanc semi-transparent pour lisibilité
        int fontSize = Math.max(9, Configuration.BLOCK_SIZE / 4);
        String energieStr = String.valueOf(mouse.getEnergie());
        g.setFont(new Font("Arial", Font.BOLD, fontSize));
        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(energieStr);
        int th = fm.getAscent();
        // Petit fond blanc derrière le chiffre
        g.setColor(new Color(255, 255, 255, 180));
        g.fillRoundRect(x + 1, y + 1, tw + 3, th + 1, 3, 3);
        // Texte en noir pour contraste maximal
        g.setColor(new Color(0, 0, 0, 220));
        g.drawString(energieStr, x + 2, y + th);

        // Rôle D/R
        g.setFont(new Font("Arial", Font.BOLD, Math.max(9, Configuration.BLOCK_SIZE / 4)));
        if (mouse.isDonneur()) {
            g.setColor(new Color(0, 170, 0));
            g.drawString("D", x + 2, y + s - 2);
        } else {
            g.setColor(new Color(180, 0, 200));
            g.drawString("R", x + 2, y + s - 2);
        }
    }
}
