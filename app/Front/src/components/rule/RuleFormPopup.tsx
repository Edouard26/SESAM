import { useState } from "react";
import type { Processus } from "../../types/Processus.ts";
import type { Rule } from "../../types/Rule.ts";
import type { RuleForm } from "../../types/RuleForm.ts";
import MultiSelect from "../generic/display/MultiSelect.tsx";
import Popup from "../generic/overlay/Popup.tsx";

type RuleFormPopupProps = {
    rule: Rule | null;
    processes: Processus[];
    saving: boolean;
    onClose: () => void;
    onSubmit: (form: RuleForm) => void;
};

// Création ou modification d'une règle (nom + processus interdits) ; la liste des processus vient du parent
export default function RuleFormPopup({ rule, processes, saving, onClose, onSubmit }: RuleFormPopupProps) {
    const [nom, setNom] = useState(rule?.nom ?? "");
    const [processusId, setProcessusId] = useState(rule?.processusId ?? []);

    return (
        <Popup
            title={rule ? "Modifier la règle" : "Ajouter une règle"}
            onClose={onClose}
            confirmationTitle={rule ? "Modifier la règle ?" : "Créer la règle ?"}
            confirmationMessage="Confirmer l’enregistrement de cette règle ?"
            confirmationLabel="Confirmer"
            actions={<button type="submit" form="rule-form" className="btn btn-primary" disabled={saving}>{saving ? "Enregistrement..." : "Enregistrer"}</button>}>
            <form id="rule-form" onSubmit={(event) => {
                event.preventDefault();
                if (nom.trim()) onSubmit({ nom: nom.trim(), processusId });
            }}>
                <div className="mb-3">
                    <label htmlFor="rule-name" className="form-label">Nom</label>
                    <input id="rule-name" className="form-control" value={nom} maxLength={20} required onChange={(event) => setNom(event.target.value)} />
                </div>
                <div className="mb-3">
                    <div className="form-label">Processus interdits</div>
                    <MultiSelect
                        searchOptions={async (query) => ({ content: processes.filter((process) => process.nom.toLocaleLowerCase("fr").includes(query.toLocaleLowerCase("fr"))) })}
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
