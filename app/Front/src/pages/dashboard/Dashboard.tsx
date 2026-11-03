import PageHeader from "../../components/layout/PageHeader.tsx";
import { useState } from "react";
import AlertPopup from "../../components/alert/AlertPopup.tsx";
import { useAuth } from "../../context/AuthContext.tsx";
import { Role } from "../../types/Role.ts";
import StatsOverview from "../../components/dashboard/StatsOverview.tsx";

export default function Dashboard() {
    const { user } = useAuth();
    const [showAlertPopup, setShowAlertPopup] = useState(false);

    const canSendAlert = user && user.role <= Role.FORMATEUR;
    const canSeeStats = user && user.role >= Role.GESTIONNAIRE;

    return (
        <>
            <PageHeader
                title="Vue d’ensemble"
                description="Centre de formation."
                actions={
                    canSendAlert
                        ? [
                            {
                                label: "Envoyer une alerte",
                                icon: "bi bi-exclamation-triangle",
                                onClick: () => setShowAlertPopup(true),
                            },
                        ]
                        : []
                }
            />

            {canSeeStats && <StatsOverview />}

            {showAlertPopup && canSendAlert && (
                <AlertPopup
                    user={user}
                    onClose={() => setShowAlertPopup(false)}
                    onSubmit={() => setShowAlertPopup(false)}
                />
            )}
        </>
    );
}