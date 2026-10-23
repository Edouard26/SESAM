
import { useEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";

type PopupProps = {
    title: string;
    onClose: () => void;
    children: React.ReactNode;
    actions?: React.ReactNode;
    blurBackground?: boolean;
    size?: "modal-sm" | "modal-md" | "modal-lg" | "modal-xl";
    confirmOnSubmit?: boolean;
    confirmationTitle?: string;
    confirmationMessage?: string;
    confirmationLabel?: string;
};

export default function Popup({
                                  title,
                                  onClose,
                                  children,
                                  actions,
                                  blurBackground = true,
                                  size = "modal-md",
                                  confirmOnSubmit = true,
                                  confirmationTitle = "Confirmation",
                                  confirmationMessage = "Voulez-vous vraiment valider ce formulaire ?",
                                  confirmationLabel = "Confirmer",
                              }: PopupProps) {
    const [showConfirmation, setShowConfirmation] = useState(false);
    const popupRef = useRef<HTMLDivElement>(null);
    const skipConfirmationRef = useRef(false);

    useEffect(() => {
        const previousOverflow = document.body.style.overflow;
        document.body.style.overflow = "hidden";

        function handleKeyDown(event: KeyboardEvent) {
            if (event.key === "Escape") {
                if (showConfirmation) {
                    setShowConfirmation(false);
                } else {
                    onClose();
                }
            }
        }

        window.addEventListener("keydown", handleKeyDown);

        return () => {
            document.body.style.overflow = previousOverflow;
            window.removeEventListener("keydown", handleKeyDown);
        };
    }, [onClose, showConfirmation]);

    function handleSubmitCapture(event: React.FormEvent<HTMLDivElement>) {
        if (!confirmOnSubmit || skipConfirmationRef.current) {
            skipConfirmationRef.current = false;
            return;
        }

        event.preventDefault();
        event.stopPropagation();
        setShowConfirmation(true);
    }

    function confirmSubmit() {
        setShowConfirmation(false);
        skipConfirmationRef.current = true;

        const form = popupRef.current?.querySelector("form");
        form?.requestSubmit();
    }

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
                ref={popupRef}
                className="modal fade show d-block"
                role="dialog"
                aria-modal="true"
                aria-labelledby="popup-title"
                style={{
                    zIndex: 1055,
                    overflowY: "hidden",
                }}
                tabIndex={-1}
                onSubmitCapture={handleSubmitCapture}
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

            {showConfirmation && (
                <>
                    <div
                        className="modal-backdrop show"
                        style={{
                            position: "fixed",
                            inset: 0,
                            zIndex: 1060,
                        }}
                        onClick={() => setShowConfirmation(false)}
                    />

                    <div
                        className="modal fade show d-block"
                        role="alertdialog"
                        aria-modal="true"
                        aria-labelledby="confirmation-title"
                        aria-describedby="confirmation-message"
                        style={{
                            zIndex: 1065,
                        }}
                    >
                        <div className="modal-dialog modal-dialog-centered">
                            <div className="modal-content border-0 shadow-lg">
                                <div className="modal-body text-center p-4">
                                    <h5
                                        id="confirmation-title"
                                        className="fw-semibold mb-2"
                                    >
                                        {confirmationTitle}
                                    </h5>

                                    <p
                                        id="confirmation-message"
                                        className="text-body-secondary mb-4"
                                    >
                                        {confirmationMessage}
                                    </p>

                                    <div className="d-flex justify-content-center gap-2">
                                        <button
                                            type="button"
                                            className="btn btn-secondary"
                                            onClick={() => setShowConfirmation(false)}
                                        >
                                            Annuler
                                        </button>

                                        <button
                                            type="button"
                                            className="btn btn-primary"
                                            onClick={confirmSubmit}
                                        >
                                            {confirmationLabel}
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </>
            )}
        </>,
        document.body,
    );
}
