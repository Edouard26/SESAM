import {Navigate, Route, Routes} from "react-router-dom";
import { routes } from "./routes";
import NotFound from "../pages/NotFound.tsx";
import Dashboard from "../pages/dashboard/Dashboard.tsx";
import Materials from "../pages/material/Materials.tsx";
import Material from "../pages/material/Material.tsx";
import Rooms from "../pages/room/Rooms.tsx";
import Room from "../pages/room/Room.tsx";
import Monitoring from "../pages/monitoring/Monitoring.tsx";
import Stages from "../pages/stage/Stages.tsx";
import Alerts from "../pages/alert/Alerts.tsx";
import Process from "../pages/process/Process.tsx";
import Login from "../pages/login/Login.tsx";
import ProtectedRoute from "./ProtectedRoute.tsx";
import Layout from "../components/layout/Layout.tsx";
import Users from "../pages/user/Users.tsx";

export default function Router() {

    return (
        <Routes>
            <Route path={routes.login} element={<Login />} />

            <Route element={<ProtectedRoute />}>
                <Route element={<Layout />}>
                    <Route path="/" element={<Navigate to={routes.dashboard} replace />} />
                    <Route path={routes.dashboard} element={<Dashboard />} />
                    <Route path={routes.materials} element={<Materials />} />
                    <Route path={`${routes.materials}/:id`} element={<Material />} />
                    <Route path={routes.rooms} element={<Rooms />} />
                    <Route path={`${routes.rooms}/:id`} element={<Room />} />
                    <Route path={routes.monitoring} element={<Monitoring />} />
                    <Route path={routes.process} element={<Process />} />
                    <Route path={routes.stages} element={<Stages />} />
                    <Route path={routes.alerts} element={<Alerts />} />
                    <Route path={routes.users} element={<Users />} />
                    <Route path="*" element={<NotFound />} />
                </Route>
            </Route>
        </Routes>
    );
}