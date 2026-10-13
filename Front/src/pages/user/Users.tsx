import PageHeader from "../../components/layout/PageHeader.tsx";

export default function Users() {
    return (
        <PageHeader
            title="Gestion des utilisateurs"
            description="Gérer les comptes, les rôles et leur état."
            actions={[
                {
                    label: "Ajouter un utilisateur",
                    icon: "bi-plus-lg",
                    variant: "primary",
                    onClick: () => console.log("Ajouter"),
                },
            ]}
        />
    );
}