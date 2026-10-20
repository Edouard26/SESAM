import { useEffect, useState } from "react";
import Popup from "../generic/overlay/Popup";
import { ApiError, stageApi, userApi } from "../../api";
import useNotification from "../../hooks/useNotification";
import type { ImportUsersPayload, UserImport } from "../../types/User";
import type { Stage } from "../../types/Stage";
import ImportJson from "../generic/ImportJson.tsx";
import {Grade} from "../../types/Grade.ts";
import {createValidator, email, oneOf, pastDate, type Schema, text} from "../../api/utils/importSchema.ts";
import {createColumns} from "../../api/utils/importColumns.tsx";

type Props = {
    onClose: () => void;
    blurBackground?: boolean;
    onSubmit: () => void;
};

const grades = Object.keys(Grade).filter((key) => Number.isNaN(Number(key))) as UserImport["grade"][];

const userSchema: Schema<UserImport> = {
    nom: text(),
    prenom: text(),
    nid: text(),
    email: email(),
    dateNaissance: pastDate(),
    grade: oneOf(grades),
};

const validateUser = createValidator(userSchema);

const columns = createColumns<UserImport>({
    nom: "Nom",
    prenom: "Prénom",
    nid: "NID",
    email: "Email",
    dateNaissance: "Date de naissance",
    grade: "Grade",
});

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
        <Popup title="Importer des stagiaires" size="modal-xl" onClose={onClose} blurBackground={blurBackground}
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
                    <ImportJson<UserImport> validate={validateUser} columns={columns} onSelectionChange={setSelectedUsers}/>
                </div>

                <div>
                    <label htmlFor="stage_id" className="form-label">
                        Stage de destination <span className="text-danger">*</span>
                    </label>
                    <select id="stage_id" className="form-select" value={stageId > 0 ? stageId : ""} required
                        onChange={(event) => setStageId(event.target.value ? Number(event.target.value) : -1)}
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