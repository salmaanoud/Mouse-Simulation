package test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Suite de tests — regroupe tous les tests JUnit du projet.
 * Lancer cette classe pour exécuter tous les tests en une fois.
 *
 * Catégories couvertes :
 * - Tests unitaires : Souris, Interactions, Environnement
 * - Tests de robustesse : dans TestEnvironnement
 * - Tests de performance : dans TestEnvironnement
 *
 * @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    TestSouris.class,
    TestInteractions.class,
    TestEnvironnement.class
})
public class TestSuiteAll {
    // Classe vide — la suite est définie par les annotations
}
