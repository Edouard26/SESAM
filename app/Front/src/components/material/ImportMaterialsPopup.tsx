import { useEffect, useState } from "react";
import Popup from "../generic/overlay/Popup";
import { ApiError, materialApi, roomApi } from "../../api";
import useNotification from "../../hooks/useNotification";
import type {ImportMaterialsPayload, MaterialForm, MaterialType} from "../../types/Material";
import type { Room } from "../../types/Room.ts";
import ImportJson from "../generic/ImportJson.tsx";
import {createColumns} from "../../api/utils/importColumns.tsx";
import {
    createValidator,
    date,
    oneOf,
    optional,
    positiveNumber,
    type Schema,
    text
} from "../../api/utils/importSchema.ts";

type Props = {
    onClose: () => void;
    blurBackground?: boolean;
    onSubmit: () => void;
};

const materialTypes: MaterialType[] = ["UC", "ECRAN", "VIDEO_PROJECTEUR"];

const materialSchema: Schema<MaterialForm> = {
    nom: text(),
    typeMateriel: oneOf(materialTypes),
    numeroSerie: text(),
    marque: text(),
    modele: text(),
    dateAjout: date(),
    dateFinGarantie: date(),
    processeur: optional(text()),
    carteGraphique: optional(text()),
    taille: optional(positiveNumber()),
};

const validateMaterial = createValidator(materialSchema, (material) => {
    if (material.typeMateriel === "UC" && (!material.processeur || !material.carteGraphique)) {
        return "processeur et carteGraphique sont obligatoires pour une unité centrale.";
    }
    if (material.typeMateriel === "ECRAN" && material.taille === undefined) {
        return "taille est obligatoire pour un écran.";
    }
    return undefined;
});

const columns = createColumns<MaterialForm>(
    {
        nom: "Nom",
        typeMateriel: "Type",
        numeroSerie: "N° de série",
        marque: "Marque",
        modele: "Modèle",
        dateAjout: "Date d'ajout",
        dateFinGarantie: "Fin de garantie",
        processeur: "Processeur",
        carteGraphique: "Carte graphique",
        taille: "Taille",
    },
    { taille: (value) => value !== undefined ? `${value}"` : "—" },
);

export default function ImportMaterialsPopup({ onClose, blurBackground, onSubmit }: Props) {
    const { notify } = useNotification();

    const [rooms, setRooms] = useState<Room[]>([]);
    const [salleId, setSalleId] = useState(-1);
    const [selectedMaterials, setSelectedMaterials] = useState<MaterialForm[]>([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        async function loadRooms() {
            try {
                const response = await roomApi.list();
                setRooms(response.content);
            } catch {
                notify("Impossible de charger les salles", "danger");
            }
        }

        loadRooms();
    }, []);

    const canSubmit = salleId > 0 && selectedMaterials.length > 0 && !loading;

    async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
        event.preventDefault();
        if (!canSubmit) return;

        setLoading(true);
        try {
            const payload: ImportMaterialsPayload = {
                salleId,
                materiels: selectedMaterials,
            };
            await materialApi.import(payload);
            notify(`${payload.materiels.length} matériel(aux) importé(s)`, "success");
            onSubmit();
        } catch (error) {
            if (error instanceof ApiError) {
                notify(error.status === 400
                    ? `Importation refusée : ${error.message}`
                    : `Impossible d'importer les matériels : ${error.message}`, "danger");
            } else {
                notify("Impossible de joindre le serveur pour importer les matériels", "danger");
            }
        } finally {
            setLoading(false);
        }
    }

    return (
        <Popup title="Importer des matériels" size="modal-xl" onClose={onClose} blurBackground={blurBackground}
            actions={
                <button type="submit" form="import-form" disabled={!canSubmit} className="btn btn-primary">
                    {loading ?
                        <>
                            <span className="spinner-border spinner-border-sm me-2" aria-hidden="true" />
                            Importation...
                        </>
                        : "Importer"}
                </button>
            }
        >
            <form id="import-form" onSubmit={handleSubmit}>
                <div className="mb-4">
                    <ImportJson<MaterialForm> validate={validateMaterial} columns={columns} onSelectionChange={setSelectedMaterials}/>
                </div>

                <div>
                    <label htmlFor="salle_id" className="form-label">
                        Salle de destination <span className="text-danger">*</span>
                    </label>
                    <select id="salle_id" className="form-select" value={salleId > 0 ? salleId : ""} required
                        onChange={(event) => setSalleId(event.target.value ? Number(event.target.value) : -1)}
                    >
                        <option value="">Choisir une salle</option>
                        {rooms.map((room) => (
                            <option key={room.id} value={room.id}>{room.nom}</option>
                        ))}
                    </select>
                    <div className="form-text">
                        Les matériels sélectionnés seront créés et affectés à cette salle.
                    </div>
                </div>
            </form>
        </Popup>
    );
}