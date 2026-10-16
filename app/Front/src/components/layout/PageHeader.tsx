import { useNavigate } from "react-router-dom";

type PageAction = {
    label: string;
    icon?: string;
    onClick: () => void;
    variant?: "primary" | "outline";
};

type PageHeaderProps = {
    title: string;
    description: string;
    actions?: PageAction[];
    back?: boolean;
};

export default function PageHeader({
                                       title,
                                       description,
                                       actions = [],
                                       back = false,
                                   }: PageHeaderProps) {
    const navigate = useNavigate();

    return (
        <div className="d-flex justify-content-between align-items-start gap-3 mb-4">
            <div>
                {back && (
                    <button
                        type="button"
                        className="btn btn-link text-decoration-none p-0 mb-2"
                        onClick={() => navigate(-1)}
                    >
                        <i className="bi bi-arrow-left me-2" />
                        Retour
                    </button>
                )}

                <h1 className="h3 fw-bold mb-1">{title}</h1>
                <p className="text-secondary mb-0">{description}</p>
            </div>

            <div className="d-flex flex-wrap gap-2">
                {actions.map((action) => (
                    <button
                        key={action.label}
                        type="button"
                        className={`btn ${
                            action.variant === "primary"
                                ? "btn-primary"
                                : "btn-outline-primary"
                        }`}
                        onClick={action.onClick}
                    >
                        {action.icon && (
                            <i className={`bi ${action.icon} me-2`} />
                        )}
                        {action.label}
                    </button>
                ))}
            </div>
        </div>
    );
}