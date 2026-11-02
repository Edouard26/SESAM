import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import PageHeader from "../../components/layout/PageHeader.tsx";
import { processApi } from "../../api";
import type { Processus } from "../../types/Processus";
import { routes } from "../../router/routes";

export default function Process() {
    console.log("Processus component mounted");
    const { id } = useParams<{ id: string }>();

    console.log("Material ID:", id);

    const [processes, setProcesses] = useState<Processus[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        let cancelled = false;

        const loadProcesses = async () => {
            if (!id || Number.isNaN(Number(id))) {
                setError("Identifiant du matériel invalide.");
                setLoading(false);
                return;
            }

            try {
                setLoading(true);
                setError(null);

                const response = await processApi.list({
                    materielId: Number(id),
                });

                if (!cancelled) {
                    setProcesses(response.content);
                }
            } catch {
                if (!cancelled) {
                    setError("Impossible de récupérer les processus.");
                }
            } finally {
                if (!cancelled) {
                    setLoading(false);
                }
            }
        };

        loadProcesses();

        return () => {
            cancelled = true;
        };
    }, [id]);

    return (
        <div className="container-fluid">
            <PageHeader
                title="Processus"
                description="Liste des processus associés à cette unité centrale."
                back
            />

            {loading ? (
                <div className="d-flex justify-content-center py-5">
                    <div className="spinner-border text-primary" role="status">
                        <span className="visually-hidden">Chargement...</span>
                    </div>
                </div>
            ) : error ? (
                <div className="alert alert-danger">{error}</div>
            ) : processes.length === 0 ? (
                <div className="alert alert-info">
                    Aucun processus associé à cette unité centrale.
                </div>
            ) : (
                <div className="list-group">
                    {processes.map((process) => (
                        <div
                            key={process.id}
                            className="list-group-item d-flex align-items-center"
                        >
                            <i className="bi bi-cpu me-3 text-secondary" />
                            {process.nom}
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}