import { useState } from "react";
import Popup from "../generic/overlay/Popup";
import type { Column } from "../generic/display/Table.tsx";
import { ApiError, stageApi } from "../../api";
import useNotification from "../../hooks/useNotification";
import type { ImportStagesPayload, StageImport } from "../../types/Stage";
import ImportJson from "../generic/ImportJson.tsx";
import type { ValidationResult } from "../generic/ImportJson.tsx";

type Props = {
    onClose: () => void;
    blurBackground?: boolean;
    onSubmit: () => void;
};

type StageRow = StageImport & { key: number };

const requiredFields = ["code", "dateDebut", "dateFin"] as const;

// Défini hors du composant pour rester stable (utilisé dans un useMemo d'ImportJson)
function validateStage(raw: unknown): ValidationResult<StageImport> {
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

    if (typeof data.code !== "string") {
        return { errors: ["code doit être une chaîne de caractères."] };
    }

    const dateDebut = new Date(String(data.dateDebut));
    const dateFin = new Date(String(data.dateFin));
    if (Number.isNaN(dateDebut.getTime()) || Number.isNaN(dateFin.getTime())) {
        return { errors: ["dateDebut et dateFin doivent être des dates valides."] };
    }

    if (dateFin < dateDebut) {
        return { errors: ["dateFin doit être postérieure ou égale à dateDebut."] };
    }

    return {
        errors: [],
        data: { code: data.code, dateDebut, dateFin },
    };
}

const formatDate = (date: Date) => date.toLocaleDateString("fr-FR");

const columns: Column<StageRow>[] = [
    { label: "Code", render: (row) => row.code },
    { label: "Date de début", render: (row) => formatDate(row.dateDebut) },
    { label: "Date de fin", render: (row) => formatDate(row.dateFin) },
];

export default function ImportStagesPopup({ onClose, blurBackground, onSubmit }: Props) {
    const { notify } = useNotification();

    const [selectedStages, setSelectedStages] = useState<StageImport[]>([]);
    const [loading, setLoading] = useState(false);

    const canSubmit = selectedStages.length > 0 && !loading;

    async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
        event.preventDefault();
        if (!canSubmit) return;

        setLoading(true);
        try {
            const payload: ImportStagesPayload = { stages: selectedStages };
            await stageApi.import(payload);
            notify(`${payload.stages.length} stage(s) importé(s)`, "success");
            onSubmit();
        } catch (error) {
            if (error instanceof ApiError) {
                notify(error.status === 400
                    ? `Importation refusée : ${error.message}`
                    : `Impossible d'importer les stages : ${error.message}`, "danger");
            } else {
                notify("Impossible de joindre le serveur pour importer les stages", "danger");
            }
        } finally {
            setLoading(false);
        }
    }

    return (
        <Popup
            title="Importer des stages"
            size="modal-lg"
            onClose={onClose}
            blurBackground={blurBackground}
            actions={
                <button type="submit" form="import-stage-form" disabled={!canSubmit} className="btn btn-primary">
                    {loading
                        ? <><span className="spinner-border spinner-border-sm me-2" aria-hidden="true" />Importation...</>
                        : "Importer"}
                </button>
            }
        >
            <form id="import-stage-form" onSubmit={handleSubmit}>
                <ImportJson<StageImport>
                    validate={validateStage}
                    columns={columns}
                    onSelectionChange={setSelectedStages}
                />
            </form>
        </Popup>
    );
}