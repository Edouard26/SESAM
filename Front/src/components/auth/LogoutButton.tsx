import {useAuth} from "../../context/AuthContext.tsx";

export function LogoutButton() {
    const { logout } = useAuth();
    return (
        <button className="btn btn-outline-secondary" onClick={logout}>
            Deconnexion
        </button>
    );
}