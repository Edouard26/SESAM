export type Building = {
    id: number;
    nom: string;
}

export type BuildingForm = Omit<Building, "id">;