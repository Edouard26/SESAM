import { useEffect, useState } from "react";

type Theme = "light" | "dark";

// Thème clair/sombre mémorisé dans localStorage et appliqué via l'attribut data-bs-theme de Bootstrap
export function useTheme() {
    const [theme, setTheme] = useState<Theme>(() => {
        return (localStorage.getItem("theme") as Theme) || "light";
    });

    useEffect(() => {
        document.documentElement.setAttribute(
            "data-bs-theme",
            theme
        );

        localStorage.setItem("theme", theme);
    }, [theme]);

    const toggleTheme = () => {
        setTheme(current =>
            current === "light" ? "dark" : "light"
        );
    };

    return {
        theme,
        toggleTheme
    };
}