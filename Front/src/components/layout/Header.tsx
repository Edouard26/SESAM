import {ThemeToggle} from "../generic/ThemeToggle.tsx";
import {LogoutButton} from "../auth/LogoutButton.tsx";

export default function Header() {
    return (
        <header className="navbar bg-body-tertiary border-bottom">
            <div className="container-fluid">
                <span className="navbar-brand mb-2 mt-2 h1">
                    SESAM
                </span>
                <ThemeToggle></ThemeToggle>
                <LogoutButton></LogoutButton>
            </div>
        </header>
    );
}