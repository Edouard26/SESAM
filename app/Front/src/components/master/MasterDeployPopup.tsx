
import { useEffect, useState, type FormEvent } from "react";
import { roomApi } from "../../api";
import type { Master } from "../../types/Master.ts";
import type { Room } from "../../types/Room.ts";
import useNotification from "../../hooks/useNotification.ts";
import Popup from "../generic/overlay/Popup.tsx";

type MasterDeployPopupProps = {
    master: Master;
    onClose: () => void;
    onSubmit: () => void;
};

// Popup permettant de sélectionner une salle et de lancer sa réinstallation
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
    const [error, setError] = useState<string | null>(null);

    const selectedRoom = rooms.find(
        (room) => String(room.id) === selectedRoomId
    );

    // Charge les salles de cours
    useEffect(() => {
        let cancelled = false;

        async function loadRooms() {
            setLoading(true);
            setError(null);

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
                    setError("Impossible de charger les salles.");
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
    }, []);

    // Lance la réinstallation de la salle sélectionnée
    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();

        if (!selectedRoom || reinstalling) return;

        setReinstalling(true);

        try {
            await roomApi.reinstall(selectedRoom.id);

            notify(
                `Réinstallation de la salle ${selectedRoom.nom} lancée.`,
                "success"
            );

            onSubmit();
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
        <Popup
            title="Plaquer un master"
            onClose={onClose}
            size="modal-md"
            confirmOnSubmit
            confirmationTitle="Confirmer le plaquage ?"
            confirmationMessage={
                `Tous les postes de la salle ${selectedRoom?.nom ?? ""} ` +
                `seront réinstallés avec le master ${master.nom}.`
            }
            confirmationLabel="Confirmer le plaquage"
            actions={
                <button
                    type="submit"
                    form="master-deploy-form"
                    className="btn btn-primary"
                    disabled={!selectedRoom || loading || reinstalling}
                >
                    <i className="bi bi-pc-display me-2" />
                    Plaquer
                </button>
            }
        >
            <form id="master-deploy-form" onSubmit={handleSubmit}>
                <div className="mb-3">
                    <label className="form-label">Master</label>
                    <input
                        type="text"
                        className="form-control"
                        value={master.nom}
                        disabled
                    />
                </div>

                <div className="mb-3">
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
                        onChange={(event) => setSelectedRoomId(event.target.value)}
                        required
                    >
                        <option value="" disabled>
                            {loading
                                ? "Chargement des salles..."
                                : error
                                    ? "Impossible de charger les salles"
                                    : "Sélectionner une salle"}
                        </option>

                        {rooms.map((room) => (
                            <option key={room.id} value={room.id}>
                                {room.nom} - {room.nomBatiment}
                            </option>
                        ))}
                    </select>

                    {loading && (
                        <div className="form-text">
                            Chargement des salles...
                        </div>
                    )}

                    {error && (
                        <div className="text-danger small mt-2">
                            {error}
                        </div>
                    )}

                    {!loading && !error && rooms.length === 0 && (
                        <div className="text-body-secondary small mt-2">
                            Aucune salle de cours disponible.
                        </div>
                    )}
                </div>

                <div className="alert alert-warning mb-0">
                    <i className="bi bi-exclamation-triangle me-2" />
                    La réinstallation concerne tous les postes de la salle
                    sélectionnée.
                </div>
            </form>
        </Popup>
    );
}
