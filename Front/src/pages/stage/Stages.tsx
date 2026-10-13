import PageHeader from "../../components/layout/PageHeader.tsx";

export default function Stages() {
    return (
        <PageHeader
            title="Gestion des stages"
            description="Stages chargés depuis SAGAIE et formations du centre."
            actions={[
                {
                    label: "Charger un stage",
                    icon: "bi-plus-lg",
                    variant: "primary",
                    onClick: () => console.log("Charger"),
                },
            ]}
        />
    );
}