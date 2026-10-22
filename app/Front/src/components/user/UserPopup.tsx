import { useEffect, useRef, useState, type FormEvent } from "react";
import { userApi } from "../../api";
import { type User, type UserForm, toDateInputValue } from "../../types/User.ts";
import { Role } from "../../types/Role.ts";
import { Grade } from "../../types/Grade.ts";
import useNotification from "../../hooks/useNotification.ts";
import Popup from "../generic/overlay/Popup";
import RequiredFieldMessage from "../generic/form/RequiredFieldMessage.tsx";

type Props = {
    user?: User;
    onClose: () => void;
    onSubmit: (user: User) => void;
};

type FormState = UserForm;

const emptyForm: FormState = {
    prenom: "",
    nom: "",
    nid: "",
    email: "",
    dateNaissance: "",
    grade: "SOLDAT",
    role: Role.STAGIAIRE,
    active: true,
    password: "",
};

const FORM_ID = "user-form";

export default function UserPopup({ user, onClose, onSubmit }: Props) {
    const { notify } = useNotification();
    const [form, setForm] = useState<FormState>(emptyForm);
    const [saving, setSaving] = useState(false);
    const formRef = useRef<HTMLFormElement>(null);
    const [canSubmit, setCanSubmit] = useState(false);

    const isEdit = user !== undefined;

    useEffect(() => {
        setCanSubmit(formRef.current?.checkValidity() ?? false);
    }, [form]);

    useEffect(() => {
        setForm(
            user
                ? {
                    prenom: user.prenom,
                    nom: user.nom,
                    nid: user.nid,
                    email: user.email,
                    dateNaissance: toDateInputValue(user.dateNaissance),
                    grade: user.grade,
                    role: user.role,
                    active: user.active,
                }
                : emptyForm
        );
    }, [user]);

    const setField = <K extends keyof FormState>(key: K, value: FormState[K]) =>
        setForm((prev) => ({ ...prev, [key]: value }));

    const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        if (!e.currentTarget.checkValidity()) return;
        setSaving(true);
        try {
            const saved = isEdit
                ? await userApi.update(user.id, { ...form, password: undefined })
                : await userApi.create(form);
            onSubmit(saved);
        } catch {
            notify("Impossible d'enregistrer l'utilisateur", "danger");
        } finally {
            setSaving(false);
        }
    };

    return (
        <Popup
            onClose={onClose}
            title={isEdit ? "Modifier l'utilisateur" : "Ajouter un utilisateur"}
            size="modal-lg"
            actions={
                <button type="submit" form={FORM_ID} className="btn btn-primary" disabled={!canSubmit || saving}>
                    {saving ? "Enregistrement..." : isEdit ? "Enregistrer" : "Ajouter"}
                </button>
            }
        >
            <form id={FORM_ID} onSubmit={handleSubmit} ref={formRef}>
                <div className="row g-3">
                    <div className="col-md-6">
                        <label className="form-label" htmlFor="user-prenom">Prénom</label>
                        <input
                            id="user-prenom"
                            className="form-control"
                            value={form.prenom}
                            onChange={(e) => setField("prenom", e.target.value)}
                            required
                        />
                    </div>
                    <div className="col-md-6">
                        <label className="form-label" htmlFor="user-nom">Nom</label>
                        <input
                            id="user-nom"
                            className="form-control"
                            value={form.nom}
                            onChange={(e) => setField("nom", e.target.value)}
                            required
                        />
                    </div>
                    <div className="col-12">
                        <label className="form-label" htmlFor="user-email">Adresse e-mail</label>
                        <input
                            id="user-email"
                            type="email"
                            className="form-control"
                            value={form.email}
                            onChange={(e) => setField("email", e.target.value)}
                            required
                        />
                    </div>
                    {!isEdit && (
                        <div className="col-12">
                            <label className="form-label" htmlFor="password">Mot de passe</label>
                            <input
                                id="password"
                                type="password"
                                autoComplete="new-password"
                                className="form-control"
                                value={form.password ?? ""}
                                onChange={(e) => setField("password", e.target.value)}
                                required
                            />
                        </div>
                    )}

                    <div className="col-md-6">
                        <label className="form-label" htmlFor="user-nid">NID</label>
                        <input
                            id="user-nid"
                            className="form-control"
                            value={form.nid}
                            onChange={(e) => setField("nid", e.target.value)}
                            required
                        />
                    </div>
                    <div className="col-md-6">
                        <label className="form-label" htmlFor="user-naissance">Date de naissance</label>
                        <input
                            id="user-naissance"
                            type="date"
                            className="form-control"
                            value={form.dateNaissance}
                            onChange={(e) => setField("dateNaissance", e.target.value)}
                            required
                        />
                    </div>
                    <div className="col-md-6">
                        <label className="form-label" htmlFor="user-grade">Grade</label>
                        <select
                            id="user-grade"
                            className="form-select"
                            value={form.grade}
                            onChange={(e) => setField("grade", e.target.value as keyof typeof Grade)}
                            required
                        >
                            {Object.keys(Grade)
                                .filter((grade) => isNaN(Number(grade)))
                                .map((grade) => (
                                    <option key={grade} value={grade}>
                                        {grade}
                                    </option>
                                ))}
                        </select>
                    </div>
                    <div className="col-md-6">
                        <label className="form-label" htmlFor="user-role">Rôle</label>
                        <select
                            id="user-role"
                            className="form-select"
                            value={form.role}
                            onChange={(e) => setField("role", Number(e.target.value) as Role)}
                            required
                        >
                            {Object.values(Role)
                                .filter((value): value is Role => typeof value === "number")
                                .map((value) => (
                                    <option key={value} value={value}>
                                        {Role[value]}
                                    </option>
                                ))}
                        </select>
                    </div>
                    <div className="col-md-6 d-flex align-items-end">
                        <div className="form-check form-switch mb-2">
                            <input
                                id="user-active"
                                type="checkbox"
                                className="form-check-input"
                                checked={form.active}
                                onChange={(e) => setField("active", e.target.checked)}
                                required
                            />
                            <label className="form-check-label" htmlFor="user-active">
                                Compte actif
                            </label>
                        </div>
                    </div>
                </div>
                <RequiredFieldMessage formId={FORM_ID} />
            </form>
        </Popup>
    );
}