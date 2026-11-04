import { useState } from "react";

type ConfirmationProps = {
    title?: string;
    message: string;
    onConfirm: () => void;
    trigger?: React.ReactNode;
    open?: boolean;
    onCancel?: () => void;
};

// Enveloppe un élément déclencheur (trigger) : au clic, demande confirmation avant d'exécuter onConfirm
export default function Confirmation({ title = "Confirmation", message, onConfirm, trigger, open, onCancel }: ConfirmationProps) {
    const [internalShow, setInternalShow] = useState(false);
    const show = open ?? internalShow;

    const setShow = (value: boolean) => {
        if (open === undefined) setInternalShow(value);
        else if (!value) onCancel?.();
    };

    const confirm = () => {
        onConfirm();
        setShow(false);
    };

    return (
        <>
            {trigger && (
                <div role="presentation" onClick={() => setShow(true)}>
                    {trigger}
                </div>
            )}

            {show && (
                <>
                    <div className="modal fade show d-block" tabIndex={-1} role="dialog">
                        <div className="modal-dialog modal-dialog-centered">
                            <div className="modal-content border-0 shadow-lg">
                                <div className="modal-body text-center p-4">
                                    <div className="mb-3">
                                        <div className="rounded-circle bg-danger bg-opacity-10 d-inline-flex align-items-center justify-content-center"
                                            style={{
                                                width: "64px",
                                                height: "64px"
                                            }}>
                                            <i className="bi bi-exclamation-triangle-fill text-danger fs-3" />
                                        </div>
                                    </div>

                                    <h5 className="fw-semibold mb-2">
                                        {title}
                                    </h5>

                                    <p className="text-body-secondary mb-4">
                                        {message}
                                    </p>

                                    <div className="d-flex justify-content-center gap-2">
                                        <button type="button" className="btn btn-secondary px-4" onClick={() => setShow(false)}>
                                            Annuler
                                        </button>

                                        <button type="button" className="btn btn-danger px-4" onClick={confirm}>
                                            Confirmer
                                        </button>
                                    </div>

                                </div>

                            </div>
                        </div>
                    </div>
                    <div role="presentation" className="modal-backdrop fade show" onClick={() => setShow(false)}/>
                </>
            )}
        </>
    );
}