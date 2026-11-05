


import { useState } from "react";
import type { FormEvent } from "react";
import Popup from "../generic/overlay/Popup";
import { ApiError, stageApi } from "../../api";
import useNotification from "../../hooks/useNotification";
import type { ImportStagesPayload, StageImport } from "../../types/Stage";

type Props = {
    onClose: () => void;
    blurBackground?: boolean;
    onSubmit: () => void;
};

const sagaieStages: StageImport[] = [
    {
        code: "DV26",
        dateDebut: new Date("2026-11-16T00:00:00"),
        dateFin: new Date("2026-11-27T00:00:00"),
    },
    {
        code: "IN26",
        dateDebut: new Date("2026-12-01T00:00:00"),
        dateFin: new Date("2026-12-11T00:00:00"),
    },
    {
        code: "SY27",
        dateDebut: new Date("2027-01-04T00:00:00"),
        dateFin: new Date("2027-01-15T00:00:00"),
    },
];

function formatDate(value: Date) {
    return value.toLocaleDateString("fr-FR");
}

export default function ImportStagesPopup({ onClose, blurBackground, onSubmit }: Props) {
    const { notify } = useNotification();
    const [loading, setLoading] = useState(false);

    const canSubmit = sagaieStages.length > 0 && !loading;

    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();
        if (!canSubmit) return;

        setLoading(true);

        try {
            const payload: ImportStagesPayload = { stages: sagaieStages };
            await stageApi.import(payload);

            notify(`${payload.stages.length} stage(s) importé(s)`, "success");
            onSubmit();
        } catch (error) {
            if (error instanceof ApiError) {
                notify(
                    error.status === 400
                        ? `Importation refusée : ${error.message}`
                        : `Impossible d'importer les stages : ${error.message}`,
                    "danger",
                );
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
                <button
                    type="submit"
                    form="import-stage-form"
                    disabled={!canSubmit}
                    className="btn btn-primary"
                >
                    {loading ? (
                        <>
                            <span className="spinner-border spinner-border-sm me-2" aria-hidden="true" />
                            Importation...
                        </>
                    ) : (
                        "Importer les stages"
                    )}
                </button>
            }
        >
            <form id="import-stage-form" onSubmit={handleSubmit}>
                <div className="alert alert-info d-flex align-items-center gap-2 mb-3" role="status">
                    <i className="bi bi-cloud-download" aria-hidden="true" />
                    <span>Stages chargés depuis SAGAIE</span>
                </div>

                <p className="text-secondary mb-3">
                    {sagaieStages.length} stage(s) disponible(s) à l'importation.
                </p>

                <div className="table-responsive">
                    <table className="table table-hover align-middle mb-0">
                        <thead>
                        <tr>
                            <th scope="col">Code</th>
                            <th scope="col">Date de début</th>
                            <th scope="col">Date de fin</th>
                        </tr>
                        </thead>
                        <tbody>
                        {sagaieStages.map((stage) => (
                            <tr key={stage.code}>
                                <td className="fw-semibold">{stage.code}</td>
                                <td>{formatDate(stage.dateDebut)}</td>
                                <td>{formatDate(stage.dateFin)}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            </form>
        </Popup>
    );
}

