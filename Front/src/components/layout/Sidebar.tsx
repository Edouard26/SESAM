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
                <li>
                    <NavLink
                        to={routes.dashboard}
                        end
                        className="nav-link text-white"
                    >
                        <i className="bi bi-house me-3" />
                        Accueil
                    </NavLink>
                </li>

                {user.role >= Role.GESTIONNAIRE && (
                    <>
                        <li>
                            <NavLink to={routes.materials} className="nav-link text-white">
                                <i className="bi bi-box me-3" />
                                Matériels
                            </NavLink>
                        </li>

                        <li>
                            <NavLink to={routes.rooms} className="nav-link text-white">
                                <i className="bi bi-display me-3" />
                                Salles
                            </NavLink>
                        </li>
                    </>
                )}

                {user.role >= Role.FORMATEUR && (
                    <li>
                        <NavLink to={routes.monitoring} className="nav-link text-white">
                            <i className="bi bi-graph-up me-3" />
                            Supervision
                        </NavLink>
                    </li>
                )}

                {user.role >= Role.GESTIONNAIRE && (
                    <li>
                        <NavLink to={routes.alerts} className="nav-link text-white">
                            <i className="bi bi-bell me-3" />
                            Alertes
                        </NavLink>
                    </li>
                )}

                {user.role === Role.ADMINISTRATEUR && (
                    <>
                        <li>
                            <NavLink to={routes.stages} className="nav-link text-white">
                                <i className="bi bi-mortarboard me-3" />
                                Stages
                            </NavLink>
                        </li>

                        <li>
                            <NavLink to={routes.users} className="nav-link text-white">
                                <i className="bi bi-people me-3" />
                                Utilisateurs
                            </NavLink>
                        </li>
                    </>
                )}
            </ul>
        </aside>
    );
}