import { useState } from "react";
import type { Processus } from "../../types/Processus.ts";
import type { MasterForm } from "../../types/Master.ts";
import MultiSelect from "../generic/display/MultiSelect.tsx";
import Popup from "../generic/overlay/Popup.tsx";

type MasterPopupProps = {
    processes: Processus[];
    saving: boolean;
    onClose: () => void;
    onSubmit: (form: MasterForm) => void;
};

// Formulaire de création d'un master : nom et processus associés (sélection multiple)
export default function MasterPopup({
                                        processes,
                                        saving,
                                        onClose,
                                        onSubmit,
                                    }: MasterPopupProps) {
    const [nom, setNom] = useState("");
    const [processusId, setProcessusId] = useState<number[]>([]);

    return (
        <Popup
            title="Ajouter un master"
            onClose={onClose}
            confirmationTitle="Créer le master ?"
            confirmationMessage="Confirmer la création de ce master ?"
            confirmationLabel="Confirmer"
            actions={
                <button
                    type="submit"
                    form="master-form"
                    className="btn btn-primary"
                    disabled={saving}
                >
                    {saving ? "Enregistrement..." : "Enregistrer"}
                </button>
            }
        >
            <form
                id="master-form"
                onSubmit={(event) => {
                    event.preventDefault();

                    if (nom.trim()) {
                        onSubmit({
                            nom: nom.trim(),
                            processusId,
                        });
                    }
                }}
            >
                <div className="mb-3">
                    <label htmlFor="master-name" className="form-label">
                        Nom
                    </label>

                    <input
                        id="master-name"
                        className="form-control"
                        value={nom}
                        maxLength={20}
                        required
                        onChange={(event) => setNom(event.target.value)}
                    />
                </div>

                <div className="mb-3">
                    <div className="form-label">
                        Processus associés
                    </div>

                    <MultiSelect
                        searchOptions={async (query) => ({
                            content: processes.filter((process) =>
                                process.nom.toLocaleLowerCase("fr").includes(
                                    query.toLocaleLowerCase("fr")
                                )
                            ),
                        })}
                        getOptionValue={(process) => process.id}
                        getOptionLabel={(process) => process.nom}
                        value={processusId}
                        onChange={setProcessusId}
                    />
                </div>
            </form>
        </Popup>
    );
}