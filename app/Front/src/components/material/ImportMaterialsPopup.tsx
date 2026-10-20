import { useEffect, useState } from "react";
import Popup from "../generic/overlay/Popup";
import { ApiError, materialApi, roomApi } from "../../api";
import useNotification from "../../hooks/useNotification";
import type {ImportMaterialsPayload, MaterialForm, MaterialType} from "../../types/Material";
import type { Room } from "../../types/Room.ts";
import ImportJson from "../generic/ImportJson.tsx";
import type { ValidationResult } from "../generic/ImportJson.tsx";
import type {Column} from "../generic/display/Table.tsx";

type Props = {
    onClose: () => void;
    blurBackground?: boolean;
    onSubmit: () => void;
};

const requiredFields = [
    "nom",
    "typeMateriel",
    "numeroSerie",
    "marque",
    "modele",
    "dateAjout",
    "dateFinGarantie",
] as const;

const materialTypes: MaterialType[] = ["UC", "ECRAN", "VIDEO_PROJECTEUR"];

// Défini hors du composant pour rester stable (utilisé dans un useMemo d'ImportJson)
function validateMaterial(raw: unknown): ValidationResult<MaterialForm> {
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
        typeof data.numeroSerie !== "string" ||
        typeof data.marque !== "string" ||
        typeof data.modele !== "string") {
        return { errors: ["nom, numeroSerie, marque et modele doivent être des chaînes de caractères."] };
    }

    if (typeof data.typeMateriel !== "string" || !materialTypes.includes(data.typeMateriel as MaterialType)) {
        return { errors: ["typeMateriel doit être UC, ECRAN ou VIDEO_PROJECTEUR."] };
    }

    const dateAjout = new Date(String(data.dateAjout));
    const dateFinGarantie = new Date(String(data.dateFinGarantie));
    if (Number.isNaN(dateAjout.getTime()) || Number.isNaN(dateFinGarantie.getTime())) {
        return { errors: ["dateAjout et dateFinGarantie doivent être des dates valides."] };
    }

    if (data.typeMateriel === "UC" &&
        (typeof data.processeur !== "string" || typeof data.carteGraphique !== "string")) {
        return { errors: ["processeur et carteGraphique sont obligatoires pour une unité centrale."] };
    }

    const hasTaille = data.taille !== undefined && data.taille !== null && data.taille !== "";

    if (data.typeMateriel === "ECRAN" &&
        (!hasTaille || !Number.isFinite(Number(data.taille)) || Number(data.taille) <= 0)) {
        return { errors: ["taille doit être un nombre positif pour un écran."] };
    }

    return {
        errors: [],
        data: {
            nom: data.nom,
            typeMateriel: data.typeMateriel as MaterialType,
            numeroSerie: data.numeroSerie,
            marque: data.marque,
            modele: data.modele,
            dateAjout,
            dateFinGarantie,
            ...(typeof data.processeur === "string" && { processeur: data.processeur }),
            ...(typeof data.carteGraphique === "string" && { carteGraphique: data.carteGraphique }),
            ...(hasTaille && { taille: Number(data.taille) }),
        },
    };
}

export default function ImportMaterialsPopup({ onClose, blurBackground, onSubmit }: Props) {
    const { notify } = useNotification();

    const [rooms, setRooms] = useState<Room[]>([]);
    const [salleId, setSalleId] = useState(-1);
    const [selectedMaterials, setSelectedMaterials] = useState<MaterialForm[]>([]);
    const [loading, setLoading] = useState(false);

    type MaterialRow = MaterialForm & { key: number };

    const formatDate = (date: Date) => date.toLocaleDateString("fr-FR");

    const materialColumns: Column<MaterialRow>[] = [
        { label: "Nom", render: (row) => row.nom },
        { label: "Type", render: (row) => row.typeMateriel },
        { label: "N° de série", render: (row) => row.numeroSerie },
        { label: "Marque", render: (row) => row.marque },
        { label: "Modèle", render: (row) => row.modele },
        { label: "Date d'ajout", render: (row) => formatDate(row.dateAjout) },
        { label: "Fin de garantie", render: (row) => formatDate(row.dateFinGarantie) },
        { label: "Processeur", render: (row) => row.processeur ?? "—" },
        { label: "Carte graphique", render: (row) => row.carteGraphique ?? "—" },
        { label: "Taille", render: (row) => row.taille !== undefined ? `${row.taille}"` : "—" },
    ];

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
        <Popup
            title="Importer des matériels"
            size="modal-xl"
            onClose={onClose}
            blurBackground={blurBackground}
            actions={
                <button type="submit" form="import-form" disabled={!canSubmit} className="btn btn-primary">
                    {loading
                        ? <><span className="spinner-border spinner-border-sm me-2" aria-hidden="true" />Importation...</>
                        : "Importer"}
                </button>
            }
        >
            <form id="import-form" onSubmit={handleSubmit}>
                <div className="mb-4">
                    <ImportJson<MaterialForm>
                        validate={validateMaterial}
                        columns={materialColumns}
                        onSelectionChange={setSelectedMaterials}
                    />
                </div>

                <div>
                    <label htmlFor="salle_id" className="form-label">
                        Salle de destination <span className="text-danger">*</span>
                    </label>
                    <select
                        id="salle_id"
                        className="form-select"
                        value={salleId > 0 ? salleId : ""}
                        onChange={(event) => setSalleId(event.target.value ? Number(event.target.value) : -1)}
                        required
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