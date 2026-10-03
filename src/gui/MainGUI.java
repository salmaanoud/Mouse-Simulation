package gui;

import java.awt.*;
import javax.swing.*;

import config.Configuration;
import engine.map.Grille;
import engine.mobile.Sexe;
import engine.process.Environnement;
import engine.process.GameBuilder;

/**
 * Fenêtre principale — stats à gauche, grille au centre, boutons à droite. @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma /
 *
 * @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma
 */
public class MainGUI extends JFrame implements Runnable {

    private static final long serialVersionUID = 1L;

    private Grille          map;
    private Environnement   manager;
    private GameDisplay     dashboard;
    private StatsPanel      statsPanel;
    private JPanel          controlPanel;
    private JournalFenetre  journalFenetre;

    private volatile boolean running = false;
    private volatile boolean paused  = true;
    private int currentSpeed = Configuration.GAME_SPEED;

    public MainGUI(String title) {
        super(title);
        init();
    }

    private void init() {
        // Calculer les dimensions dès le départ avec les valeurs par défaut
        Configuration.appliquer(
            Configuration.COLUMN_COUNT, Configuration.LINE_COUNT,
            Configuration.INITIAL_MOUSE_COUNT, Configuration.INITIAL_FOOD_COUNT,
            Configuration.INITIAL_OBSTACLE_COUNT, Configuration.MAX_POPULATION);

        map     = GameBuilder.buildMap();
        manager = (Environnement) GameBuilder.buildInitMobile(map);

        dashboard      = new GameDisplay(map, manager);
        statsPanel     = new StatsPanel(manager.getStats());
        journalFenetre = JournalFenetre.getInstance(this);
        controlPanel   = buildControlPanel();

        // Clic sur une souris → journal
        dashboard.setOnSourisClicked(souris -> journalFenetre.afficher(souris));

        // Callback quand l'utilisateur clique "Lancer" dans l'overlay
        dashboard.setOnLancer(() -> {
            // Reconstruire le monde avec les nouveaux paramètres
            map     = GameBuilder.buildMap();
            manager = (Environnement) GameBuilder.buildInitMobile(map);
            dashboard.updateManager(map, manager);
            journalFenetre.reset();

            Container cp = getContentPane();
            cp.remove(statsPanel);
            statsPanel = new StatsPanel(manager.getStats());
            cp.add(statsPanel, BorderLayout.WEST);

            // Adapter la taille du dashboard à la grille recalculée
            updateDashboardSize();

            paused = true;
            dashboard.setPaused(true);

            pack();
            setLocationRelativeTo(null);
            revalidate();
            repaint();
        });

        Container cp = getContentPane();
        cp.setLayout(new BorderLayout(0, 0));
        cp.add(dashboard,    BorderLayout.CENTER);
        cp.add(controlPanel, BorderLayout.EAST);
        cp.add(statsPanel,   BorderLayout.WEST);

        // Taille initiale avant que l'utilisateur configure
        updateDashboardSize();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);   // fenêtre fixe — pas de redimensionnement
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // /** Met à jour la taille du dashboard en fonction de GRID_WIDTH/HEIGHT courants. */
    private void updateDashboardSize() {
        dashboard.setPreferredSize(new Dimension(
            Configuration.GRID_WIDTH,
            Configuration.GRID_HEIGHT + Configuration.STATUS_HEIGHT));
    }

    private JPanel buildControlPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.setLayout(new GridLayout(10, 1, 6, 6));
        panel.setPreferredSize(new Dimension(Configuration.CONTROL_WIDTH, 0));

        JButton startBtn   = btn("Démarrer",     new Color(0, 160, 80));
        JButton pauseBtn   = btn("Pause",         new Color(180, 40, 40));
        JButton resetBtn   = btn("Réinitialiser", new Color(100, 100, 120));
        JButton fasterBtn  = btn("Vitesse +",    new Color(60, 130, 200));
        JButton slowerBtn  = btn("Vitesse −",    new Color(60, 130, 200));
        JButton addMiceBtn = btn("+ Souris",         new Color(0, 140, 70));
        JButton addMaleBtn = btn("+ Mâle ♂",         new Color(50, 100, 180));
        JButton addFemBtn  = btn("+ Femelle ♀",       new Color(180, 60, 130));
        JButton addFoodBtn = btn("+ Nourriture",      new Color(180, 140, 0));
        JButton addObsBtn  = btn("+ Obstacle",        new Color(150, 90, 30));

        startBtn.addActionListener(e -> {
            running = true; paused = false;
            dashboard.setPaused(false); dashboard.repaint();
        });
        pauseBtn.addActionListener(e -> {
            paused = true; dashboard.setPaused(true); dashboard.repaint();
        });
        resetBtn.addActionListener(e -> {
            paused = true;
            manager.reset();
            journalFenetre.reset();
            dashboard.setPaused(true);
            dashboard.repaint();
            statsPanel.repaint();
        });
        fasterBtn.addActionListener(e -> { currentSpeed = Math.max(50,   currentSpeed - 50); });
        slowerBtn.addActionListener(e -> { currentSpeed = Math.min(2000,  currentSpeed + 50); });
        addMiceBtn.addActionListener(e -> { manager.addMouse();             dashboard.repaint(); });
        addMaleBtn.addActionListener(e -> { manager.addMouse(Sexe.MALE);    dashboard.repaint(); });
        addFemBtn.addActionListener(e  -> { manager.addMouse(Sexe.FEMELLE); dashboard.repaint(); });
        addFoodBtn.addActionListener(e -> { manager.addFood();              dashboard.repaint(); });
        addObsBtn.addActionListener(e  -> { manager.addObstacle();          dashboard.repaint(); });

        panel.add(startBtn); panel.add(pauseBtn);   panel.add(resetBtn);
        panel.add(fasterBtn); panel.add(slowerBtn);
        panel.add(addMiceBtn); panel.add(addMaleBtn); panel.add(addFemBtn);
        panel.add(addFoodBtn); panel.add(addObsBtn);
        return panel;
    }

    private JButton btn(String txt, Color bg) {
        JButton b = new JButton(txt);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Arial", Font.BOLD, 15));
        b.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        return b;
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            try { Thread.sleep(currentSpeed); }
            catch (InterruptedException e) { e.printStackTrace(); }

            if (!paused) {
                if (manager.isSimTerminee()) {
                    paused = true;
                    SwingUtilities.invokeLater(() -> dashboard.repaint());
                    continue;
                }
                manager.nextRound();
                SwingUtilities.invokeLater(() -> {
                    dashboard.repaint();
                    statsPanel.repaint();
                    if (journalFenetre.isVisible()) journalFenetre.rafraichir();
                });
            }
        }
    }
}
