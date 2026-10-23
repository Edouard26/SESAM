import type { Material } from "./Material.ts";

export type RoomType = "STOCKAGE" | "COURS";

export type Room = {
    id: number;
    typeSalle: RoomType;
    etat: string;
    nom: string;
    nbPlaces: number;
    nomBatiment: string;
    nbMaterielDispo: number;
    materielsId?: number[];
    materiels?: Material[];
};

export type RoomForm = {
    typeSalle: RoomType | "";
    etat: string;
    nom: string;
    nbPlaces: number;
    nomBatiment: string;
};

export const roomTemplate: RoomForm = {
    typeSalle: "COURS",
    etat: "",
    nom: "",
    nbPlaces: 0,
    nomBatiment: "",
};