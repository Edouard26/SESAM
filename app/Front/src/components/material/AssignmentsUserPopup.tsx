import { useEffect, useRef, useState } from "react";
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
    const [canSubmit, setCanSubmit] = useState(false);

    const formRef = useRef<HTMLFormElement>(null);

    useEffect(() => {
        setCanSubmit(
            (formRef.current?.checkValidity() ?? false) &&
            values.user !== null &&
            values.materielsId.length > 0
        );
    }, [values, selectedMaterials]);

    useEffect(() => {
        async function loadUsers() {
            try {
                const response = await userApi.list({
                    page: 0,
                    size: 1000,
                });

                setUsers(response.content);
            } catch {
                notify("Impossible de charger les utilisateurs", "danger");
            }
        }

        loadUsers();
    }, [notify]);

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
    ];

    const initialFilters: Record<string, string> = Object.fromEntries(
        selects.map(({ name }) => [name, ""])
    );

    const columns = [
        { key: "nom", label: "Nom" },
        { key: "marque", label: "Marque" },
        { key: "modele", label: "Modèle" },
        { key: "numeroSerie", label: "Num série" },
        { key: "typeMateriel", label: "Type" },
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
        load: (params) =>
            materialApi.list({
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

    function handleChange(event: React.ChangeEvent<HTMLSelectElement>) {
        const { name, value } = event.target;

        if (name === "userId") {
            const user = users.find(
                (item) => item.id === Number(value)
            ) ?? null;

            setValues({
                user,
                materielsId: [],
            });

            setSelectedMaterials([]);
            setPage(0);
            return;
        }

        // Les autres filtres sont gérés par Filters.
        setValues((previous) => ({
            ...previous,
            [name]: value,
        }));
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

        if (
            !event.currentTarget.checkValidity() ||
            values.user === null ||
            values.materielsId.length === 0
        ) {
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
            <form
                id="material-form"
                onSubmit={handleSubmit}
                ref={formRef}
            >
                <div className="mb-4">
                    <label htmlFor="userId" className="form-label">
                        Utilisateur <span className="text-danger">*</span>
                    </label>

                    <select
                        id="userId"
                        name="userId"
                        className="form-select"
                        value={values.user?.id ?? ""}
                        onChange={handleChange}
                        required
                    >
                        <option value="">Choisir un utilisateur</option>

                        {users.map((user) => (
                            <option key={user.id} value={user.id}>
                                {user.prenom} {user.nom} — {user.email}
                            </option>
                        ))}
                    </select>

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