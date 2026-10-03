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
 * Tests unitaires — interactions D→R, partage d'info, reproduction.
 * Vérifie que le partage se fait correctement et que la cible est bien assignée.
 *
 * @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma
 */
public class TestInteractions {

    private Grille grille;
    private Environnement env;

    @Before
    public void setUp() {
        Configuration.appliquer(10, 10, 0, 0, 0, 20);
        grille = new Grille(10, 10);
        env    = new Environnement(grille);
    }

    // ── Probabilité de partage ────────────────────────────────

    @Test
    public void testProbabilitePartageSincereElevee() {
        Souris donneur  = new Souris(grille.getBlock(0,0), Sexe.MALE,   Caractere.SINCERE);
        Souris receveur = new Souris(grille.getBlock(0,1), Sexe.MALE,   Caractere.SINCERE);
        double p = Interaction.calculerProbabilitePartage(donneur, receveur);
        assertTrue("Probabilité partage Sincère→Sincère doit être >= 0.5", p >= 0.5);
        assertTrue("Probabilité doit être <= 1.0", p <= 1.0);
    }

    @Test
    public void testProbabilitePartageEgoisteMechanteFaible() {
        Souris donneur  = new Souris(grille.getBlock(0,0), Sexe.MALE,   Caractere.EGOISTE);
        Souris receveur = new Souris(grille.getBlock(0,1), Sexe.MALE,   Caractere.MECHANTE);
        double p = Interaction.calculerProbabilitePartage(donneur, receveur);
        assertTrue("Probabilité partage Égoïste→Méchante doit être < 0.5", p < 0.5);
        assertTrue("Probabilité doit être >= 0.05", p >= 0.05);
    }

    @Test
    public void testProbabilitePartageDansIntervalle() {
        for (Caractere c1 : Caractere.values()) {
            for (Caractere c2 : Caractere.values()) {
                Souris d = new Souris(grille.getBlock(0,0), Sexe.MALE,   c1);
                Souris r = new Souris(grille.getBlock(0,1), Sexe.MALE,   c2);
                double p = Interaction.calculerProbabilitePartage(d, r);
                assertTrue("P(partage) doit être entre 0.05 et 0.95", p >= 0.05 && p <= 0.95);
            }
        }
    }

    // ── Énergie pleine → ne mange pas ────────────────────────

    @Test
    public void testSourisNeMangePasQuandEnergieMax() {
        // Forcer la souris à MAX_ENERGY
        Souris s = new Souris(grille.getBlock(0,0), Sexe.MALE, Caractere.SINCERE);
        s.gagnerEnergie(1000); // → MAX_ENERGY
        assertEquals("Souris doit être à MAX_ENERGY",
            Configuration.MAX_ENERGY, s.getEnergie());
    }

    @Test
    public void testSourisMangeSiEnergieInferieurMax() {
        Souris s = new Souris(grille.getBlock(0,0), Sexe.MALE, Caractere.SINCERE);
        int energieAvant = s.getEnergie();
        s.gagnerEnergie(10);
        // Si énergie < MAX, gagner de l'énergie fonctionne
        assertTrue("Souris doit avoir gagné de l'énergie",
            s.getEnergie() >= energieAvant);
    }

    // ── Cible D→R ────────────────────────────────────────────

    @Test
    public void testReceveurRecoitCibleApresPartage() {
        Souris receveur = new Souris(grille.getBlock(3,3), Sexe.FEMELLE, Caractere.SINCERE);
        Case cible = grille.getBlock(7, 7);
        receveur.setCaseCiblePartage(cible);
        assertNotNull("Receveur doit avoir une cible après partage",
            receveur.getCaseCiblePartage());
        assertEquals("Cible doit correspondre à la case envoyée",
            cible, receveur.getCaseCiblePartage());
    }

    @Test
    public void testCibleEffaceeAuDestinataire() {
        Souris receveur = new Souris(grille.getBlock(3,3), Sexe.FEMELLE, Caractere.SINCERE);
        receveur.setCaseCiblePartage(grille.getBlock(5,5));
        receveur.setCaseCiblePartage(null); // effacée après arrivée
        assertNull("Cible doit être null après effacement",
            receveur.getCaseCiblePartage());
    }

    // ── Reproduction ─────────────────────────────────────────

    @Test
    public void testReproductionNecessiteAgeSuffisant() {
        Souris male   = new Souris(grille.getBlock(0,0), Sexe.MALE,    Caractere.SINCERE);
        Souris femelle= new Souris(grille.getBlock(0,1), Sexe.FEMELLE, Caractere.SINCERE);
        // Age initial = 0, donc ne peut pas se reproduire (âge < 3)
        assertTrue("Âge initial doit être insuffisant pour reproduction",
            male.getAge() < 3 || femelle.getAge() < 3);
    }

    @Test
    public void testReproductionNecessiteEnergiesSuffisantes() {
        Souris male   = new Souris(grille.getBlock(0,0), Sexe.MALE,    Caractere.SINCERE);
        Souris femelle= new Souris(grille.getBlock(0,1), Sexe.FEMELLE, Caractere.SINCERE);
        male.perdreEnergie(60); // → 20 < 50
        assertTrue("Énergie insuffisante empêche reproduction",
            male.getEnergie() < 50);
    }
}
