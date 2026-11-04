import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import PageHeader from "../../components/layout/PageHeader.tsx";
import { processApi } from "../../api";
import type { Processus } from "../../types/Processus";
import useNotification from "../../hooks/useNotification.ts";

// Liste des processus d'une unité centrale (l'id du matériel vient de l'URL)
export default function Process() {
    const { id } = useParams<{ id: string }>();
    const { notify } = useNotification();

    const [processes, setProcesses] = useState<Processus[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        let cancelled = false;

        const loadProcesses = async () => {
            if (!id || Number.isNaN(Number(id))) {
                notify("Identifiant du matériel invalide.", "danger");
                setLoading(false);
                return;
            }

            try {
                setLoading(true);

                const response = await processApi.list({
                    uniteCentralId: Number(id),
                });

                if (!cancelled) {
                    setProcesses(response.content);
                }
            } catch {
                if (!cancelled) {
                    notify("Impossible de récupérer les processus.", "danger");
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