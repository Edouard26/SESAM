
import { useState, type FormEvent } from "react";
import { useAuth } from "../../context/AuthContext.tsx";
import { useNavigate } from "react-router-dom";
import { routes } from "../../router/routes.ts";
import { ApiError } from "../../api";
import useNotification from "../../hooks/useNotification.ts";
import {ThemeToggle} from "../../components/generic/ThemeToggle.tsx";

export default function Login() {
    const { login } = useAuth();
    const { notify } = useNotification();
    const navigate = useNavigate();

    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [chargement, setChargement] = useState(false);

    async function handleSubmit(e: FormEvent) {
        e.preventDefault();
        setChargement(true);

        try {
            await login(username, password);
            navigate(routes.dashboard);
        } catch (err) {
            if (err instanceof ApiError) {
                switch (err.status) {
                    case 400:
                        notify("Données invalides : " + err.message, "danger")
                        break;
                    case 500:
                        notify("Erreur serveur, réessayez plus tard", "danger");
                        break;
                    default:
                        notify(err.message, "danger");
                }
            } else {
                notify("Impossible de joindre le serveur", "danger");
            }
        } finally {
            setChargement(false);
        }
    }


    return (
        <div
            className="min-vh-100 d-flex align-items-center justify-content-center p-3 position-relative"
            style={{
                background: "linear-gradient(135deg, var(--bs-body-bg), #173b50)",
            }}
        >
            <div className="position-absolute top-0 end-0 p-3">
                <ThemeToggle />
            </div>

            <form
                onSubmit={handleSubmit}
                className="rounded-4 shadow p-4 p-md-5 w-100 border"
                style={{
                    maxWidth: "400px",
                    backgroundColor: "var(--bs-tertiary-bg)",
                    borderColor: "var(--bs-border-color)",
                    color: "var(--bs-body-color)",
                }}
            >
                <div className="text-center mb-4">
                    <i className="bi bi-pc-display-horizontal fs-1 text-primary" />
                    <h1 className="h3 fw-bold mt-2 mb-1">SESAM</h1>
                    <p className="text-secondary mb-0">Accès réservé</p>
                </div>

                <div className="mb-3">
                    <label htmlFor="username" className="form-label">
                        Identifiant
                    </label>
                    <input
                        id="username"
                        type="text"
                        className="form-control"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        autoComplete="username"
                        required
                        disabled={chargement}
                    />
                </div>

                <div className="mb-4">
                    <label htmlFor="password" className="form-label">
                        Mot de passe
                    </label>
                    <input
                        id="password"
                        type="password"
                        className="form-control"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        autoComplete="current-password"
                        required
                        disabled={chargement}
                    />
                </div>

                <button
                    type="submit"
                    className="btn btn-primary w-100"
                    disabled={chargement}
                >
                    {chargement ? "Connexion..." : "Se connecter"}
                </button>
            </form>
        </div>
    );

}
