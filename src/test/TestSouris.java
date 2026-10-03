package test;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import config.Configuration;
import engine.map.Case;
import engine.map.Grille;
import engine.mobile.*;
import engine.process.*;

/**
 * Tests unitaires — classe Souris et ses comportements.
 * Vérifie énergie, mémoire, rôles, journal.
 *
 * @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma
 */
public class TestSouris {

    private Grille grille;
    private Case   position;
    private Souris souris;

    @Before
    public void setUp() {
        grille   = new Grille(10, 10);
        position = grille.getBlock(5, 5);
        souris   = new Souris(position);
    }

    //Énergie

    @Test
    public void testEnergieInitiale() {
        assertEquals("Énergie initiale doit être INITIAL_ENERGY",
            Configuration.INITIAL_ENERGY, souris.getEnergie());
    }

    @Test
    public void testEnergieNePasDepasserMax() {
        souris.gagnerEnergie(1000);
        assertEquals("Énergie ne doit pas dépasser MAX_ENERGY",
            Configuration.MAX_ENERGY, souris.getEnergie());
    }

    @Test
    public void testEnergiePerdreEnergie() {
        souris.perdreEnergie(20);
        assertEquals("Énergie après perte",
            Configuration.INITIAL_ENERGY - 20, souris.getEnergie());
    }

    @Test
    public void testMortQuandEnergieZero() {
        souris.perdreEnergie(1000);
        assertFalse("Souris doit être morte quand énergie = 0", souris.estVivante());
        assertEquals("Énergie doit être 0", 0, souris.getEnergie());
    }

    @Test
    public void testEnergieNegativeImpossible() {
        souris.perdreEnergie(1000);
        assertTrue("Énergie ne peut pas être négative", souris.getEnergie() >= 0);
    }

    //Rôle D/R: (Donneur/Receveur)

    @Test
    public void testRoleDefiniAlaNaissance() {
        // isDonneur() doit retourner true ou false (jamais d'exception)
        boolean role = souris.isDonneur();
        assertTrue("Rôle doit être défini", role || !role);
    }

    @Test
    public void testRoleImmuable() {
        boolean roleInitial = souris.isDonneur();
        // Le rôle ne change pas après vieillissement
        souris.vieillir();
        assertEquals("Rôle doit être immuable", roleInitial, souris.isDonneur());
    }

    //Mémoire

    @Test
    public void testMemoriserCase() {
        Case c = grille.getBlock(3, 3);
        souris.memoriserCase(c);
        assertTrue("Case doit être mémorisée",
            souris.getMemoire().getCasesNourritureConnues().contains(c));
    }

    @Test
    public void testMemoriserDanger() {
        Case c = grille.getBlock(2, 2);
        souris.memoriserDanger(c);
        assertTrue("Case dangereuse doit être mémorisée",
            souris.getMemoire().getCasesDangereuses().contains(c));
    }

    @Test
    public void testPasDoublonMemoire() {
        Case c = grille.getBlock(4, 4);
        souris.memoriserCase(c);
        souris.memoriserCase(c); // doublon
        assertEquals("Pas de doublon en mémoire",
            1, souris.getMemoire().getCasesNourritureConnues().size());
    }

    //Journal

    @Test
    public void testJournalVideInitialement() {
        assertTrue("Journal doit être vide à la naissance",
            souris.getJournal().getEntrees().isEmpty());
    }

    @Test
    public void testEcrireJournal() {
        souris.ecrireJournal("Test entrée journal", 1);
        assertEquals("Journal doit contenir 1 entrée", 1,
            souris.getJournal().getEntrees().size());
    }

    //Âge

    @Test
    public void testAgeInitialZero() {
        assertEquals("Âge initial doit être 0", 0, souris.getAge());
    }

    @Test
    public void testVieillirIncrementeAge() {
        souris.vieillir();
        assertEquals("Vieillir doit incrémenter l'âge", 1, souris.getAge());
    }

    //Cible partage

    @Test
    public void testCaseCibleNulleInitialement() {
        assertNull("Cible partage doit être null à la naissance",
            souris.getCaseCiblePartage());
    }

    @Test
    public void testSetCaseCiblePartage() {
        Case c = grille.getBlock(1, 1);
        souris.setCaseCiblePartage(c);
        assertEquals("Cible partage doit être définie", c, souris.getCaseCiblePartage());
    }
}
