import { useEffect, useRef, useState } from "react";
import Popup from "../generic/overlay/Popup";
import { useAuth } from "../../context/AuthContext";
import type { AlertForm } from "../../types/Alert";
import type { Material } from "../../types/Material";
import { ApiError, alertApi, materialApi, roomApi } from "../../api";
import type { Room } from "../../types/Room";
import { Role } from "../../types/Role";
import useNotification from "../../hooks/useNotification";
import type {User} from "../../types/User.ts";

type Props = {
    user: User;
    onClose: () => void;
    blurBackground?: boolean;
    onSubmit: (alert: AlertForm) => void;
};

// Formulaire de signalement d'un problème sur un ou plusieurs matériels, au nom de l'utilisateur connecté
export default function AlertPopup({ onClose, blurBackground, onSubmit }: Props) {
    const { user } = useAuth();
    const { notify } = useNotification();

    const [values, setValues] = useState<AlertForm>({
        signaleurId: user?.id ?? 0,
        dateSignalement: new Date(),
        typeProbleme: "",
        description: "",
        materielsId: [],
    });

    const [materials, setMaterials] = useState<Material[]>([]);
    const [rooms, setRooms] = useState<Room[]>([]);
    const [salleId, setSalleId] = useState("");
    const isFormateur = user?.role === Role.FORMATEUR;
    const [canSubmit, setCanSubmit] = useState(false);
    const formRef = useRef<HTMLFormElement>(null);

    useEffect(() => {
        setValues((previous) => ({
            ...previous,
            signaleurId: user?.id ?? 0,
        }));
    }, [user?.id]);

    // Le bouton d'envoi n'est actif que si le formulaire est valide
    useEffect(() => {
        setCanSubmit(
            formRef.current?.checkValidity() ?? false,
        );
    }, [values]);

    useEffect(() => {
        if (!isFormateur) return;

        roomApi.list({ page: 0, size: 1000, sort: "nom,asc" })
            .then((response) => setRooms(response.content))
            .catch(() => notify("Impossible de charger les salles", "danger"));
    }, [isFormateur, notify]);

    useEffect(() => {
        async function loadMaterials() {
            if (isFormateur && !salleId) {
                setMaterials([]);
                return;
            }

            try {
                const response = await materialApi.list({
                    page: 0,
                    size: 100,
                    sort: "nom,asc",
                    ...(isFormateur ? { salleId: Number(salleId) } : {}),
                });

                setMaterials(response.content);
                setValues((previous) => ({ ...previous, materielsId: [] }));
            } catch (error) {
                if (error instanceof ApiError) {
                    notify(error.message, "danger");
                } else {
                    notify("Impossible de charger les matériels", "danger");
                }
            }
        }

        void loadMaterials();
    }, [notify, isFormateur, salleId]);

    function handleChange(
        event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>,
    ) {
        const { name, value } = event.target;

        setValues((previous) => ({
            ...previous,
            [name]: value,
        }));
    }

    function handleMaterialsChange(
        event: React.ChangeEvent<HTMLSelectElement>,
    ) {
        const materielsId = Array.from(
            event.target.selectedOptions,
            (option) => Number(option.value),
        );

        setValues((previous) => ({
            ...previous,
            materielsId,
        }));
    }

    // Envoie l'alerte ; le message d'erreur dépend du code HTTP renvoyé
    async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
        event.preventDefault();

        if (!event.currentTarget.checkValidity()) return;
        if (!values.signaleurId) return;
        if (!values.materielsId?.length) return;

        try {
            await alertApi.create(values);
            notify("Alerte envoyée.", "success");
            onSubmit(values);
        } catch (error) {
            if (error instanceof ApiError) {
                switch (error.status) {
                    case 400:
                        notify("Données invalides : " + error.message, "danger");
                        break;
                    case 500:
                        notify("Erreur serveur, réessaie plus tard", "danger");
                        break;
                    default:
                        notify(error.message, "danger");
                }
            } else {
                notify("Impossible de joindre le serveur", "danger");
            }
        }
    }

    return (
        <Popup
            title="Signaler un problème"
            onClose={onClose}
            blurBackground={blurBackground}
            actions={
                <button
                    type="submit"
                    form="alert-form"
                    disabled={!canSubmit}
                    className="btn btn-primary"
                >
                    Valider
                </button>
            }
        >
            <form id="alert-form" onSubmit={handleSubmit} ref={formRef}>
                <div className="row g-4">
                    <div className="col-12">
                        <label htmlFor="typeProbleme" className="form-label">
                            Type de problème <span className="text-danger">*</span>
                        </label>
                        <input
                            id="typeProbleme"
                            name="typeProbleme"
                            type="text"
                            className="form-control"
                            value={values.typeProbleme}
                            onChange={handleChange}
                            placeholder="Ex. : Matériel défectueux"
                            required
                        />
                        {!values.typeProbleme.trim() && (
                            <div className="invalid-feedback">
                                Le type de problème est obligatoire.
                            </div>
                        )}
                    </div>

                    <div className="col-12">
                        <label htmlFor="description" className="form-label">
                            Description <span className="text-danger">*</span>
                        </label>
                        <textarea
                            id="description"
                            name="description"
                            className="form-control"
                            rows={4}
                            value={values.description}
                            onChange={handleChange}
                            placeholder="Décris le problème rencontré..."
                            required
                        />
                    </div>

                    {isFormateur && (
                        <div className="col-12">
                            <label htmlFor="salleId" className="form-label">
                                Salle <span className="text-danger">*</span>
                            </label>
                            <select id="salleId" className="form-select" value={salleId} onChange={(event) => setSalleId(event.target.value)} required>
                                <option value="">Choisir une salle</option>
                                {rooms.map((room) => (
                                    <option key={room.id} value={room.id}>{room.nom}</option>
                                ))}
                            </select>
                        </div>
                    )}

                    <div className="col-12">
                        <label htmlFor="materielsId" className="form-label">
                            Matériels concernés <span className="text-danger">*</span>
                        </label>
                        <select
                            id="materielsId"
                            name="materielsId"
                            className="form-select"
                            multiple
                            size={5}
                            value={(values.materielsId ?? []).map(String)}
                            onChange={handleMaterialsChange}
                            required
                        >
                            {materials.map((material) => (
                                <option key={material.id} value={material.id}>
                                    {material.nom} — {material.typeMateriel}
                                </option>
                            ))}
                        </select>
                        <div className="form-text">
                            Maintiens Ctrl pour sélectionner plusieurs matériels.
                        </div>
                    </div>
                </div>
            </form>
        </Popup>
    );
}