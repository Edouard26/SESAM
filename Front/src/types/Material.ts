export type MaterialType = "UniteCentral" | "Ecran" | "VideoProjecteur";

export type Material = {
    id: number;
    typeMateriel: MaterialType;
    numeroSerie: string;
    marque: string;
    modele: string;
    dateAjout: Date;
    dateFinGarantie: Date;
    nom: string;
    processeur?: string;
    carteGraphique?: string;
    taille?: number;
};

export type MaterialForm = Omit<Material, "id" | "typeMateriel"> & {
    typeMateriel: MaterialType | "";
};

export const materialTemplate: MaterialForm = {
    typeMateriel: "",
    numeroSerie: "",
    marque: "",
    modele: "",
    dateAjout: new Date(),
    dateFinGarantie: new Date(),
    nom: "",
};