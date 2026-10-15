import { useEffect, useState } from "react";
import Popup from "../generic/overlay/Popup";
import {
    materialAssignmentTemplate, type MaterialAssignmentForm, type Material,
} from "../../types/Material";
import {ApiError, materialApi, roomApi} from "../../api";
import useNotification from "../../hooks/useNotification.ts";
import type {Room} from "../../types/Room.ts";
import Filters from "../generic/Filters.tsx";
import Table, {type Column} from "../generic/display/Table.tsx";
import usePaginatedList from "../../hooks/usePaginatedList.ts";


type Props = {
    open: boolean;
    onClose: () => void;
    blurBackground?: boolean;
    onSubmit: (material: MaterialAssignmentForm) => void;
};

export default function AssignmentsPopup({ open, onClose, blurBackground, onSubmit }: Props) {
    const {notify} = useNotification();
    const [values, setValues] = useState<MaterialAssignmentForm>({...materialAssignmentTemplate,});
    const [rooms, setRooms] = useState<Room[]>([]);
    const [selectedMaterials, setSelectedMaterials] = useState<Material[]>([]);


    const selects = [
        {
            name: "type",
            label: "Type de matériel",
            options: [
                { label: "Tous", value: "" },
                { label: "UC", value: "UC" },
                { label: "Écran", value: "ECRAN" },
                { label: "Vidéoprojecteur", value: "PROJECTEUR" },
            ],
        },
        {
            name: "room",
            label: "Salle actuelle",
            options: [
                { label: "Toutes", value: "" },
                ...rooms.map((room) => ({
                    label: room.nom,
                    value: String(room.id),
                })),
            ],
        },
    ];

    const initialFilters: Record<string, string> = Object.fromEntries(
        selects.map(({ name }) => [name, ""])
    );

    const columns = [
        { key: "nom", label: "Nom" },
        { key: "marque", label: "Marque" },
        { key: "modele", label: "Modèle" },
        { key: "typeMateriel", label: "Type" }
    ] satisfies Column<Material>[];

    const {search, filters, items: materials, loading, page, totalPages, setPage, setSearch, handleFilterChange, resetFilters} = usePaginatedList({
        load: materialApi.list,
        initialFilters,
        pageSize: 10,
        errorMessage: "Impossible de charger les matériels",
    });

    useEffect(() => {
        if (!open) {
            setValues({ ...materialAssignmentTemplate });
            setSelectedMaterials([]);
            return;
        }
        async function loadRooms() {
            try {
                const response = await roomApi.list();
                setRooms(response.content);
            } catch {
                notify("Impossible de charger les salles", "danger");
            }
        }

        loadRooms();

        const initial: MaterialAssignmentForm = { ...materialAssignmentTemplate };

        setValues(initial);
    }, [open]);

    function handleChange(event: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) {
        const { name, value } = event.target;

        if (name === "salle_id") {
            setValues({
                salleId: Number(value),
                materielsId: [],
            });
            setSelectedMaterials([]);
            return;
        }

        setValues((previous) => ({
            ...previous,
            [name]: value,
        }));
    }

    async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
        event.preventDefault();
        try {
            const payload: MaterialAssignmentForm = {
                ...values,
                materielsId: selectedMaterials.map((material) => material.id),
            };
            await materialApi.assignments(payload);
            onSubmit(payload);
        } catch (error) {
            if (error instanceof ApiError) {
                switch (error.status) {
                    case 400: notify("Données invalides : " + error.message, "danger"); break;
                    case 500: notify("Erreur serveur, réessaie plus tard", "danger"); break;
                    default:  notify(error.message, "danger");
                }
            } else {
                notify("Impossible de joindre le serveur", "danger");
            }
        }
    }

    return (
        <Popup title={"Affecter du matériel"}
            open={open}
            onClose={onClose}
            blurBackground={blurBackground}
            actions={
                <button type="submit" form="material-form" className="btn btn-primary">
                    {"Affecter"}
                </button>
            }
        >
            <form id="material-form" onSubmit={handleSubmit}>
                <div className="mb-4">
                    <label htmlFor="salle_id" className="form-label">
                        Salle de destination <span className="text-danger">*</span>
                    </label>

                    <select
                        id="salle_id"
                        name="salle_id"
                        className="form-select"
                        value={values.salleId || ""}
                        onChange={handleChange}
                        required
                    >
                        <option value="">Choisir une salle</option>

                        {rooms.map((room) => (
                            <option key={room.id} value={room.id}>
                                {room.nom}
                            </option>
                        ))}
                    </select>

                    <div className="form-text">
                        Seuls les matériels disponibles hors de cette salle peuvent être affectés.
                    </div>
                </div>

                <Filters
                    embedded
                    search={search}
                    searchPlaceholder="Nom, numéro de série ou marque..."
                    selects={selects}
                    values={filters}
                    onSearchChange={setSearch}
                    onFilterChange={handleFilterChange}
                    onReset={resetFilters}
                />
                <Table
                    columns={columns}
                    data={materials}
                    currentPage={page}
                    totalPages={totalPages}
                    onPageChange={setPage}
                    loading={loading}
                    selection={{
                        selected: selectedMaterials,
                        getId: (material) => material.id,
                        onChange: setSelectedMaterials,
                    }}
                />
                {selectedMaterials.length > 0 && (
                    <button
                        type="button"
                        className="btn btn-primary"
                        onClick={() => console.log(selectedMaterials)}
                    >
                        Agir sur {selectedMaterials.length} matériel(s)
                    </button>
                )}
            </form>
        </Popup>
    );
}
