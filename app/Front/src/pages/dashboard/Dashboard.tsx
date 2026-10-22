import PageHeader from "../../components/layout/PageHeader.tsx";
import { useState } from "react";
import AlertPopup from "../../components/alert/AlertPopup.tsx";
import { useAuth } from "../../context/AuthContext.tsx";
import { Role } from "../../types/Role.ts";

export default function Dashboard() {
    const { user } = useAuth();
    const [showAlertPopup, setShowAlertPopup] = useState(false);

    const canSendAlert = user && user.role <= Role.FORMATEUR;

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