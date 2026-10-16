type NotificationProps = {
    message: string;
    variant: "success" | "danger" | "warning" | "info";
    duration: number;
    onClose: () => void;
};

export default function Notification({ message, variant, duration, onClose }: NotificationProps) {
    return (
        <div className={`alert alert-${variant} shadow position-relative overflow-hidden mb-0`} role="alert">
            <div className="d-flex align-items-center">
                <span className="flex-grow-1">
                    {message}
                </span>

                <button type="button" className="btn-close ms-3" aria-label="Fermer" onClick={onClose}/>
            </div>

            <div className="position-absolute bottom-0 start-0"
                style={{
                    height: "3px",
                    width: "0%",
                    backgroundColor: "var(--bs-emphasis-color)",
                    opacity: 0.35,
                    animation: `notification-progress ${duration}ms linear forwards`,
                }}
            />
        </div>
    );
}