import {ThemeToggle} from "../generic/ThemeToggle.tsx";
import {LogoutButton} from "../auth/LogoutButton.tsx";

export default function Header() {
    return (
        <header className="navbar bg-body-tertiary border-bottom">
            <div className="container-fluid justify-content-between py-1">
                <ThemeToggle></ThemeToggle>
                <LogoutButton></LogoutButton>
            </div>
        </header>
    );
}