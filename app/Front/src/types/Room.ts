import type {Material} from "./Material.ts";

export type RoomType = "STOCKAGE" | "COURS";

export type Room = {
    id: number;
    typeSalle: RoomType;
    etat: string; // dispo/non dispo/maintenance
    nom: string;
    nbPlaces: number;
    nomBatiment: string;
    nbMaterielDispo: number;
    materielsId?: number[];
    materiels?: Material[];
};

export type RoomForm = Omit<Room, "id" | "typeSalle"> & {
    typeSalle: RoomType | "";
};

export const roomTemplate: RoomForm = {
    typeSalle: "COURS",
    etat: "",
    nom: "",
    nbPlaces: 0,
    nomBatiment: "",
    nbMaterielDispo: 0,
    materielsId: []
};