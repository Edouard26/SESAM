export type AlertStatut = "EN_ATTENTE" | "TRAITEE" | "ANNULEE";

export type Alert = {
    id: number;
    dateSignalement: Date;
    resolveurId: number;
    dateResolution: Date;
    typeProbleme: string;
    description: string;
    statut: AlertStatut;
    materielsId: number[];

};

export type AlertForm = {
    signaleurId: number;
    dateSignalement: Date;
    typeProbleme: string;
    description: string;
    materielsId: number[];
};

export type AlertUpdateForm = {
    resolveurId: number;
    dateResolution: Date;
    statut: AlertStatut;
};