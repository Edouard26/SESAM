import {useEffect, useMemo, useState} from "react";
import Popup from "../generic/overlay/Popup";
import {ApiError, materialApi, roomApi} from "../../api";
import useNotification from "../../hooks/useNotification";
import type {ImportedMaterial, MaterialType} from "../../types/Material";
import type {Room} from "../../types/Room.ts";
import Filters from "../generic/Filters.tsx";
import Table, {type Column} from "../generic/display/Table.tsx";

type Props = {
    onClose: () => void;
    blurBackground?: boolean;
    onSubmit: () => void;
};

type ImportRow = Omit<ImportedMaterial, "salleId"> & { id: number };

const PAGE_SIZE = 10;

const requiredFields = [
    "nom",
    "typeMateriel",
    "numeroSerie",
    "marque",
    "modele",
    "dateAjout",
    "dateFinGarantie",
] as const;

const materialTypes: MaterialType[] = ["UniteCentral", "Ecran", "VideoProjecteur"];

const selects = [
    {
        name: "type",
        label: "Type de matériel",
        options: [
            { label: "Tous", value: "" },
            { label: "UC", value: "UniteCentral" },
            { label: "Écran", value: "Ecran" },
            { label: "Vidéoprojecteur", value: "VideoProjecteur" },
        ],
    },
];

const initialFilters: Record<string, string> = { type: "" };

const columns = [
    { key: "nom", label: "Nom" },
    { key: "marque", label: "Marque" },
    { key: "modele", label: "Modèle" },
    { key: "typeMateriel", label: "Type" },
] satisfies Column<ImportRow>[];

function validateMaterials(value: unknown): { rows: ImportRow[]; errors: string[] } {
    const rows: ImportRow[] = [];
    const errors: string[] = [];

    if (!Array.isArray(value)) {
        return { rows, errors: ["Le JSON doit contenir un tableau de matériels."] };
    }

    if (value.length === 0) {
        return { rows, errors: ["Le fichier ne contient aucun matériel."] };
    }

    value.forEach((item: unknown, index: number) => {
        const line = index + 1;

        if (!item || typeof item !== "object" || Array.isArray(item)) {
            errors.push(`Ligne ${line} : l'élément doit être un objet JSON.`);
            return;
        }

        const data = item as Record<string, unknown>;
        const missing = requiredFields.filter((field) =>
            data[field] === undefined || data[field] === null || data[field] === ""
        );

        if (missing.length > 0) {
            errors.push(`Ligne ${line} : champs obligatoires manquants : ${missing.join(", ")}.`);
            return;
        }

        if (typeof data.nom !== "string" ||
            typeof data.numeroSerie !== "string" ||
            typeof data.marque !== "string" ||
            typeof data.modele !== "string") {
            errors.push(`Ligne ${line} : nom, numeroSerie, marque et modele doivent être des chaînes de caractères.`);
            return;
        }

        if (typeof data.typeMateriel !== "string" || !materialTypes.includes(data.typeMateriel as MaterialType)) {
            errors.push(`Ligne ${line} : typeMateriel doit être UniteCentral, Ecran ou VideoProjecteur.`);
            return;
        }

        const dateAjout = new Date(String(data.dateAjout));
        const dateFinGarantie = new Date(String(data.dateFinGarantie));
        if (Number.isNaN(dateAjout.getTime()) || Number.isNaN(dateFinGarantie.getTime())) {
            errors.push(`Ligne ${line} : dateAjout et dateFinGarantie doivent être des dates valides.`);
            return;
        }

        if (data.typeMateriel === "UniteCentral" &&
            (typeof data.processeur !== "string" || typeof data.carteGraphique !== "string")) {
            errors.push(`Ligne ${line} : processeur et carteGraphique sont obligatoires pour une unité centrale.`);
            return;
        }

        const hasTaille = data.taille !== undefined && data.taille !== null && data.taille !== "";

        if (data.typeMateriel === "Ecran" && (!hasTaille || !Number.isFinite(Number(data.taille)) || Number(data.taille) <= 0)) {
            errors.push(`Ligne ${line} : taille doit être un nombre positif pour un écran.`);
            return;
        }

        rows.push({
            id: line,
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
        });
    });

    return { rows, errors };
}

