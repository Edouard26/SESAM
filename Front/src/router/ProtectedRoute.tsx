import {Navigate, Outlet, useLocation} from "react-router-dom";
import { routes } from "./routes";
import {useAuth} from "../context/AuthContext.tsx";

export default function ProtectedRoute() {
    const { isAuthenticated } = useAuth();
    const location = useLocation();

    if (!isAuthenticated) {
        return <Navigate to={routes.login} replace state={{ from: location }} />;
    }
    return <Outlet />;
}