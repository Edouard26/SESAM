import {useEffect, useRef, useState} from "react";
import Popup from "../generic/overlay/Popup";
import {
    materialTemplate,
    type Material,
    type MaterialForm, type MaterialType,
} from "../../types/Material";
import {ApiError, materialApi} from "../../api";
import useNotification from "../../hooks/useNotification.ts";
import RequiredFieldMessage from "../generic/form/RequiredFieldMessage.tsx";


type Props = {
    material?: Material;
    onClose: () => void;
    blurBackground?: boolean;
    onSubmit: (material: MaterialForm) => void;
};

export default function MaterialPopup({ material, onClose, blurBackground, onSubmit }: Props) {
    const {notify} = useNotification();
    const [values, setValues] = useState<MaterialForm>({...materialTemplate});
    const [initialValues, setInitialValues] = useState<MaterialForm>({...materialTemplate});
    const formRef = useRef<HTMLFormElement>(null);
    const [canSubmit, setCanSubmit] = useState(false);

    useEffect(() => {
        setCanSubmit(formRef.current?.checkValidity() ?? false);
    }, [values]);

    const isEditing = !!material;



    useEffect(() => {
        let initial: MaterialForm;

        if (material) {
            initial = Object.fromEntries(
                Object.entries(material).filter(([key]) => key !== "id"),
            ) as MaterialForm;

            initial.dateAjout = new Date(material.dateAjout);
            initial.dateFinGarantie = new Date(material.dateFinGarantie);
        } else {
            initial = {
                ...materialTemplate,
                dateAjout: new Date(materialTemplate.dateAjout),
                dateFinGarantie: new Date(materialTemplate.dateFinGarantie),
            };
        }

        setValues(initial);
        setInitialValues({ ...initial });
    }, [material]);

    function handleChange(event: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) {
        const { name, value } = event.target;

        if (name === "typeMateriel") {
            setValues((previous) => {
                const next: MaterialForm = {
                    ...previous,
                    typeMateriel: value as MaterialType | "",
                };

                if (value !== "UC") {
                    delete next.processeur;
                    delete next.carteGraphique;
                }

                if (value !== "ECRAN") {
                    delete next.taille;
                }

                return next;
            });

            return;
        }

        if (name === "taille") {
            setValues((previous) => ({
                ...previous,
                taille: value === "" ? undefined : Number(value),
            }));

            return;
        }

        setValues((previous) => ({
            ...previous,
            [name]: value,
        }));
    }

    function handleDateChange(event: React.ChangeEvent<HTMLInputElement>) {
        const { name, value } = event.target;

        setValues((previous) => ({
            ...previous,
            [name]: new Date(`${value}T00:00:00`),
        }));
    }

    function formatDate(date: Date): string {
        return date.toISOString().split("T")[0];
    }

    async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
        event.preventDefault();
        if (!event.currentTarget.checkValidity()) return;

        try {
            if (material) {
                const changedValues = Object.fromEntries(
                    Object.entries(values).filter(([key, value]) => {
                        const initialValue = initialValues[key as keyof MaterialForm];

                        if (initialValue instanceof Date && value instanceof Date) {
                            return initialValue.getTime() !== value.getTime();
                        }

                        return initialValue !== value;
                    }),
                ) as Partial<MaterialForm>;
                await materialApi.update(material.id, changedValues);
                onSubmit(values);
            } else {
                await materialApi.create(values);
                onSubmit(values);
            }
        } catch (error) {
            if (error instanceof ApiError) {
                switch (error.status) {
                    case 400: notify("Données invalides : " + error.message, "danger"); break;
                    case 500: notify("Erreur serveur, réessaie plus tard", "danger"); break;
                    default:  notify(error.message, "danger");
                }
            } else {
                notify("Impossible de joindre le serveur", "danger");
            }
        }
    }

    return (
        <Popup title={isEditing ? "Modifier un matériel" : "Ajouter un matériel"}
            onClose={onClose}
            blurBackground={blurBackground}
            actions={
                <button type="submit" form="material-form" disabled={!canSubmit} className="btn btn-primary">
                    {isEditing ? "Enregistrer" : "Ajouter"}
                </button>
            }
        >
            <form id="material-form" onSubmit={handleSubmit} ref={formRef}>
                <div className="row g-4">
                    <div className="col-12 col-md-6">
                        <label htmlFor="nom" className="form-label">
                            Nom <span className="text-danger">*</span>
                        </label>
                        <input id="nom" name="nom" type="text" className="form-control" value={values.nom} onChange={handleChange} required/>
                    </div>

                    <div className="col-12 col-md-6">
                        <label htmlFor="numeroSerie" className="form-label">
                            Numéro de serie <span className="text-danger">*</span>
                        </label>
                        <input id="numeroSerie" name="numeroSerie" type="text" className="form-control" value={values.numeroSerie} onChange={handleChange} required/>
                    </div>

                    <div className="col-12 col-md-6">
                        <label htmlFor="marque" className="form-label">
                            Marque <span className="text-danger">*</span>
                        </label>
                        <select id="marque" name="marque" className="form-select" value={values.marque} onChange={handleChange} required>
                            <option value="" disabled>Choisir une marque</option>
                            <option value="Acer">Acer</option>
                            <option value="Apple">Apple</option>
                            <option value="Asus">Asus</option>
                            <option value="BenQ">BenQ</option>
                            <option value="Dell">Dell</option>
                            <option value="Epson">Epson</option>
                            <option value="Gigabyte">Gigabyte</option>
                            <option value="HP">HP</option>
                            <option value="Huawei">Huawei</option>
                            <option value="iiyama">iiyama</option>
                            <option value="Intel">Intel</option>
                            <option value="Lenovo">Lenovo</option>
                            <option value="LG">LG</option>
                            <option value="Microsoft">Microsoft</option>
                            <option value="MSI">MSI</option>
                            <option value="NEC">NEC</option>
                            <option value="Optoma">Optoma</option>
                            <option value="Philips">Philips</option>
                            <option value="Samsung">Samsung</option>
                            <option value="Sony">Sony</option>
                            <option value="Toshiba">Toshiba</option>
                            <option value="ViewSonic">ViewSonic</option>
                            <option value="Xiaomi">Xiaomi</option>
                        </select>
                    </div>

                    <div className="col-12 col-md-6">
                        <label htmlFor="modele" className="form-label">
                            Modèle <span className="text-danger">*</span>
                        </label>
                        <select id="modele" name="modele" className="form-select" value={values.modele} onChange={handleChange} required>
                            <option value="" disabled>Choisir un Modèle</option>
                            <option value="modele1">Modèle 1</option>
                            <option value="modele2">Modèle 2</option>
                            <option value="modele3">Modèle 3</option>
                            <option value="modele11">Modèle 11</option>
                            <option value="modele12">Modèle 12</option>
                        </select>
                    </div>

                    <div className="col-12 col-md-6">
                        <label htmlFor="typeMateriel" className="form-label">
                            Type <span className="text-danger">*</span>
                        </label>

                        <select id="typeMateriel" name="typeMateriel" className="form-select"
                            value={values.typeMateriel}
                            onChange={handleChange}
                            required
                        >
                            <option value="" disabled>Choisir un type</option>
                            <option value="UC">Unité Centrale</option>
                            <option value="ECRAN">Écran</option>
                            <option value="VIDEO_PROJECTEUR">Vidéo Projecteur</option>
                        </select>
                    </div>

                    <div className="col-12 col-md-6">
                        <label htmlFor="dateAjout" className="form-label">
                            Date d'ajout <span className="text-danger">*</span>
                        </label>
                        <input id="dateAjout" name="dateAjout" type="date" className="form-control"
                           value={formatDate(values.dateAjout)}
                            onChange={handleDateChange}
                            required
                        />
                    </div>

                    <div className="col-12 col-md-6">
                        <label htmlFor="dateFinGarantie" className="form-label">
                            Date fin de garantie <span className="text-danger">*</span>
                        </label>
                        <input id="dateFinGarantie" name="dateFinGarantie" type="date" className="form-control"
                           value={formatDate(values.dateFinGarantie)}
                            onChange={handleDateChange}
                            required
                        />
                    </div>

                    {values.typeMateriel === "UC" && (
                        <>
                            <div className="col-12 col-md-6">
                                <label htmlFor="processeur" className="form-label">
                                    Processeur <span className="text-danger">*</span>
                                </label>

                                <input id="processeur" name="processeur" type="text" className="form-control"
                                    value={values.processeur ?? ""}
                                    onChange={handleChange}
                                    required
                                />
                            </div>

                            <div className="col-12 col-md-6">
                                <label htmlFor="carteGraphique" className="form-label">
                                    Carte graphique <span className="text-danger">*</span>
                                </label>

                                <input id="carteGraphique" name="carteGraphique" type="text" className="form-control"
                                    value={values.carteGraphique ?? ""}
                                    onChange={handleChange}
                                    required
                                />
                            </div>
                        </>
                    )}

                    {values.typeMateriel === "ECRAN" && (
                        <div className="col-12 col-md-6">
                            <label htmlFor="taille" className="form-label">
                                Taille (pouces) <span className="text-danger">*</span>
                            </label>

                            <input id="taille" name="taille" type="number" min="1"
                                className="form-control"
                                value={values.taille ?? ""}
                                onChange={handleChange}
                                required
                            />
                        </div>
                    )}
                </div>
                <RequiredFieldMessage formId="material-form" />
            </form>
        </Popup>
    );
}
