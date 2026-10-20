import PageHeader from "../../components/layout/PageHeader.tsx";
import Filters from "../../components/generic/Filters.tsx";
import Table, {type Column} from "../../components/generic/display/Table.tsx";
import { useState } from "react";
import { userApi } from "../../api";
import { type User, getUserFullName } from "../../types/User.ts";
import { Role } from "../../types/Role.ts";
import Badge from "../../components/generic/display/Badge.tsx";
import UserPopup from "../../components/user/UserPopup.tsx";
import usePaginatedList from "../../hooks/usePaginatedList.ts";
import ImportUsersPopup from "../../components/user/ImportUsersPopup.tsx";

export default function Users() {
    const [showUserPopup, setShowUserPopup] = useState(false);
    const [selectedUser, setSelectedUser] = useState<User | undefined>();
    const [showImportPopup, setShowImportPopup] = useState(false);

    const selects = [
        {
            name: "role",
            label: "Rôle",
            options: [
                { label: "Tous", value: "" },
                ...Object.keys(Role)
                    .filter((key) => isNaN(Number(key)))
                    .map((key) => ({ label: key, value: key })),
            ],
        },
        {
            name: "active",
            label: "État",
            options: [
                { label: "Tous", value: "" },
                { label: "Actif", value: "true" },
                { label: "Inactif", value: "false" },
            ],
        },
    ];

    const initialFilters: Record<string, string> = Object.fromEntries(
        selects.map(({ name }) => [name, ""])
    );

    const {search, filters, items: users, loading, page, totalPages, totalElements, setPage, setSearch, handleFilterChange, resetFilters, reload,} = usePaginatedList({
        load: userApi.list,
        initialFilters,
        pageSize: 10,
        errorMessage: "Impossible de charger les utilisateurs",
    });

    const toggleActive = async (user: User) => {
        try {
            await userApi.update(user.id, { active: !user.active });
            reload();
        } catch {
            alert(`Impossible de ${user.active ? "désactiver" : "activer"} cet utilisateur`);
        }
    };

    const columns = [
        {
            label: "Utilisateur",
            render: (user: User) => (
                <div>
                    <div className="fw-semibold">{getUserFullName(user)}</div>
                    <small className="text-muted">{user.nid}</small>
                </div>
            ),
        },
        { key: "email", label: "Adresse e-mail" },
        {
            label: "Rôle",
            render: (user: User) => <Badge variant="primary">{user.role}</Badge>,
        },
        {
            label: "Grade",
            render: (user: User) => (
                <Badge variant="secondary">{user.grade.replaceAll("_", " ")}</Badge>
            ),
        },
        {
            label: "État",
            render: (user: User) => (
                <span
                    className={`badge rounded-pill ${
                        user.active
                            ? "bg-success-subtle text-success"
                            : "bg-secondary-subtle text-secondary"
                    }`}
                >
                    {user.active ? "Actif" : "Inactif"}
                </span>
            ),
        },
        {
            label: "Actions",
            render: (user: User) => (
                <div className="d-flex gap-2">
                    <button
                        type="button"
                        className="btn btn-sm btn-outline-secondary"
                        onClick={() => {
                            setSelectedUser(user);
                            setShowUserPopup(true);
                        }}
                    >
                        <i className="bi bi-pencil me-1" />
                        Modifier
                    </button>
                    <button
                        type="button"
                        className={`btn btn-sm ${user.active ? "btn-outline-danger" : "btn-outline-secondary"}`}
                        onClick={() => toggleActive(user)}
                    >
                        {user.active ? "Désactiver" : "Activer"}
                    </button>
                </div>
            ),
        },
    ] satisfies Column<User>[];

    return (
        <>
            {showImportPopup && (
                <ImportUsersPopup
                    onClose={() => setShowImportPopup(false)}
                    onSubmit={() => {
                        setShowImportPopup(false);
                        reload();
                    }}
                />
            )}
            {showUserPopup && (
                <UserPopup
                    user={selectedUser}
                    onClose={() => setShowUserPopup(false)}
                    onSubmit={(user) => {
                        setShowUserPopup(false);
                        reload();
                        setSearch(user.email);
                    }}
                />
            )}

            <PageHeader
                title="Gestion des utilisateurs"
                description="Gérer les comptes, les rôles et leur état."
                actions={[
                    {
                        label: "Ajouter un utilisateur",
                        icon: "bi-plus-lg",
                        variant: "primary",
                        onClick: () => {
                            setSelectedUser(undefined);
                            setShowUserPopup(true);
                        },
                    },
                    {
                        label: "Importer",
                        icon: "bi-file-earmark-arrow-down",
                        onClick: () => setShowImportPopup(true),
                    },
                ]}
            />
            <Filters
                embedded
                search={search}
                searchPlaceholder="Nom, prénom ou adresse e-mail..."
                selects={selects}
                values={filters}
                onSearchChange={setSearch}
                onFilterChange={handleFilterChange}
                onReset={resetFilters}
            />

            <Table
                columns={columns}
                data={users}
                currentPage={page}
                totalPages={totalPages}
                totalElements={totalElements}
                onPageChange={setPage}
                loading={loading}
            />
        </>
    );
}