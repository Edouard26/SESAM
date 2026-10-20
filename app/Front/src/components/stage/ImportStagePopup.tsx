import { useState } from "react";
import Popup from "../generic/overlay/Popup";
import { ApiError, stageApi } from "../../api";
import useNotification from "../../hooks/useNotification";
import type { ImportStagesPayload, StageImport } from "../../types/Stage";
import ImportJson from "../generic/ImportJson.tsx";
import {createValidator, date, type Schema, text} from "../../api/utils/importSchema.ts";
import {createColumns} from "../../api/utils/importColumns.tsx";

type Props = {
    onClose: () => void;
    blurBackground?: boolean;
    onSubmit: () => void;
};

const stageSchema: Schema<StageImport> = {
    code: text(),
    dateDebut: date(),
    dateFin: date(),
};

const validateStage = createValidator(stageSchema, (stage) =>
    stage.dateFin < stage.dateDebut ? "dateFin doit être postérieure ou égale à dateDebut." : undefined
);

const columns = createColumns<StageImport>({
    code: "Code",
    dateDebut: "Date de début",
    dateFin: "Date de fin",
});

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
        <Popup title="Importer des stages" size="modal-lg" onClose={onClose} blurBackground={blurBackground}
            actions={
                <button type="submit" form="import-stage-form" disabled={!canSubmit} className="btn btn-primary">
                    {loading ?
                        <>
                            <span className="spinner-border spinner-border-sm me-2" aria-hidden="true" />
                            Importation...
                        </>
                        : "Importer"}
                </button>
            }
        >
            <form id="import-stage-form" onSubmit={handleSubmit}>
                <ImportJson<StageImport> validate={validateStage} columns={columns} onSelectionChange={setSelectedStages}/>
            </form>
        </Popup>
    );
}