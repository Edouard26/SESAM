import { useEffect, useState } from "react";
import Popup from "../generic/overlay/Popup";
import type { Column } from "../generic/display/Table.tsx";
import { ApiError, stageApi, userApi } from "../../api";
import useNotification from "../../hooks/useNotification";
import type { ImportUsersPayload, UserImport } from "../../types/User";
import type { Stage } from "../../types/Stage";
import ImportJson from "../generic/ImportJson.tsx";
import type { ValidationResult } from "../generic/ImportJson.tsx";
import {Grade} from "../../types/Grade.ts";

type Props = {
    onClose: () => void;
    blurBackground?: boolean;
    onSubmit: () => void;
};

type UserRow = UserImport & { key: number };

const requiredFields = ["nom", "prenom", "nid", "email", "dateNaissance", "grade"] as const;

// Clés de Grade (on écarte les clés numériques si c'est un enum numérique)
const grades = Object.keys(Grade).filter((key) => Number.isNaN(Number(key)));

const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

// Défini hors du composant pour rester stable (utilisé dans un useMemo d'ImportJson)
function validateUser(raw: unknown): ValidationResult<UserImport> {
    if (!raw || typeof raw !== "object" || Array.isArray(raw)) {
        return { errors: ["L'élément doit être un objet JSON."] };
    }

    const data = raw as Record<string, unknown>;
    const missing = requiredFields.filter((field) =>
        data[field] === undefined || data[field] === null || data[field] === ""
    );

    if (missing.length > 0) {
        return { errors: [`Champs obligatoires manquants : ${missing.join(", ")}.`] };
    }

    if (typeof data.nom !== "string" ||
        typeof data.prenom !== "string" ||
        typeof data.nid !== "string" ||
        typeof data.email !== "string") {
        return { errors: ["nom, prenom, nid et email doivent être des chaînes de caractères."] };
    }

    if (!emailRegex.test(data.email)) {
        return { errors: ["email n'est pas une adresse valide."] };
    }

    if (typeof data.grade !== "string" || !grades.includes(data.grade)) {
        return { errors: [`grade doit être l'une des valeurs : ${grades.join(", ")}.`] };
    }

    const dateNaissance = new Date(String(data.dateNaissance));
    if (Number.isNaN(dateNaissance.getTime())) {
        return { errors: ["dateNaissance doit être une date valide."] };
    }
    if (dateNaissance > new Date()) {
        return { errors: ["dateNaissance ne peut pas être dans le futur."] };
    }

    return {
        errors: [],
        data: {
            nom: data.nom,
            prenom: data.prenom,
            nid: data.nid,
            email: data.email,
            dateNaissance,
            grade: data.grade as UserImport["grade"],
        },
    };
}

const formatDate = (date: Date) => date.toLocaleDateString("fr-FR");

const columns: Column<UserRow>[] = [
    { label: "Nom", render: (row) => row.nom },
    { label: "Prénom", render: (row) => row.prenom },
    { label: "NID", render: (row) => row.nid },
    { label: "Email", render: (row) => row.email },
    { label: "Date de naissance", render: (row) => formatDate(row.dateNaissance) },
    { label: "Grade", render: (row) => row.grade },
];

export default function ImportUsersPopup({ onClose, blurBackground, onSubmit }: Props) {
    const { notify } = useNotification();

    const [stages, setStages] = useState<Stage[]>([]);
    const [stageId, setStageId] = useState(-1);
    const [selectedUsers, setSelectedUsers] = useState<UserImport[]>([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        async function loadStages() {
            try {
                const response = await stageApi.list();
                setStages(response.content);
            } catch {
                notify("Impossible de charger les stages", "danger");
            }
        }

        loadStages();
    }, []);

    const canSubmit = stageId > 0 && selectedUsers.length > 0 && !loading;

    async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
        event.preventDefault();
        if (!canSubmit) return;

        setLoading(true);
        try {
            const payload: ImportUsersPayload = {
                stageId,
                stagiaires: selectedUsers,
            };
            await userApi.import(payload);
            notify(`${payload.stagiaires.length} stagiaire(s) importé(s)`, "success");
            onSubmit();
        } catch (error) {
            if (error instanceof ApiError) {
                notify(error.status === 400
                    ? `Importation refusée : ${error.message}`
                    : `Impossible d'importer les utilisateurs : ${error.message}`, "danger");
            } else {
                notify("Impossible de joindre le serveur pour importer les utilisateurs", "danger");
            }
        } finally {
            setLoading(false);
        }
    }

    return (
        <Popup
            title="Importer des stagiaires"
            size="modal-xl"
            onClose={onClose}
            blurBackground={blurBackground}
            actions={
                <button type="submit" form="import-user-form" disabled={!canSubmit} className="btn btn-primary">
                    {loading
                        ? <><span className="spinner-border spinner-border-sm me-2" aria-hidden="true" />Importation...</>
                        : "Importer"}
                </button>
            }
        >
            <form id="import-user-form" onSubmit={handleSubmit}>
                <div className="mb-4">
                    <ImportJson<UserImport>
                        validate={validateUser}
                        columns={columns}
                        onSelectionChange={setSelectedUsers}
                    />
                </div>

                <div>
                    <label htmlFor="stage_id" className="form-label">
                        Stage de destination <span className="text-danger">*</span>
                    </label>
                    <select
                        id="stage_id"
                        className="form-select"
                        value={stageId > 0 ? stageId : ""}
                        onChange={(event) => setStageId(event.target.value ? Number(event.target.value) : -1)}
                        required
                    >
                        <option value="">Choisir un stage</option>
                        {stages.map((stage) => (
                            <option key={stage.id} value={stage.id}>{stage.code}</option>
                        ))}
                    </select>
                    <div className="form-text">
                        Les utilisateurs sélectionnés seront créés avec le rôle STAGIAIRE et affectés à ce stage.
                    </div>
                </div>
            </form>
        </Popup>
    );
}