import { useState } from "react";

type PopupProps = {
    title: string;
    children: React.ReactNode;
    trigger: React.ReactNode;
    actions?: React.ReactNode;
};

export default function Popup({ title, children, trigger, actions }: PopupProps) {
    const [show, setShow] = useState(false);

    return (
        <>
            <div onClick={() => setShow(true)}>
                {trigger}
            </div>

            {show && (
                <>
                    <div className="modal fade show d-block" tabIndex={-1} role="dialog">
                        <div className="modal-dialog modal-dialog-centered">
                            <div className="modal-content">
                                <div className="modal-header">
                                    <h5 className="modal-title">
                                        {title}
                                    </h5>
                                    <button type="button" className="btn-close" onClick={() => setShow(false)}/>
                                </div>

                                <div className="modal-body">
                                    {children}
                                </div>

                                <div className="modal-footer">
                                    <button type="button" className="btn btn-secondary" onClick={() => setShow(false)}>
                                        Annuler
                                    </button>

                                    {actions}
                                </div>

                            </div>
                        </div>
                    </div>

                    <div className="modal-backdrop fade show" onClick={() => setShow(false)}/>
                </>
            )}
        </>
    );
}