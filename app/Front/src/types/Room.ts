import type { Material } from "./Material.ts";
import type { Rule } from "./Rule.ts";

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
    regles?: Rule[];
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
