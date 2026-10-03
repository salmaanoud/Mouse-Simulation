package test;

import javax.swing.SwingUtilities;
import log.LoggerUtility;
import org.apache.log4j.Logger;
import gui.MainGUI;

/**
 * Point d'entrée de la simulation.
 * Configure log4j et lance l'interface graphique.
 *
 * @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma
 */
public class TestGame {

    private static final Logger logger = LoggerUtility.getLogger(TestGame.class, "html");

    public static void main(String[] args) {
        // Fix netteté Windows DPI scaling
        System.setProperty("sun.java2d.noddraw", "true");
        System.setProperty("sun.java2d.dpiaware", "true");
        System.setProperty("sun.java2d.uiScale.enabled", "false");
        System.setProperty("awt.useSystemAAFontSettings", "gasp");
        System.setProperty("swing.aatext", "true");
        System.setProperty("sun.java2d.xrender", "false");

        logger.info("=== Démarrage Simulation Souris ===");

        SwingUtilities.invokeLater(() -> {
            MainGUI gui = new MainGUI("Simulation de Souris — GLP L2");
            Thread t = new Thread(gui);
            t.setDaemon(true);
            t.start();
            logger.info("Interface graphique lancée");
        });
    }
}
