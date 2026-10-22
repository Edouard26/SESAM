import { useEffect } from "react";
import { createPortal } from "react-dom";

type PopupProps = {
    title: string;
    onClose: () => void;
    children: React.ReactNode;
    actions?: React.ReactNode;
    blurBackground?: boolean;
    size?: "modal-sm" | "modal-md" | "modal-lg" | "modal-xl";
};

export default function Popup({
    title,
    onClose,
    children,
    actions,
    blurBackground = true,
    size = "modal-md",
}: PopupProps) {
    useEffect(() => {
        const previousOverflow = document.body.style.overflow;
        document.body.style.overflow = "hidden";

        function handleKeyDown(event: KeyboardEvent) {
            if (event.key === "Escape") {
                onClose();
            }
        }

        window.addEventListener("keydown", handleKeyDown);

        return () => {
            document.body.style.overflow = previousOverflow;
            window.removeEventListener("keydown", handleKeyDown);
        };
    }, [onClose]);

    return createPortal(
        <>
            <div
                className="popup-backdrop"
                aria-hidden="true"
                onClick={onClose}
                style={{
                    position: "fixed",
                    inset: 0,
                    zIndex: 1050,
                    backgroundColor: "rgba(0, 0, 0, 0.3)",
                    backdropFilter: blurBackground ? "blur(5px)" : "none",
                    WebkitBackdropFilter: blurBackground ? "blur(5px)" : "none",
                }}
            />

            <div
                className="modal fade show d-block"
                role="dialog"
                aria-modal="true"
                aria-labelledby="popup-title"
                style={{
                    zIndex: 1055,
                    overflowY: "hidden",
                }}
                tabIndex={-1}
                onClick={(event) => {
                    if (event.target === event.currentTarget) onClose();
                }}
            >
                <div
                    className={`modal-dialog modal-dialog-centered ${size}`}
                    style={{ minHeight: "calc(100% - 1rem)" }}
                >
                    <div
                        className="modal-content"
                        style={{
                            maxHeight: "calc(100vh - 2rem)",
                        }}
                    >
                        <div className="modal-header flex-shrink-0">
                            <h5 id="popup-title" className="modal-title">
                                {title}
                            </h5>
                            <button
                                type="button"
                                className="btn-close"
                                aria-label="Fermer"
                                onClick={onClose}
                            />
                        </div>

                        <div
                            className="modal-body"
                            style={{
                                overflowY: "auto",
                                minHeight: 0,
                            }}
                        >
                            {children}
                        </div>

                        <div className="modal-footer flex-shrink-0">
                            <button
                                type="button"
                                className="btn btn-secondary"
                                onClick={onClose}
                            >
                                Annuler
                            </button>
                            {actions}
                        </div>
                    </div>
                </div>
            </div>
        </>,
        document.body,
    );
}