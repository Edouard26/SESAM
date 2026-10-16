import { useTheme } from "../../hooks/useTheme";

export function ThemeToggle() {
    const { theme, toggleTheme } = useTheme();

    return (
        <button className="btn btn-outline-secondary" onClick={toggleTheme}>
            {theme === "dark" ? "☀" : "☾"}
        </button>
    );
}