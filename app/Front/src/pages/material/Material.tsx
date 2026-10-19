import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { materialApi } from "../../api";
import type { Material as MaterialModel, MaterialType } from "../../types/Material.ts";
import PageHeader from "../../components/layout/PageHeader.tsx";
import { routes } from "../../router/routes.ts";

// Champs supplémentaires attendus de l'API pour la fiche détail
type MaterialDetail = MaterialModel & {
    statut?: string;
    salleId?: number;
    salleNom?: string;
    batiment?: string;
};

const typeLabels: Record<MaterialType, string> = {
    UniteCentral: "Unité centrale",
    Ecran: "Écran",
    VideoProjecteur: "Projecteur",
};

const formatDate = (value?: Date | string) =>
    value ? new Date(value).toLocaleDateString("fr-FR") : "—";

type RowProps = { label: string; children: React.ReactNode };

function Row({ label, children }: RowProps) {
    return (
        <div className="row py-2">
            <div className="col-4 col-md-2 text-secondary">{label}</div>
            <div className="col-8 col-md-10">{children}</div>
        </div>
    );
}

export default function Material() {
    const { id } = useParams<{ id: string }>();
    const [material, setMaterial] = useState<MaterialDetail | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!id) return;
        let cancelled = false;

        setLoading(true);
        setError(null);
        materialApi
            .get(Number(id))
            .then((data) => {
                if (!cancelled) setMaterial(data as MaterialDetail);
            })
            .catch(() => {
                if (!cancelled) setError("Impossible de charger ce matériel.");
            })
            .finally(() => {
                if (!cancelled) setLoading(false);
            });

        return () => {
            cancelled = true;
        };
    }, [id]);

    if (loading) {
        return (
            <div className="d-flex justify-content-center py-5">
                <div className="spinner-border text-primary" role="status" />
            </div>
        );
    }

    if (error || !material) {
        return (
            <>
                <PageHeader
                    title="Matériel introuvable"
                    description={error ?? "Ce matériel n'existe pas."}
                    back
                />
                <Link to={routes.materials}>Retour à la liste</Link>
            </>
        );
    }

    const isAvailable = (material.statut ?? "Disponible") === "Disponible";

    return (
        <>
            <PageHeader
                title={material.nom}
                description={`${material.numeroSerie} · Fiche descriptive`}
                back
            />

            <div className="card shadow-sm">
                <div className="card-header bg-body-tertiary d-flex justify-content-between align-items-center py-3 px-4">
                    <h2 className="h6 fw-bold mb-0">Informations générales</h2>
                    <span
                        className={`badge rounded-pill ${
                            isAvailable
                                ? "bg-success-subtle text-success-emphasis"
                                : "bg-warning-subtle text-warning-emphasis"
                        }`}
                    >
                        {material.statut ?? "Disponible"}
                    </span>
                </div>

                <div className="card-body px-4 py-3">
                    <Row label="N° de série">{material.numeroSerie}</Row>
                    <Row label="Nom">{material.nom}</Row>
                    <Row label="Type">{typeLabels[material.typeMateriel]}</Row>
                    <Row label="Marque">{material.marque}</Row>
                    <Row label="Modèle">{material.modele}</Row>
                    <Row label="Date d'ajout">{formatDate(material.dateAjout)}</Row>
                    <Row label="Fin de garantie">
                        {formatDate(material.dateFinGarantie)}
                    </Row>
                    {material.typeMateriel === "UniteCentral" && (
                        <>
                            <Row label="Processeur">{material.processeur ?? "—"}</Row>
                            <Row label="Carte graphique">
                                {material.carteGraphique ?? "—"}
                            </Row>
                        </>
                    )}

                    {material.typeMateriel === "Ecran" && (
                        <Row label="Taille">
                            {material.taille ? `${material.taille}"` : "—"}
                        </Row>
                    )}

                    <Row label="Salle">
                        {material.salleId ? (
                            <Link to={`${routes.rooms}/${material.salleId}`}>
                                {material.salleNom}
                            </Link>
                        ) : (
                            "Non affecté"
                        )}
                    </Row>
                    <Row label="Bâtiment">{material.batiment ?? "—"}</Row>
                </div>
            </div>
        </>
    );
}