import { useState, type FormEvent } from "react";
import { useAuth } from "../../context/AuthContext.tsx";
import { useNavigate } from "react-router-dom";
import { routes } from "../../router/routes.ts";
import {ApiError} from "../../api";

export default function Login() {
    const { login } = useAuth();
    const navigate = useNavigate();
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [erreur, setErreur] = useState<string | null>(null);
    const [chargement, setChargement] = useState(false);

    async function handleSubmit(e: FormEvent) {
        e.preventDefault();
        setErreur(null);
        setChargement(true);
        try {
            await login(username, password);
            navigate(routes.dashboard);
        } catch (err) {
            if (err instanceof ApiError) {
                switch (err.status) {
                    case 400: setErreur("Données invalides : " + err.message); break;
                    case 500: setErreur("Erreur serveur, réessaie plus tard"); break;
                    default:  setErreur(err.message);
                }
            } else {
                setErreur("Impossible de joindre le serveur");
            }
        } finally {
            setChargement(false);
        }
    }

    return (
        <form onSubmit={handleSubmit}>
            <h2>Accès réservé</h2>
            <div>
                <label>
                    Identifiant
                    <input
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        autoComplete="username"
                    />
                </label>
            </div>
            <div>
                <label>
                    Mot de passe
                    <input
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        autoComplete="current-password"
                    />
                </label>
            </div>
            {erreur && <p style={{ color: "red" }}>{erreur}</p>}
            <button type="submit" disabled={chargement}>
                {chargement ? "Connexion..." : "Entrer dans le dashboard"}
            </button>
        </form>
    );
}
