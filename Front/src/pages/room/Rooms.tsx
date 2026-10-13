import PageHeader from "../../components/layout/PageHeader.tsx";

export default function Rooms() {
    return (
        <PageHeader
            title="Salles"
            description="Retrouvez les équipements et la disponibilité des salles."
            actions={[
                {
                    label: "Ajouter une salle",
                    icon: "bi-plus-lg",
                    variant: "primary",
                    onClick: () => console.log("Ajouter"),
                },
            ]}
        />
    );
}