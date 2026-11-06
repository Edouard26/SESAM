import { useCallback, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import PageHeader from "../../components/layout/PageHeader.tsx";
import { processApi } from "../../api";
import type { Processus } from "../../types/Processus";
import useNotification from "../../hooks/useNotification.ts";

export default function Process() {
    const { id } = useParams<{ id: string }>();
    const { notify } = useNotification();

    const [processes, setProcesses] = useState<Processus[]>([]);
    const [loading, setLoading] = useState(true);
    const [refreshing, setRefreshing] = useState(false);
    const [killingIds, setKillingIds] = useState<number[]>([]);

    const loadProcesses = useCallback(async (isRefresh = false) => {
        if (!id || Number.isNaN(Number(id))) {
            notify("Identifiant du matériel invalide.", "danger");
            setLoading(false);
            setRefreshing(false);
            return;
        }

        try {
            if (isRefresh) {
                setRefreshing(true);
            } else {
                setLoading(true);
            }

            const response = await processApi.list({
                uniteCentralId: Number(id),
            });

            setProcesses(response.content);

            if (isRefresh) {
                notify("Liste des processus actualisée.", "success");
            }
        } catch {
            notify("Impossible de récupérer les processus.", "danger");
        } finally {
            setLoading(false);
            setRefreshing(false);
        }
    }, [id, notify]);

    useEffect(() => {
        void loadProcesses();
    }, [loadProcesses]);

    const handleRefresh = () => {
        if (!loading && !refreshing) {
            void loadProcesses(true);
        }
    };

    const handleKill = (process: Processus) => {
        if (killingIds.includes(process.id)) {
            return;
        }

        setKillingIds((previous) => [...previous, process.id]);

        window.setTimeout(() => {
            setProcesses((previous) =>
                previous.filter((item) => item.id !== process.id)
            );

            setKillingIds((previous) =>
                previous.filter((id) => id !== process.id)
            );

            notify(`Le processus "${process.nom}" a été arrêté.`, "success");
        }, 700);
    };

    return (
        <div className="container-fluid">
            <PageHeader
                title="Processus"
                description="Liste des processus associés à cette unité centrale."
                back
            />

            <div className="d-flex justify-content-end mb-3">
                <button
                    type="button"
                    className="btn btn-outline-primary"
                    onClick={handleRefresh}
                    disabled={loading || refreshing}
                >
                    {refreshing ? (
                        <>
                            <span
                                className="spinner-border spinner-border-sm me-2"
                                role="status"
                                aria-hidden="true"
                            />
                            Actualisation...
                        </>
                    ) : (
                        <>
                            <i className="bi bi-arrow-clockwise me-2" />
                            Actualiser
                        </>
                    )}
                </button>
            </div>

            {loading ? (
                <div className="d-flex justify-content-center py-5">
                    <div className="spinner-border text-primary" role="status">
                        <span className="visually-hidden">Chargement...</span>
                    </div>
                </div>
            ) : processes.length === 0 ? (
                <div className="alert alert-info">
                    Aucun processus associé à cette unité centrale.
                </div>
            ) : (
                <div className="list-group">
                    {processes.map((process) => {
                        const isKilling = killingIds.includes(process.id);

                        return (
                            <div
                                key={process.id}
                                className="list-group-item d-flex align-items-center justify-content-between"
                            >
                                <div className="d-flex align-items-center">
                                    <i className="bi bi-cpu me-3 text-secondary" />
                                    <span>{process.nom}</span>
                                </div>

                                <button
                                    type="button"
                                    className="btn btn-sm btn-outline-danger"
                                    onClick={() => handleKill(process)}
                                    disabled={isKilling}
                                >
                                    {isKilling ? (
                                        <>
                                            <span
                                                className="spinner-border spinner-border-sm me-2"
                                                role="status"
                                                aria-hidden="true"
                                            />
                                            Arrêt...
                                        </>
                                    ) : (
                                        <>
                                            <i className="bi bi-x-circle me-2" />
                                            Arrêter
                                        </>
                                    )}
                                </button>
                            </div>
                        );
                    })}
                </div>
            )}
        </div>
    );
}