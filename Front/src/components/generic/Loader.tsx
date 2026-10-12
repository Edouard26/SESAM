type LoaderProps = {
    size?: "sm" | "";
    text?: string;
};

export default function Loader({ size = "", text }: LoaderProps) {
    return (
        <div className="d-flex flex-column align-items-center gap-2">
            <div className={`spinner-border text-primary ${
                    size ? `spinner-border-${size}` : ""
                }`}
                role="status">
                <span className="visually-hidden">
                    Chargement...
                </span>
            </div>

            {text && (
                <span className="text-body-secondary small">
                    {text}
                </span>
            )}
        </div>
    );
}