import type {Role} from "./Role.ts";

export type User = {
    id: number;
    nom: string;
    prenom: string;
    email: string;
    role: Role;
    active: boolean;
};