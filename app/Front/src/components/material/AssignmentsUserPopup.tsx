import { useCallback, useEffect, useState } from "react";
import Popup from "../generic/overlay/Popup";
import {
    materialApi,
    userApi,
    ApiError,
} from "../../api";
import useNotification from "../../hooks/useNotification.ts";
import type { User } from "../../types/User.ts";
import Filters from "../generic/Filters.tsx";
import Table, { type Column } from "../generic/display/Table.tsx";
import MultiSelect from "../generic/display/MultiSelect.tsx";
import usePaginatedList from "../../hooks/usePaginatedList.ts";
import type { Material, MaterialAssignmentUserForm } from "../../types/Material.ts";

type Props = {
    onClose: () => void;
    blurBackground?: boolean;
    onSubmit: (material: MaterialAssignmentUserForm) => void;
};

type AssignmentValues = {
    user: User | null;
    materielsId: number[];
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

const getUserValue = (user: User) => user.id;
const getUserLabel = (user: User) => `${user.prenom} ${user.nom} — ${user.email}`;

// Affecte des matériels à un stagiaire : on choisit le stagiaire, puis les matériels à lui attribuer.
export default function AssignmentsUserPopup({
                                                 onClose,
                                                 blurBackground,
                                                 onSubmit,
                                             }: Props) {
    const { notify } = useNotification();

    const [values, setValues] = useState<AssignmentValues>({
        user: null,
        materielsId: [],
    });
    const [users, setUsers] = useState<User[]>([]);
    const [selectedMaterials, setSelectedMaterials] = useState<Material[]>([]);

    const searchUsers = useCallback(async (query: string) => {
        const normalizedQuery = query.trim().toLocaleLowerCase();

        return {
            content: users.filter((user) =>
                [user.prenom, user.nom, user.email]
                    .some((value) => value.toLocaleLowerCase().includes(normalizedQuery))
            ),
        };
    }, [users]);

    useEffect(() => {
        async function loadUsers() {
            try {
                const response = await userApi.list({
                    page: 0,
                    size: 1000,
                    role: "STAGIAIRE",
                    active: "true",
                    sort: "nom,asc",
                });

                setUsers(response.content);
            } catch {
                notify("Impossible de charger les utilisateurs", "danger");
            }
        }

        void loadUsers();
    }, [notify]);

    const selects = [
        {
            type: "multiselect" as const,
            name: "type",
            label: "Type de matériel",
            searchOptions: searchMaterialTypes,
            maxSelected: 1,
            placeholder: "Tous les types",
        },
    ];

    const initialFilters: Record<string, string> = Object.fromEntries(
        selects.map(({ name }) => [name, ""])
    );

    const columns = [
        { key: "nom", label: "Nom", sortKey: "nom" },
        { key: "marque", label: "Marque", sortKey: "marque" },
        { key: "modele", label: "Modèle", sortKey: "modele" },
        { key: "numeroSerie", label: "Num série", sortKey: "numeroSerie" },
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
            excludeStagiaire: true,
            roomFromUserId: values.user?.id ?? 0,
        }),
        initialFilters,
        pageSize: 10,
        errorMessage: "Impossible de charger les matériels",
        enabled: values.user !== null,
        loadKey: values.user?.id,
    });

    function handleUserChange(selectedUserIds: number[]) {
        const user = users.find((item) => item.id === selectedUserIds[0]) ?? null;

        setValues({
            user,
            materielsId: [],
        });
        setSelectedMaterials([]);
        setPage(0);
    }

    function handleMaterialsChange(materials: Material[]) {
        setSelectedMaterials(materials);

        setValues((previous) => ({
            ...previous,
            materielsId: materials.map((material) => material.id),
        }));
    }

    async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
        event.preventDefault();

        if (values.user === null || values.materielsId.length === 0) {
            notify(
                "Sélectionnez un utilisateur et au moins un matériel",
                "danger"
            );
            return;
        }

        const payload: MaterialAssignmentUserForm = {
            stagiaireId: values.user.id,
            materielsId: values.materielsId,
        };

        try {
            await userApi.assignments(payload);
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

    const canSubmit = values.user !== null && values.materielsId.length > 0;

    return (
        <Popup
            title="Affecter du matériel à un utilisateur"
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
                        Utilisateur <span className="text-danger">*</span>
                    </label>

                    <MultiSelect<User, number>
                        searchOptions={searchUsers}
                        getOptionValue={getUserValue}
                        getOptionLabel={getUserLabel}
                        value={values.user ? [values.user.id] : []}
                        selectedOptions={values.user ? [values.user] : []}
                        onChange={handleUserChange}
                        maxSelected={1}
                        placeholder="Choisir un utilisateur"
                    />

                    <div className="form-text">
                        Sélectionnez un utilisateur pour afficher les matériels.
                    </div>
                </div>

                {values.user !== null && (
                    <>
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
                                onChange: handleMaterialsChange,
                            }}
                        />

                        <div className="text-end mt-3">
                            <span className="text-muted">
                                {values.materielsId.length} matériel
                                {values.materielsId.length > 1 ? "s" : ""} sélectionné
                                {values.materielsId.length > 1 ? "s" : ""}
                            </span>
                        </div>
                    </>
                )}
            </form>
        </Popup>
    );
}
