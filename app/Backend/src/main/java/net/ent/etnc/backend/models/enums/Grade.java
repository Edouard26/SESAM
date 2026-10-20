package net.ent.etnc.backend.models.enums;

import lombok.Getter;

public enum Grade {
    //-------------------//
    // Militaire du rang //
    //-------------------//
    SOLDAT("Soldat", "SLT"),
    AVIATEUR("Aviateur", "AVT"),
    CAPORAL("Caporal", "CPL"),
    CAPORAL_CHEF("Caporal-chef", "CCH"),
    CAPORAL_CHEF_PREMIERE_CLASSE("Caporal-chef de première classe", "CC1"),
    // Marine national
    MATELOT("Matelot", "MAT"),
    QUARTIER_MAITRE_2("Quartier-maître de deuxième classe", "QM2"),
    QUARTIER_MAITRE_1("Quartier-maître de première classe", "QM1"),

    //---------------//
    // Sous-officier //
    //---------------//
    SERGENT("Sergent", "SGT"),
    SERGENT_CHEF("Sergent-chef", "SCH"),
    ADJUDANT("Adjudant", "ADJ"),
    ADJUDANT_CHEF("Adjudant-chef", "ADC"),
    MAJOR("Major", "MAJ"),
    // Marine national
    SECOND_MAITRE("Second maître", "SM"),
    MAITRE("Maître", "MT"),
    PREMIER_MAITRE("Premier maître", "PM"),
    MAITRE_PRINCIPAL("Maître principal", "MP"),

    //----------//
    // Officier //
    //----------//
    ASPIRANT("Aspirant", "ASP"),
    SOUS_LIEUTENANT("Sous-lieutenant", "SLT"),
    LIEUTENANT("Lieutenant", "LTN"),
    CAPITAINE("Capitaine", "CNE"),
    COMMANDANT("Commandant", "CDT"),
    LIEUTENANT_COLONEL("Lieutenant-colonel", "LCL"),
    COLONEL("Colonel", "COL"),
    // Marine national
    ENSEIGNE_VAISSEAU_2("Enseigne de vaisseau de deuxième classe", "EV2"),
    ENSEIGNE_VAISSEAU_1("Enseigne de vaisseau de première classe", "EV1"),
    LIEUTENANT_VAISSEAU("Lieutenant de vaisseau", "LV"),
    CAPITAINE_CORVETTE("Capitaine de corvette", "CC"),
    CAPITAINE_FREGATE("Capitaine de frégate", "CF"),
    CAPITAINE_VAISSEAU("Capitaine de vaisseau", "CV"),

    //----------//
    // Généraux //
    //----------//
    GENERAL_BRIGADE("Général de brigade", "GDI"),
    GENERAL_DIVISION("Général de division", "GDA"),
    GENERAL_CORPS_ARMEE("Général de corps d'armée", "GCA"),
    GENERAL_ARMEE("Général d'armée", "GA"),
    // Marine national
    CONTRE_AMIRAL("Contre-amiral", "CA"),
    VICE_AMIRAL("Vice-amiral", "VA"),
    VICE_AMIRAL_ESCADRE("Vice-amiral d'escadre", "VAE"),
    AMIRAL("Amiral", "AM");

    @Getter
    private final String libelle;
    @Getter
    private final String abreviation;

    Grade(String libelle, String abreviation) {
        this.libelle = libelle;
        this.abreviation = abreviation;
    }

    @Override
    public String toString() {
        return libelle;
    }
}
