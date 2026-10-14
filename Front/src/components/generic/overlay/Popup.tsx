import { useEffect } from "react";
import { createPortal } from "react-dom";

type PopupProps = {
    title: string;
    open: boolean;
    onClose: () => void;
    children: React.ReactNode;
    actions?: React.ReactNode;
    blurBackground?: boolean;
};

export default function Popup({title, open, onClose, children, actions, blurBackground = true }: PopupProps) {
    useEffect(() => {
        if (!open) {
            return;
        }

        function handleKeyDown(event: KeyboardEvent) {
            if (event.key === "Escape") {
                onClose();
            }
        }

        window.addEventListener("keydown", handleKeyDown);
        return () => window.removeEventListener("keydown", handleKeyDown);
    }, [open, onClose]);

    if (!open) {
        return null;
    }

    return createPortal(
        <>
            <div className="popup-backdrop" aria-hidden="true" onClick={onClose}
                style={{
                    position: "fixed",
                    inset: 0,
                    zIndex: 1050,
                    backgroundColor: "rgba(0, 0, 0, 0.3)",
                    backdropFilter: blurBackground ? "blur(5px)" : "none",
                    WebkitBackdropFilter: blurBackground ? "blur(5px)" : "none",
                }}
            />

            <div className="modal fade show d-block" role="dialog" aria-modal="true" aria-labelledby="popup-title"
                style={{ zIndex: 1055 }}
                tabIndex={-1}
                onClick={(event) => {
                    if (event.target === event.currentTarget) {
                        onClose();
                    }
                }}
            >
                <div className="modal-dialog modal-dialog-centered">
                    <div className="modal-content">
                        <div className="modal-header">
                            <h5 id="popup-title" className="modal-title">
                                {title}
                            </h5>
                            <button type="button" className="btn-close" aria-label="Fermer" onClick={onClose}/>
                        </div>

                        <div className="modal-body">{children}</div>

                        <div className="modal-footer">
                            <button type="button" className="btn btn-secondary" onClick={onClose}>
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
