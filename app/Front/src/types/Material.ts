export type MaterialType = "UC" | "ECRAN" | "VIDEO_PROJECTEUR";

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

export type MaterialAssignmentForm = {
    salleId: number;
    materielsId: number[];
}

export const materialTemplate: MaterialForm = {
    typeMateriel: "",
    numeroSerie: "",
    marque: "",
    modele: "",
    dateAjout: new Date(),
    dateFinGarantie: new Date(),
    nom: "",
};

export const materialAssignmentTemplate: MaterialAssignmentForm = {
    salleId: -1, materielsId: []
};


export type ImportMaterialsPayload = {
    salleId: number;
    materiels: MaterialForm[];
};