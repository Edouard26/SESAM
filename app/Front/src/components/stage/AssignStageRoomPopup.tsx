import { useEffect, useState } from "react";
import Popup from "../generic/overlay/Popup";
import { ApiError, roomApi, stageApi } from "../../api";
import useNotification from "../../hooks/useNotification";
import type { Stage } from "../../types/Stage";
import type { Room } from "../../types/Room";

type Props = {
    stage: Stage;
    onClose: () => void;
    blurBackground?: boolean;
    onSubmit: () => void;
};

export default function AssignStageRoomPopup({
                                                 stage,
                                                 onClose,
                                                 blurBackground,
                                                 onSubmit,
                                             }: Props) {
    const { notify } = useNotification();

    const [rooms, setRooms] = useState<Room[]>([]);
    const [roomId, setRoomId] = useState(-1);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        async function loadRooms() {
            try {
                const response = await roomApi.list({
                        selectedStageId: stage.id,
                        typeSalle: "COURS"
                    });
                // FILTRER PAR NON ASSIGNES
                setRooms(response.content);
            } catch {
                notify("Impossible de charger les salles", "danger");
            }
        }

        loadRooms();
    }, []);

    const canSubmit = roomId > 0 && !loading;

    async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
        event.preventDefault();
        if (!canSubmit) return;

        setLoading(true);

        try {
            await stageApi.update(stage.id, { salleId: roomId });

            notify(`Salle affectée au stage ${stage.code}`, "success");
            onSubmit();
        } catch (error) {
            if (error instanceof ApiError) {
                notify(
                    `Impossible d'affecter la salle : ${error.message}`,
                    "danger",
                );
            } else {
                notify(
                    "Impossible de joindre le serveur pour affecter la salle",
                    "danger",
                );
            }
        } finally {
            setLoading(false);
        }
    }

    return (
        <Popup
            title={`Affecter une salle au stage ${stage.code}`}
            size="modal-md"
            onClose={onClose}
            blurBackground={blurBackground}
            actions={
                <button
                    type="submit"
                    form="assign-stage-room-form"
                    disabled={!canSubmit}
                    className="btn btn-primary"
                >
                    {loading ? (
                        <>
                            <span
                                className="spinner-border spinner-border-sm me-2"
                                aria-hidden="true"
                            />
                            Affectation...
                        </>
                    ) : (
                        "Affecter"
                    )}
                </button>
            }
        >
            <form id="assign-stage-room-form" onSubmit={handleSubmit}>
                <div className="mb-3">
                    {rooms && rooms.length > 0 && (
                        <>
                            <label htmlFor="room_id" className="form-label">
                                Salle de destination{" "}
                                <span className="text-danger">*</span>
                            </label>

                            <select
                                id="room_id"
                                className="form-select"
                                value={roomId > 0 ? roomId : ""}
                                required
                                onChange={(event) =>
                                    setRoomId(
                                        event.target.value
                                            ? Number(event.target.value)
                                            : -1,
                                    )
                                }
                            >
                                <option value="">Choisir une salle</option>
                                {rooms.map((room) => (
                                    <option key={room.id} value={room.id}>
                                        {room.nom}
                                    </option>
                                ))}
                            </select>

                            <div className="form-text">
                                Le stage sera affecté à la salle sélectionnée.
                            </div>
                        </>
                    )}

                </div>
            </form>
        </Popup>
    );
}