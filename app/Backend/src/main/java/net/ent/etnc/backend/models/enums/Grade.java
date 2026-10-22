package net.ent.etnc.backend.models.enums;

import lombok.Getter;

public enum Grade {
    //-------------------//
    // Militaire du rang //
    //-------------------//
    SOLDAT,
    AVIATEUR,
    CAPORAL,
    CAPORAL_CHEF,
    CAPORAL_CHEF_PREMIERE_CLASSE,
    // Marine national
    MATELOT,
    QUARTIER_MAITRE_2,
    QUARTIER_MAITRE_1,

    //---------------//
    // Sous-officier //
    //---------------//
    SERGENT,
    SERGENT_CHEF,
    ADJUDANT,
    ADJUDANT_CHEF,
    MAJOR,
    // Marine national
    SECOND_MAITRE,
    MAITRE,
    PREMIER_MAITRE,
    MAITRE_PRINCIPAL,

    //----------//
    // Officier //
    //----------//
    ASPIRANT,
    SOUS_LIEUTENANT,
    LIEUTENANT,
    CAPITAINE,
    COMMANDANT,
    LIEUTENANT_COLONEL,
    COLONEL,
    // Marine national
    ENSEIGNE_VAISSEAU_2,
    ENSEIGNE_VAISSEAU_1,
    LIEUTENANT_VAISSEAU,
    CAPITAINE_CORVETTE,
    CAPITAINE_FREGATE,
    CAPITAINE_VAISSEAU,

    //----------//
    // Généraux //
    //----------//
    GENERAL_BRIGADE,
    GENERAL_DIVISION,
    GENERAL_CORPS_ARMEE,
    GENERAL_ARMEE,
    // Marine national
    CONTRE_AMIRAL,
    VICE_AMIRAL,
    VICE_AMIRAL_ESCADRE,
    AMIRAL

//    @Getter
//    private final String libelle;
//    @Getter
//    private final String abreviation;
//
//    Grade(String libelle, String abreviation) {
//        this.libelle = libelle;
//        this.abreviation = abreviation;
//    }
//
//    @Override
//    public String toString() {
////        return libelle;
////    }
}
