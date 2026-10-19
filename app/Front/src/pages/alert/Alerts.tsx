import { useState } from "react";
import PageHeader from "../../components/layout/PageHeader.tsx";
import Filters from "../../components/generic/Filters.tsx";
import Table, { type Column } from "../../components/generic/display/Table.tsx";
import { alertApi } from "../../api";
import usePaginatedList from "../../hooks/usePaginatedList.ts";
import { useAuth } from "../../context/AuthContext.tsx";
import type {
    Alert,
    AlertStatut,
    AlertUpdateForm,
} from "../../types/Alert.ts";

const statutLabels: Record<AlertStatut, string> = {
    EN_ATTENTE: "En attente",
    TRAITEE: "Traitée",
    ANNULEE: "Annulée",
};

const statutClasses: Record<AlertStatut, string> = {
    EN_ATTENTE: "text-bg-warning",
    TRAITEE: "text-bg-success",
    ANNULEE: "text-bg-secondary",
};

function formatDate(date: Date): string {
    const value = new Date(date);

    if (Number.isNaN(value.getTime())) {
        return "—";
    }

    return value.toLocaleString("fr-FR", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
    });
}

export default function Alerts() {
    const { user } = useAuth();

    const [updatingId, setUpdatingId] = useState<number | null>(null);
    const [errorMessage, setErrorMessage] = useState("");

    const selects = [
        {
            name: "statut",
            label: "Statut",
            options: [
                { label: "Tous", value: "" },
                { label: "En attente", value: "EN_ATTENTE" },
                { label: "Traitée", value: "TRAITEE" },
                { label: "Annulée", value: "ANNULEE" },
            ],
        },
    ];

    const initialFilters: Record<string, string> = {
        statut: "",
    };

    const {
        filters,
        items: alerts,
        loading,
        page,
        totalPages,
        totalElements,
        setPage,
        handleFilterChange,
        resetFilters,
        reload,
    } = usePaginatedList({
        load: alertApi.list,
        initialFilters,
        pageSize: 10,
        errorMessage: "Impossible de charger les alertes",
    });

    async function handleStatusChange(
        alert: Alert,
        statut: AlertStatut,
    ) {
        if (!user?.id) {
            setErrorMessage(
                "Impossible de récupérer l'utilisateur connecté.",
            );
            return;
        }

        const update: AlertUpdateForm = {
            resolveurId: Number(user.id),
            dateResolution: new Date(),
            statut,
        };

        setUpdatingId(alert.id);
        setErrorMessage("");

        try {
            await alertApi.update(alert.id, update);
            await reload();
        } catch {
            setErrorMessage(
                "Impossible de mettre à jour le statut de l'alerte.",
            );
        } finally {
            setUpdatingId(null);
        }
    }

    const columns = [
        {
            label: "Matériel",
            render: (alert: Alert) => (
                <div className="d-flex flex-wrap gap-1">
                    {alert.materielsId.length > 0 ? (
                        alert.materielsId.map((materielId) => (
                            <span
                                key={materielId}
                                className="badge text-bg-light border"
                            >
                                #{materielId}
                            </span>
                        ))
                    ) : (
                        "—"
                    )}
                </div>
            ),
        },
        {
            label: "Signalement",
            render: (alert: Alert) => (
                <div>
                    <div className="fw-semibold">
                        {alert.typeProbleme}
                    </div>

                    <div className="text-body-secondary text-wrap">
                        {alert.description}
                    </div>

                    <small className="text-body-secondary">
                        {formatDate(alert.dateSignalement)}
                    </small>
                </div>
            ),
        },
        {
            label: "Statut",
            render: (alert: Alert) => (
                <span
                    className={`badge ${statutClasses[alert.statut]}`}
                >
                    {statutLabels[alert.statut]}
                </span>
            ),
        },
        {
            label: "Actions",
            render: (alert: Alert) => (
                <div className="d-flex gap-2">
                    {alert.statut === "EN_ATTENTE" && (
                        <>
                            <button
                                type="button"
                                className="btn btn-sm btn-outline-primary"
                                disabled={updatingId !== null}
                                onClick={() =>
                                    handleStatusChange(alert, "TRAITEE")
                                }
                            >
                                {updatingId === alert.id
                                    ? "Traitement..."
                                    : "Valider"}
                            </button>

                            <button
                                type="button"
                                className="btn btn-sm btn-outline-danger"
                                disabled={updatingId !== null}
                                onClick={() =>
                                    handleStatusChange(alert, "ANNULEE")
                                }
                            >
                                Refuser
                            </button>
                        </>
                    )}
                </div>
            ),
        },
    ] satisfies Column<Alert>[];

    return (
        <>
            <PageHeader
                title="Alertes"
                description="Consulter et traiter les signalements de matériels."
            />

            <Filters
                embedded
                selects={selects}
                values={filters}
                onFilterChange={handleFilterChange}
                onReset={resetFilters}
            />

            {errorMessage && (
                <div
                    className="alert alert-danger mt-3"
                    role="alert"
                >
                    {errorMessage}
                </div>
            )}

            <Table
                columns={columns}
                data={alerts}
                currentPage={page}
                totalPages={totalPages}
                totalElements={totalElements}
                onPageChange={setPage}
                loading={loading}
            />
        </>
    );
}