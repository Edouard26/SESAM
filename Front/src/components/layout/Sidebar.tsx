import { NavLink } from "react-router-dom";
import { routes } from "../../router/routes";
import { useAuth } from "../../context/AuthContext";
import { Role } from "../../types/Role";

export default function Sidebar() {
    const { user } = useAuth();

    if (user === null) {
        return null;
    }

    return (
        <aside className="sidebar bg-primary-gradient d-none d-md-flex flex-column flex-shrink-0 p-3 text-bg-dark">
            <h4>SESAM</h4>

            <hr />

            <ul className="nav nav-pills flex-column gap-1">
                {user.role >= Role.STAGIAIRE && (
                <li>
                    <NavLink to={routes.dashboard} className="nav-link text-white">
                        Dashboard
                    </NavLink>
                </li>
                )}

                {user.role >= Role.FORMATEUR && (
                    <li>
                        <NavLink to={routes.monitoring} className="nav-link text-white">
                            Supervision
                        </NavLink>
                    </li>
                )}

                {user.role >= Role.GESTIONNAIRE && (
                    <>
                        <li>
                            <NavLink to={routes.materials} className="nav-link text-white">
                                Matériels
                            </NavLink>
                        </li>
                        <li>
                            <NavLink to={routes.rooms} className="nav-link text-white">
                                Salles
                            </NavLink>
                        </li>
                        <li>
                            <NavLink to={routes.alerts} className="nav-link text-white">
                                Alertes
                            </NavLink>
                        </li>
                    </>
                )}

                {user.role === Role.ADMINISTRATEUR && (
                    <>
                        <li>
                            <NavLink to={routes.stages} className="nav-link text-white">
                                Stages
                            </NavLink>
                        </li>
                        <li>
                            <NavLink to={routes.users} className="nav-link text-white">
                                Utilisateurs
                            </NavLink>
                        </li>
                    </>
                )}
            </ul>
        </aside>
    );
}