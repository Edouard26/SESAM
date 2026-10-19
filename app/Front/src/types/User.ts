import type { Role } from "./Role.ts";
import type { Grade } from "./Grade.ts";

export type User = {
    id: number;
    nom: string;
    nid: string;
    prenom: string;
    email: string;
    role: Role;
    dateNaissance: Date;
    grade: keyof typeof Grade;
    active: boolean;
};

// L'API échange la date de naissance au format "YYYY-MM-DD" (string en JSON).
export type UserForm = Omit<User, "id" | "dateNaissance"> & {
    dateNaissance: string;
    password?: string;
};

export type ImportedUser = Omit<UserForm, "active">;

// Valeur "YYYY-MM-DD" pour un <input type="date">, que l'API renvoie une string ou un Date.
export const toDateInputValue = (value: Date | string) =>
    (typeof value === "string" ? value : value.toISOString()).slice(0, 10);

export const getUserFullName = (user: Pick<User, "prenom" | "nom">) =>
    `${user.prenom} ${user.nom}`;