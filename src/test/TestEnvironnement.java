package test;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import config.Configuration;
import engine.map.*;
import engine.mobile.*;
import engine.process.*;

/**
 * Tests unitaires — moteur de simulation (Environnement).
 * Vérifie population, nourriture, obstacles, boucle de simulation.
 *
 * @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma
 */
public class TestEnvironnement {

    private Grille       grille;
    private Environnement env;

    @Before
    public void setUp() {
        Configuration.appliquer(15, 10, 5, 4, 3, 20);
        grille = new Grille(Configuration.LINE_COUNT, Configuration.COLUMN_COUNT);
        env    = new Environnement(grille);
    }

    //Initialisation

    @Test
    public void testInitialisationPopulation() {
        assertEquals("Population initiale doit correspondre à INITIAL_MOUSE_COUNT",
            Configuration.INITIAL_MOUSE_COUNT, env.getMouseCount());
    }

    @Test
    public void testInitialisationNourriture() {
        assertEquals("Nourriture initiale doit correspondre à INITIAL_FOOD_COUNT",
            Configuration.INITIAL_FOOD_COUNT, env.getFoodCount());
    }

    @Test
    public void testInitialisationObstacles() {
        assertEquals("Obstacles initiaux doivent correspondre à INITIAL_OBSTACLE_COUNT",
            Configuration.INITIAL_OBSTACLE_COUNT, env.getObstacleCount());
    }

    @Test
    public void testSimulationPasTermineeAuDebut() {
        assertFalse("Simulation ne doit pas être terminée au démarrage",
            env.isSimTerminee());
    }

    //Grille

    @Test
    public void testGrilleDimensions() {
        assertEquals("Nombre de lignes doit correspondre",
            Configuration.LINE_COUNT, grille.getLineCount());
        assertEquals("Nombre de colonnes doit correspondre",
            Configuration.COLUMN_COUNT, grille.getColumnCount());
    }

    @Test
    public void testGrilleAccesBlocs() {
        assertNotNull("Bloc (0,0) ne doit pas être null", grille.getBlock(0, 0));
        assertNotNull("Bloc (max-1, max-1) ne doit pas être null",
            grille.getBlock(Configuration.LINE_COUNT - 1, Configuration.COLUMN_COUNT - 1));
    }

    //Ajout dynamique

    @Test
    public void testAjoutSouris() {
        int avant = env.getMouseCount();
        env.addMouse();
        assertEquals("Ajout d'une souris doit augmenter la population",
            avant + 1, env.getMouseCount());
    }

    @Test
    public void testAjoutNourriture() {
        int avant = env.getFoodCount();
        env.addFood();
        assertEquals("Ajout de nourriture doit augmenter le compteur",
            avant + 1, env.getFoodCount());
    }

    @Test
    public void testAjoutObstacle() {
        int avant = env.getObstacleCount();
        env.addObstacle();
        assertEquals("Ajout d'un obstacle doit augmenter le compteur",
            avant + 1, env.getObstacleCount());
    }

    //Boucle nextRound

    @Test
    public void testTempsIncrementeApresNextRound() {
        int avant = env.getTime();
        env.nextRound();
        assertEquals("Temps doit s'incrémenter après nextRound",
            avant + 1, env.getTime());
    }

    @Test
    public void testPopulationNeDeplassePasMax() {
        // Verifier que la population initiale respecte MAX_POPULATION
        assertTrue("Population initiale ne doit pas depasser MAX_POPULATION",
            env.getMouseCount() <= Configuration.MAX_POPULATION);
        // Apres nextRound, la population doit toujours etre dans les limites
        for (int i = 0; i < 5; i++) {
            if (!env.isSimTerminee()) env.nextRound();
        }
        assertTrue("Population apres simulation ne doit pas depasser MAX_POPULATION",
            env.getMouseCount() <= Configuration.MAX_POPULATION);
    }

    @Test
    public void testResetRemetAZero() {
        env.nextRound();
        env.nextRound();
        env.reset();
        assertEquals("Reset doit remettre le temps à 0", 0, env.getTime());
        assertFalse("Reset ne doit pas terminer la simulation",
            env.isSimTerminee());
    }

    //Performance

    @Test
    public void testPerformance100Tours() {
        long debut = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            if (!env.isSimTerminee()) env.nextRound();
        }
        long duree = System.currentTimeMillis() - debut;
        assertTrue("100 tours doivent s'exécuter en moins de 5 secondes", duree < 5000);
    }

    @Test
    public void testRobustesseSansNourriture() {
        // Simulation sans nourriture ne doit pas planter
        Configuration.appliquer(10, 10, 3, 0, 0, 20);
        Grille g2 = new Grille(10, 10);
        Environnement env2 = new Environnement(g2);
        try {
            for (int i = 0; i < 20; i++) {
                if (!env2.isSimTerminee()) env2.nextRound();
            }
        } catch (Exception e) {
            fail("Simulation sans nourriture ne doit pas lever d'exception : " + e.getMessage());
        }
    }

    @Test
    public void testRobustesseSansSouris() {
        // Simulation sans souris : le moteur ne plante pas et renvoie 0 souris
        Configuration.appliquer(10, 10, 0, 3, 0, 20);
        Grille g2 = new Grille(10, 10);
        Environnement env2 = new Environnement(g2);
        assertEquals("Simulation sans souris doit avoir 0 souris",
            0, env2.getMouseCount());
        // Le moteur doit gérer ce cas sans exception
        try {
            env2.nextRound();
        } catch (Exception e) {
            fail("Simulation sans souris ne doit pas lever d'exception : " + e.getMessage());
        }
    }
}
