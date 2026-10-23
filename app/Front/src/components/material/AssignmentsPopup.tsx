import {useEffect, useRef, useState} from "react";
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
    onClose: () => void;
    blurBackground?: boolean;
    onSubmit: (material: MaterialAssignmentForm) => void;
};

export default function AssignmentsPopup({ onClose, blurBackground, onSubmit }: Props) {
    const {notify} = useNotification();
    const [values, setValues] = useState<MaterialAssignmentForm>({...materialAssignmentTemplate,});
    const [rooms, setRooms] = useState<Room[]>([]);
    const [selectedMaterials, setSelectedMaterials] = useState<Material[]>([]);
    const formRef = useRef<HTMLFormElement>(null);
    const [canSubmit, setCanSubmit] = useState(false);

    useEffect(() => {
        setCanSubmit((formRef.current?.checkValidity() ?? false) && selectedMaterials.length > 0);
    }, [values, selectedMaterials]);

    const selects = [
        {
            name: "typeMateriel",
            label: "Type de matériel",
            options: [
                { label: "Tous", value: "" },
                { label: "UC", value: "UC" },
                { label: "Écran", value: "ECRAN" },
                { label: "Vidéoprojecteur", value: "VIDEO_PROJECTEUR" },
            ],
        },
        {
            name: "salleId",
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

    const {
        search,
        filters,
        items: materials,
        loading,
        page,
        totalPages,
        totalElements,
        setPage,
        setSearch,
        handleFilterChange,
        resetFilters,
    } = usePaginatedList({
        load: (params) => materialApi.list({
            ...params,
            excludeSalleId: values.salleId,
        }),
        initialFilters,
        pageSize: 10,
        errorMessage: "Impossible de charger les matériels",
        enabled: values.salleId > 0,
        loadKey: values.salleId,
    });

    useEffect(() => {
        setValues({ ...materialAssignmentTemplate });
        setSelectedMaterials([]);

        async function loadRooms() {
            try {
                const response = await roomApi.list({
                    page: 0,
                    size: 1000,
                });
                setRooms(response.content);
            } catch {
                notify("Impossible de charger les salles", "danger");
            }
        }

        loadRooms();

        const initial: MaterialAssignmentForm = { ...materialAssignmentTemplate };

        setValues(initial);
    }, []);

    function handleChange(event: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) {
        const { name, value } = event.target;

        if (name === "salle_id") {
            setValues({
                salleId: Number(value),
                materielsId: [],
            });
            setSelectedMaterials([]);
            setPage(0);
            return;
        }

        setValues((previous) => ({
            ...previous,
            [name]: value,
        }));
    }

    async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
        event.preventDefault();
        if (!event.currentTarget.checkValidity() || selectedMaterials.length === 0) {
            notify("Sélectionnez une salle et au moins un matériel", "danger");
            return;
        }

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
            size={"modal-lg"}
            onClose={onClose}
            blurBackground={blurBackground}
            actions={
                <button type="submit" form="material-form" disabled={!canSubmit} className="btn btn-primary">
                    {"Affecter"}
                </button>
            }
        >
            <form id="material-form" onSubmit={handleSubmit} ref={formRef}>
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

                <div hidden={values.salleId < 0}>
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
                        totalElements={totalElements}
                        selection={{
                            selected: selectedMaterials,
                            getId: (material) => material.id,
                            onChange: setSelectedMaterials,
                        }}
                    />
                </div>
                {selectedMaterials.length > 0 && (
                    <a>{selectedMaterials.length} matériels</a>
                )}
            </form>
        </Popup>
    );
}
