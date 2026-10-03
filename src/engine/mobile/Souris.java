package engine.mobile;

import config.Configuration;
import engine.map.Case;

/**
 * Représente un agent autonome (souris) évoluant dans la grille de simulation. Une souris est l'entité centrale de la simulation. Elle possède un état interne complet : sexe, âge, énergie, caractère, couleur de robe, rôle (DONNEUR ou RECEVEUR), direction de déplacement, mémoire des expériences passées et journal intime. Cycle de vie Naît avec config.Configuration#INITIAL_ENERGY points d'énergie. Vieillit d'un tour à chaque appel de #vieillir(). Perd de l'énergie par la faim et les obstacles ; en gagne en mangeant. Naît en tant que souriceau (#isSouriceau() = true) et devient adulte après 5 tours. Meurt quand son énergie atteint 0 (Etat#MORTE). Rôle DONNEUR / RECEVEUR Chaque souris se voit attribuer un rôle fixe à sa création : DONNEUR (#isDonneur() = true) : peut partager des informations sur la nourriture lors d'une rencontre avec un RECEVEUR de même sexe. La fiabilité de l'information dépend de son Caractere. RECEVEUR : reçoit les informations du DONNEUR. Sa réceptivité dépend de son caractère et de son niveau de confiance. Interactions Même sexe, D+R : échange d'information (une seule fois dans la simulation). Même sexe, D+D ou R+R : aucune interaction. Sexes opposés : reproduction possible (quel que soit le rôle D/R). @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma /
 *
 * @author SIMYA Meriem, LAIFAOUI Ines, ANOUD Salma
 */
public class Souris extends Element {

    private Sexe sexe;
    private int age;
    private int energie;
    private Caractere caractere;
    private Etat etat;
    private Memoire memoire;
    private Journal journal;
    private boolean donneur;
    private CouleurRobe couleur;
    private boolean souriceau; // true si c'est un bébé (age < 5)

    // Direction de déplacement pour le sprite
    public enum Direction { LEFT, RIGHT, UP, DOWN }
    private Direction direction = Direction.RIGHT;

    // Case cible nourriture vers laquelle la souris se dirige après avoir reçu une info de partage (vraie ou fausse). Null si pas de destination particulière. /
    private Case caseCibleNourriture = null;
    // /** Case cible reçue par partage D→R — suivie aveuglément (vraie ou fausse). */
    private Case caseCiblePartage = null;

    public Souris(Case position) {
        super(position);
        this.sexe      = Math.random() < 0.5 ? Sexe.MALE : Sexe.FEMELLE;
        this.caractere = Caractere.values()[(int)(Math.random() * Caractere.values().length)];
        this.couleur   = CouleurRobe.values()[(int)(Math.random() * CouleurRobe.values().length)];
        this.age       = 0;
        this.energie   = Configuration.INITIAL_ENERGY;
        this.etat      = Etat.VIVANTE;
        this.memoire   = new Memoire();
        this.journal   = new Journal();
        this.donneur   = Math.random() < 0.5;
        this.souriceau = false;
    }

    public Souris(Case position, Sexe sexe, Caractere caractere) {
        super(position);
        this.sexe      = sexe;
        this.caractere = caractere;
        this.couleur   = CouleurRobe.values()[(int)(Math.random() * CouleurRobe.values().length)];
        this.age       = 0;
        this.energie   = Configuration.INITIAL_ENERGY;
        this.etat      = Etat.VIVANTE;
        this.memoire   = new Memoire();
        this.journal   = new Journal();
        this.donneur   = Math.random() < 0.5;
        this.souriceau = false;
    }

    public Souris(Case position, Sexe sexe, Caractere caractere, CouleurRobe couleur) {
        super(position);
        this.sexe      = sexe;
        this.caractere = caractere;
        this.couleur   = couleur;
        this.age       = 0;
        this.energie   = Configuration.INITIAL_ENERGY;
        this.etat      = Etat.VIVANTE;
        this.memoire   = new Memoire();
        this.journal   = new Journal();
        this.donneur   = Math.random() < 0.5;
        this.souriceau = true; // naît souriceau
    }

    public Sexe getSexe()              { return sexe; }
    public int getAge()                { return age; }
    public int getEnergie()            { return energie; }
    public Caractere getCaractere()    { return caractere; }
    public Etat getEtat()              { return etat; }
    public Memoire getMemoire()        { return memoire; }
    public Journal getJournal()        { return journal; }
    public boolean isDonneur()         { return donneur; }
    public boolean estVivante()        { return etat != Etat.MORTE; }
    public CouleurRobe getCouleur()    { return couleur; }
    public boolean isSouriceau()       { return souriceau; }
    public Direction getDirection()    { return direction; }

    public void vieillir() {
        age++;
        // Devient adulte après 5 tours
        if (souriceau && age >= 5) souriceau = false;
    }

    public void setDirection(Direction dir) { this.direction = dir; }

    // Définit la case cible vers laquelle la souris doit se diriger après avoir reçu une info nourriture d'un DONNEUR. /
    public void setCaseCibleNourriture(Case c) { this.caseCibleNourriture = c; }
    public void setCaseCiblePartage(Case c)     { this.caseCiblePartage = c; }

    // Retourne la case cible nourriture mémorisée, ou null si aucune. /
    public Case getCaseCibleNourriture() { return caseCibleNourriture; }
    public Case getCaseCiblePartage()     { return caseCiblePartage; }

    public void perdreEnergie(int valeur) {
        energie -= valeur;
        if (energie <= 0) {
            energie = 0;
            etat = Etat.MORTE;
        } else if (energie < 20) {
            etat = Etat.AFFAMEE;
        } else if (energie < 40) {
            etat = Etat.FATIGUEE;
        } else {
            etat = Etat.VIVANTE;
        }
    }

    public void gagnerEnergie(int valeur) {
        energie += valeur;
        if (energie > Configuration.MAX_ENERGY) energie = Configuration.MAX_ENERGY;
        if (energie <= 0)      etat = Etat.MORTE;
        else if (energie < 20) etat = Etat.AFFAMEE;
        else if (energie < 40) etat = Etat.FATIGUEE;
        else                   etat = Etat.VIVANTE;
    }

    public void mourir()                              { energie = 0; etat = Etat.MORTE; }
    public void changerEtat(Etat etat)                { this.etat = etat; }
    public void ecrireJournal(String texte, int temps){ journal.ajouterEntree(texte, temps); }
    public void memoriserCase(Case c)                 { memoire.ajouterSouvenir(c); }
    public void memoriserDanger(Case c)               { memoire.ajouterDanger(c); }
}
