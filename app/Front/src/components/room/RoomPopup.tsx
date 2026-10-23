import { useEffect, useRef, useState } from "react";
import Popup from "../generic/overlay/Popup";
import {
    roomTemplate,
    type Room,
    type RoomForm,
    type RoomType,
} from "../../types/Room";
import { ApiError, roomApi } from "../../api";
import type { Building } from "../../types/Building";
import useNotification from "../../hooks/useNotification.ts";
import {buildingApi} from "../../api/building.api.ts";
import RequiredFieldMessage from "../generic/form/RequiredFieldMessage.tsx";

type Props = {
    room?: Room;
    onClose: () => void;
    blurBackground?: boolean;
    onSubmit: (room: RoomForm) => void;
};

export default function RoomPopup({
                                      room,
                                      onClose,
                                      blurBackground,
                                      onSubmit,
                                  }: Props) {
    const { notify } = useNotification();

    const [values, setValues] = useState<RoomForm>({ ...roomTemplate });
    const [initialValues, setInitialValues] = useState<RoomForm>({
        ...roomTemplate,
    });
    const [buildings, setBuildings] = useState<Building[]>([]);
    const [loadingBuildings, setLoadingBuildings] = useState(true);
    const [canSubmit, setCanSubmit] = useState(false);

    const formRef = useRef<HTMLFormElement>(null);

    const isEditing = !!room;
    const isCours = values.typeSalle === "COURS";

    useEffect(() => {
        let cancelled = false;

        async function loadBuildings() {
            setLoadingBuildings(true);

            try {
                const response = await buildingApi.list({
                    page: 0,
                    size: 100,
                    sort: "nom,asc",
                });

                if (!cancelled) {
                    setBuildings(response.content);
                }
            } catch (error) {
                if (!cancelled) {
                    if (error instanceof ApiError) {
                        notify(
                            "Impossible de charger les bâtiments : " + error.message,
                            "danger",
                        );
                    } else {
                        notify("Impossible de charger les bâtiments", "danger");
                    }
                }
            } finally {
                if (!cancelled) {
                    setLoadingBuildings(false);
                }
            }
        }

        void loadBuildings();

        return () => {
            cancelled = true;
        };
    }, [notify]);

    useEffect(() => {
        const initial: RoomForm = room
            ? {
                typeSalle: room.typeSalle,
                etat: room.etat,
                nom: room.nom,
                nbPlaces: room.nbPlaces ?? 0,
                nomBatiment: room.nomBatiment ?? "",
            }
            : { ...roomTemplate };

        setValues(initial);
        setInitialValues({ ...initial });
    }, [room]);

    useEffect(() => {
        setCanSubmit(
            (formRef.current?.checkValidity() ?? false) &&
            !loadingBuildings &&
            buildings.length > 0,
        );
    }, [values, loadingBuildings, buildings]);

    function handleChange(
        event: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>,
    ) {
        const { name, value } = event.target;

        if (name === "typeSalle") {
            setValues((previous) => {
                const next: RoomForm = {
                    ...previous,
                    typeSalle: value as RoomType | "",
                };

                if (value !== "COURS") {
                    next.nbPlaces = 0;
                }

                return next;
            });

            return;
        }

        if (name === "nbPlaces") {
            setValues((previous) => ({
                ...previous,
                nbPlaces: value === "" ? 0 : Number(value),
            }));

            return;
        }

        setValues((previous) => ({
            ...previous,
            [name]: value,
        }));
    }

    async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
        event.preventDefault();

        if (!event.currentTarget.checkValidity()) {
            return;
        }

        try {
            if (room) {
                const changedValues = Object.fromEntries(
                    Object.entries(values).filter(
                        ([key, value]) =>
                            initialValues[key as keyof RoomForm] !== value,
                    ),
                ) as Partial<RoomForm>;

                await roomApi.update(room.id, changedValues);
            } else {
                await roomApi.create(values);
            }

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
            title={isEditing ? "Modifier une salle" : "Ajouter une salle"}
            onClose={onClose}
            blurBackground={blurBackground}
            actions={
                <button
                    type="submit"
                    form="room-form"
                    disabled={!canSubmit}
                    className="btn btn-primary"
                >
                    {isEditing ? "Enregistrer" : "Ajouter"}
                </button>
            }
        >
            <form id="room-form" onSubmit={handleSubmit} ref={formRef}>
                <div className="row g-4">
                    <div className="col-12 col-md-6">
                        <label htmlFor="typeSalle" className="form-label">
                            Type de salle <span className="text-danger">*</span>
                        </label>
                        <select
                            id="typeSalle"
                            name="typeSalle"
                            className="form-select"
                            value={values.typeSalle}
                            onChange={handleChange}
                            required
                        >
                            <option value="" disabled>
                                Choisir un type
                            </option>
                            <option value="COURS">Cours</option>
                            <option value="STOCKAGE">Stockage</option>
                        </select>
                    </div>

                    <div className="col-12 col-md-6">
                        <label htmlFor="etat" className="form-label">
                            État <span className="text-danger">*</span>
                        </label>
                        <select
                            id="etat"
                            name="etat"
                            className="form-select"
                            value={values.etat}
                            onChange={handleChange}
                            required
                        >
                            <option value="" disabled>
                                Choisir un état
                            </option>
                            <option value="DISPONIBLE">Disponible</option>
                            <option value="NON_DISPONIBLE">Non disponible</option>
                            <option value="MAINTENANCE">
                                Réinstallation en cours
                            </option>
                        </select>
                    </div>

                    <div className="col-12 col-md-6">
                        <label htmlFor="nom" className="form-label">
                            Nom <span className="text-danger">*</span>
                        </label>
                        <input
                            id="nom"
                            name="nom"
                            type="text"
                            className="form-control"
                            value={values.nom}
                            onChange={handleChange}
                            required
                            maxLength={10}
                            minLength={2}
                        />
                    </div>

                    <div className="col-12 col-md-6">
                        <label htmlFor="nomBatiment" className="form-label">
                            Bâtiment <span className="text-danger">*</span>
                        </label>
                        <select
                            id="nomBatiment"
                            name="nomBatiment"
                            className="form-select"
                            value={values.nomBatiment}
                            onChange={handleChange}
                            disabled={loadingBuildings || buildings.length === 0}
                            required
                        >
                            <option value="" disabled>
                                {loadingBuildings
                                    ? "Chargement des bâtiments..."
                                    : buildings.length === 0
                                        ? "Aucun bâtiment disponible"
                                        : "Choisir un bâtiment"}
                            </option>
                            {buildings.map((building) => (
                                <option key={building.id} value={building.nom}>
                                    {building.nom}
                                </option>
                            ))}
                        </select>
                    </div>

                    {isCours && (
                        <div className="col-12 col-md-6">
                            <label htmlFor="nbPlaces" className="form-label">
                                Capacité (places){" "}
                                <span className="text-danger">*</span>
                            </label>
                            <input
                                id="nbPlaces"
                                name="nbPlaces"
                                type="number"
                                min="1"
                                className="form-control"
                                value={values.nbPlaces || ""}
                                onChange={handleChange}
                                required
                            />
                        </div>
                    )}
                </div>
                <RequiredFieldMessage formId="room-form" />
            </form>
        </Popup>
    );
}