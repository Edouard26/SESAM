import {Navigate, Outlet, useLocation} from "react-router-dom";
import { routes } from "./routes";
import {useAuth} from "../context/AuthContext.tsx";

export default function ProtectedRoute() {
    const { isAuthenticated, isLoading  } = useAuth();
    const location = useLocation();

    // On attend la restauration de la session pour ne pas rediriger à tort vers le login
    if (isLoading) {
        return null;
    }

    // On retient la page demandée (state.from) pour y revenir après connexion
    if (!isAuthenticated) {
        return <Navigate to={routes.login} replace state={{ from: location }} />;
    }
    return <Outlet />;
}