import { useCallback, useEffect, useMemo, useState } from "react";
import Popup from "../generic/overlay/Popup";
import {
    materialAssignmentTemplate,
    type MaterialAssignmentForm,
    type Material,
} from "../../types/Material";
import { ApiError, materialApi, roomApi } from "../../api";
import useNotification from "../../hooks/useNotification.ts";
import type { Room } from "../../types/Room.ts";
import Filters from "../generic/Filters.tsx";
import Table, { type Column } from "../generic/display/Table.tsx";
import MultiSelect from "../generic/display/MultiSelect.tsx";
import usePaginatedList from "../../hooks/usePaginatedList.ts";

type Props = {
    onClose: () => void;
    blurBackground?: boolean;
    onSubmit: (material: MaterialAssignmentForm) => void;
};

const materialTypeOptions = [
    { label: "UC", value: "UC" },
    { label: "Écran", value: "ECRAN" },
    { label: "Vidéoprojecteur", value: "VIDEO_PROJECTEUR" },
];

async function searchMaterialTypes(query: string) {
    const normalizedQuery = query.trim().toLocaleLowerCase();

    return {
        content: materialTypeOptions.filter((option) =>
            option.label.toLocaleLowerCase().includes(normalizedQuery)
        ),
    };
}

const getRoomValue = (room: Room) => room.id;
const getRoomLabel = (room: Room) => room.nom;

// Affecte des matériels à une salle : on choisit la salle, puis les matériels (hors ceux déjà dans cette salle).
export default function AssignmentsPopup({ onClose, blurBackground, onSubmit }: Props) {
    const { notify } = useNotification();
    const [values, setValues] = useState<MaterialAssignmentForm>({
        ...materialAssignmentTemplate,
    });
    const [rooms, setRooms] = useState<Room[]>([]);
    const [selectedMaterials, setSelectedMaterials] = useState<Material[]>([]);

    const searchRooms = useCallback(async (query: string) => {
        const normalizedQuery = query.trim().toLocaleLowerCase();

        return {
            content: rooms.filter((room) =>
                room.nom.toLocaleLowerCase().includes(normalizedQuery)
            ),
        };
    }, [rooms]);

    const selects = useMemo(() => [
        {
            type: "multiselect" as const,
            name: "typeMateriel",
            label: "Type de matériel",
            searchOptions: searchMaterialTypes,
            maxSelected: 1,
            placeholder: "Tous les types",
        },
        {
            type: "multiselect" as const,
            name: "salleId",
            label: "Salle actuelle",
            searchOptions: async (query: string) => {
                const normalizedQuery = query.trim().toLocaleLowerCase();
                return {
                    content: rooms
                        .filter((room) => room.nom.toLocaleLowerCase().includes(normalizedQuery))
                        .map((room) => ({
                            label: room.nom,
                            value: String(room.id),
                        })),
                };
            },
            maxSelected: 1,
            placeholder: "Toutes les salles",
        },
    ], [rooms]);

    const initialFilters: Record<string, string> = Object.fromEntries(
        selects.map(({ name }) => [name, ""])
    );

    const columns = [
        { key: "nom", label: "Nom", sortKey: "nom" },
        { key: "marque", label: "Marque", sortKey: "marque" },
        { key: "modele", label: "Modèle", sortKey: "modele" },
        { key: "typeMateriel", label: "Type", sortKey: "typeMateriel" },
    ] satisfies Column<Material>[];

    const {
        search,
        filters,
        items: materials,
        loading,
        page,
        totalPages,
        totalElements,
        sort,
        setSort,
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
                const response = await roomApi.list({ page: 0, size: 1000 });
                setRooms(response.content);
            } catch {
                notify("Impossible de charger les salles", "danger");
            }
        }

        void loadRooms();
    }, [notify]);

    function handleRoomChange(selectedRoomIds: number[]) {
        setValues({
            salleId: selectedRoomIds[0] ?? -1,
            materielsId: [],
        });
        setSelectedMaterials([]);
        setPage(0);
    }

    async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
        event.preventDefault();

        if (values.salleId <= 0 || selectedMaterials.length === 0) {
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
                    case 400:
                        notify("Données invalides : " + error.message, "danger");
                        break;
                    case 500:
                        notify("Erreur serveur, réessaie plus tard", "danger");
                        break;
                    default:
                        notify(error.message, "danger");
                }
            } else {
                notify("Impossible de joindre le serveur", "danger");
            }
        }
    }

    const canSubmit = values.salleId > 0 && selectedMaterials.length > 0;

    return (
        <Popup
            title="Affecter du matériel"
            size="modal-lg"
            onClose={onClose}
            blurBackground={blurBackground}
            actions={
                <button
                    type="submit"
                    form="material-form"
                    disabled={!canSubmit}
                    className="btn btn-primary"
                >
                    Affecter
                </button>
            }
        >
            <form id="material-form" onSubmit={handleSubmit}>
                <div className="mb-4">
                    <label className="form-label">
                        Salle de destination <span className="text-danger">*</span>
                    </label>

                    <MultiSelect<Room, number>
                        searchOptions={searchRooms}
                        getOptionValue={getRoomValue}
                        getOptionLabel={getRoomLabel}
                        value={values.salleId > 0 ? [values.salleId] : []}
                        selectedOptions={rooms.filter((room) => room.id === values.salleId)}
                        onChange={handleRoomChange}
                        maxSelected={1}
                        placeholder="Choisir une salle"
                    />

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
                        sort={sort}
                        onSortChange={setSort}
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
