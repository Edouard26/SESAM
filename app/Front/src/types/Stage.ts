export type StageStatut = "A_AFFECTER" | "AFFECTER" | "EN_COURS" | "CLOTURER";

export type Stage = {
    id: number;
    code: string;
    dateDebut: Date;
    dateFin: Date;
    statut: StageStatut;
    stagiairesId: number[];
    salleId: number;
};

export type StageForm = Omit<Stage, "id" | "stagiairesId" | "statut"> & {
};

export type StageImport = Omit<StageForm, "salleId">;

export type ImportStagesPayload = {
    stages: StageImport[];
};