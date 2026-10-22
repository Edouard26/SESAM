import { useTheme } from "../../hooks/useTheme";

export function ThemeToggle() {
    const { theme, toggleTheme } = useTheme();
    const isDark = theme === "dark";

    return (
        <button
            type="button"
            onClick={toggleTheme}
            className="position-relative rounded-pill border-0 p-0"
            aria-label={isDark ? "Activer le mode jour" : "Activer le mode nuit"}
            title={isDark ? "Mode jour" : "Mode nuit"}
            style={{
                width: "52px",
                height: "30px",
                backgroundColor: isDark ? "var(--bs-primary)" : "#e9ecef",
                transition: "background-color 0.2s ease",
                cursor: "pointer",
            }}
        >
            <span
                className="position-absolute top-50 d-flex align-items-center justify-content-center rounded-circle shadow-sm"
                style={{
                    width: "24px",
                    height: "24px",
                    left: isDark ? "25px" : "3px",
                    transform: "translateY(-50%)",
                    backgroundColor: isDark ? "#173b50" : "#ffffff",
                    color: isDark ? "#ffffff" : "#f0ad00",
                    transition: "left 0.2s ease",
                }}
            >
                <i
                    className={`bi ${isDark ? "bi-moon-fill" : "bi-sun-fill"}`}
                    style={{ fontSize: "13px" }}
                />
            </span>
        </button>
    );
}