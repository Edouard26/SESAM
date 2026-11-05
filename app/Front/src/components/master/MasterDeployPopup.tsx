
import { useEffect, useState } from "react";
import { roomApi } from "../../api";
import type { Master } from "../../types/Master.ts";
import type { Room } from "../../types/Room.ts";
import useNotification from "../../hooks/useNotification.ts";

type MasterDeployPopupProps = {
    master: Master;
    onClose: () => void;
    onSubmit: () => void;
};

// Popup de sélection d'une salle et de réinstallation pour un master
export default function MasterDeployPopup({
                                              master,
                                              onClose,
                                              onSubmit,
                                          }: MasterDeployPopupProps) {
    const { notify } = useNotification();

    const [rooms, setRooms] = useState<Room[]>([]);
    const [selectedRoomId, setSelectedRoomId] = useState("");
    const [loading, setLoading] = useState(true);
    const [reinstalling, setReinstalling] = useState(false);
    const [confirming, setConfirming] = useState(false);

    const selectedRoom = rooms.find(
        (room) => String(room.id) === selectedRoomId
    );

    // Charge les salles de cours
    useEffect(() => {
        let cancelled = false;

        async function loadRooms() {
            setLoading(true);

            try {
                const result = await roomApi.list({
                    page: 0,
                    size: 1000,
                    sort: "nom,asc",
                });

                if (!cancelled) {
                    setRooms(
                        result.content.filter(
                            (room) => room.typeSalle === "COURS"
                        )
                    );
                }
            } catch {
                if (!cancelled) {
                    notify("Impossible de charger les salles.", "danger");
                }
            } finally {
                if (!cancelled) {
                    setLoading(false);
                }
            }
        }

        void loadRooms();

        return () => {
            cancelled = true;
        };
    }, [notify]);

    // Lance la réinstallation de la salle sélectionnée
    async function reinstallRoom() {
        if (!selectedRoom || reinstalling) return;

        setReinstalling(true);

        try {
            await roomApi.reinstall(selectedRoom.id);

            notify(
                `Réinstallation de la salle ${selectedRoom.nom} lancée.`,
                "success"
            );

            onSubmit();
            onClose();
        } catch {
            notify(
                "Impossible de lancer la réinstallation de la salle.",
                "danger"
            );
        } finally {
            setReinstalling(false);
        }
    }

    return (
        <>
            <div
                className="modal fade show d-block"
                tabIndex={-1}
                role="dialog"
                aria-modal="true"
                aria-labelledby="master-deploy-title"
            >
                <div className="modal-dialog modal-dialog-centered">
                    <div className="modal-content">
                        <div className="modal-header">
                            <h5
                                className="modal-title"
                                id="master-deploy-title"
                            >
                                {confirming
                                    ? "Confirmer le plaquage"
                                    : "Plaquer un master"}
                            </h5>

                            <button
                                type="button"
                                className="btn-close"
                                aria-label="Fermer"
                                disabled={reinstalling}
                                onClick={onClose}
                            />
                        </div>

                        <div className="modal-body">
                            <p>
                                Master : <strong>{master.nom}</strong>
                            </p>

                            {!confirming ? (
                                <>
                                    <label
                                        htmlFor="master-deploy-room"
                                        className="form-label"
                                    >
                                        Salle de destination
                                    </label>

                                    <select
                                        id="master-deploy-room"
                                        className="form-select"
                                        value={selectedRoomId}
                                        onChange={(event) =>
                                            setSelectedRoomId(
                                                event.target.value
                                            )
                                        }
                                        disabled={loading}
                                    >
                                        <option value="">
                                            Sélectionner une salle
                                        </option>

                                        {rooms.map((room) => (
                                            <option
                                                key={room.id}
                                                value={room.id}
                                            >
                                                {room.nom} - {room.nomBatiment}
                                            </option>
                                        ))}
                                    </select>

                                    {loading && (
                                        <div className="form-text">
                                            Chargement des salles...
                                        </div>
                                    )}

                                    {!loading && rooms.length === 0 && (
                                        <div className="alert alert-info mt-3 mb-0">
                                            Aucune salle de cours disponible.
                                        </div>
                                    )}
                                </>
                            ) : (
                                <div className="alert alert-warning mb-0">
                                    <i className="bi bi-exclamation-triangle me-2" />
                                    Tous les postes de la salle{" "}
                                    <strong>{selectedRoom?.nom}</strong>{" "}
                                    seront réinstallés.
                                </div>
                            )}
                        </div>

                        <div className="modal-footer">
                            <button
                                type="button"
                                className="btn btn-outline-secondary"
                                disabled={reinstalling}
                                onClick={() => {
                                    if (confirming) {
                                        setConfirming(false);
                                    } else {
                                        onClose();
                                    }
                                }}
                            >
                                {confirming ? "Retour" : "Annuler"}
                            </button>

                            {!confirming ? (
                                <button
                                    type="button"
                                    className="btn btn-primary"
                                    disabled={
                                        !selectedRoomId ||
                                        loading ||
                                        reinstalling
                                    }
                                    onClick={() => setConfirming(true)}
                                >
                                    Continuer
                                </button>
                            ) : (
                                <button
                                    type="button"
                                    className="btn btn-primary"
                                    disabled={reinstalling}
                                    onClick={() => void reinstallRoom()}
                                >
                                    <i className="bi bi-arrow-repeat me-2" />
                                    {reinstalling
                                        ? "Réinstallation..."
                                        : "Confirmer le plaquage"}
                                </button>
                            )}
                        </div>
                    </div>
                </div>
            </div>

            <div className="modal-backdrop fade show" />
        </>
    );
}