export default function ImportMaterialsPopup({ onClose, blurBackground, onSubmit }: Props) {
    const { notify } = useNotification();

    const [fileName, setFileName] = useState("");
    const [rows, setRows] = useState<ImportRow[]>([]);
    const [errors, setErrors] = useState<string[]>([]);
    const [rooms, setRooms] = useState<Room[]>([]);
    const [salleId, setSalleId] = useState(-1);
    const [selectedMaterials, setSelectedMaterials] = useState<ImportRow[]>([]);
    const [search, setSearch] = useState("");
    const [filters, setFilters] = useState(initialFilters);
    const [page, setPage] = useState(0);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        setFileName("");
        setRows([]);
        setErrors([]);
        setSalleId(-1);
        setSelectedMaterials([]);
        setSearch("");
        setFilters(initialFilters);
        setPage(0);
        setLoading(false);

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

    const filtered = useMemo(() => {
        const term = search.trim().toLowerCase();
        return rows.filter((row) =>
            (!filters.type || row.typeMateriel === filters.type) &&
            (!term || [row.nom, row.numeroSerie, row.marque, row.modele]
                .some((field) => field.toLowerCase().includes(term)))
        );
    }, [rows, search, filters]);

    const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
    const pageItems = filtered.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE);
    const hasSource = rows.length > 0;
    const canSubmit = salleId > 0 && selectedMaterials.length > 0 && !loading;

    function handleSearchChange(value: string) {
        setSearch(value);
        setPage(0);
    }

    function handleFilterChange(name: string, value: string) {
        setFilters((previous) => ({ ...previous, [name]: value }));
        setPage(0);
    }

    function resetFilters() {
        setSearch("");
        setFilters(initialFilters);
        setPage(0);
    }

    async function handleFileChange(event: React.ChangeEvent<HTMLInputElement>) {
        const file = event.target.files?.[0];
        setFileName(file?.name ?? "");
        setRows([]);
        setErrors([]);
        setSelectedMaterials([]);
        resetFilters();

        if (!file) return;

        if (!file.name.toLowerCase().endsWith(".json")) {
            setErrors(["Sélectionnez un fichier JSON (.json)."]);
            return;
        }

        try {
            const result = validateMaterials(JSON.parse(await file.text()));
            setRows(result.rows);
            setErrors(result.errors);
        } catch {
            setErrors(["Le fichier n'est pas un JSON valide."]);
        }
    }

    async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
        event.preventDefault();
        if (!canSubmit) return;

        setLoading(true);
        try {
            const payload: ImportedMaterial[] = selectedMaterials.map(({ id: _id, ...material }) => ({
                ...material,
                salleId,
            }));
            await materialApi.import(payload);
            notify(`${payload.length} matériel(aux) importé(s)`, "success");
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
            size="modal-lg"
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
                    <label htmlFor="materials-json-file" className="form-label">
                        Source de données (fichier JSON) <span className="text-danger">*</span>
                    </label>
                    <input
                        id="materials-json-file"
                        className="form-control"
                        type="file"
                        accept=".json,application/json"
                        onChange={handleFileChange}
                    />
                    {fileName && <div className="form-text">Fichier sélectionné : {fileName}</div>}
                </div>

                {errors.length > 0 && (
                    <div className={`alert ${hasSource ? "alert-warning" : "alert-danger"}`} role="alert">
                        <div className="fw-semibold mb-2">
                            {hasSource
                                ? "Certaines lignes sont invalides et ne peuvent pas être importées."
                                : "Le fichier contient des erreurs."}
                        </div>
                        <ul className="mb-0">
                            {errors.map((error, index) => <li key={`${index}-${error}`}>{error}</li>)}
                        </ul>
                    </div>
                )}

                {hasSource && (
                    <div className="mb-4">
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
                )}

                {hasSource && salleId > 0 && (
                    <>
                        <Filters
                            embedded
                            search={search}
                            searchPlaceholder="Nom, numéro de série ou marque..."
                            selects={selects}
                            values={filters}
                            onSearchChange={handleSearchChange}
                            onFilterChange={handleFilterChange}
                            onReset={resetFilters}
                        />
                        <Table
                            columns={columns}
                            data={pageItems}
                            currentPage={page}
                            totalPages={totalPages}
                            onPageChange={setPage}
                            loading={false}
                            totalElements={filtered.length}
                            selection={{
                                selected: selectedMaterials,
                                getId: (material) => material.id,
                                onChange: setSelectedMaterials,
                                allItems: filtered,
                            }}
                        />
                    </>
                )}
            </form>
        </Popup>
    );
}