import { createContext, useContext, useEffect, useState, type ReactNode } from "react";
import { useNavigate } from "react-router-dom";
import { authApi } from "../api";
import { ApiError, TOKEN_KEY } from "../api/client.ts";
import { routes } from "../router/routes.ts";
import type { User } from "../types/User.ts";
import {Role} from "../types/Role.ts";

type AuthContextType = {
  isAuthenticated: boolean;
  isLoading: boolean;
  user: User | null;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
};

const AuthContext = createContext<AuthContextType | null>(null);

// Fournit l'utilisateur connecté et les actions login/logout à toute l'application
export function AuthProvider({ children }: { children: ReactNode }) {
  const navigate = useNavigate();
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Au chargement : si un token existe, on le rafraîchit pour retrouver l'utilisateur ; un 401 supprime le token
    async function restoreSession() {
      if (!localStorage.getItem(TOKEN_KEY)) {
        setIsLoading(false);
        return;
      }

      try {
        const response = await authApi.refresh();
        localStorage.setItem(TOKEN_KEY, response.accessToken);
        setUser(normalizeUser(response.userDto));
      } catch (error) {
        if (error instanceof ApiError && error.status === 401) {
          localStorage.removeItem(TOKEN_KEY);
        }
      } finally {
        setIsLoading(false);
      }
    }

    void restoreSession();
  }, []);

  async function login(username: string, password: string): Promise<void> {
    const response = await authApi.login(username, password);

    localStorage.setItem(TOKEN_KEY, response.accessToken);
    setUser(normalizeUser(response.userDto));

    navigate(routes.dashboard, { replace: true });
  }

  function logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    setUser(null);

    navigate(routes.login, { replace: true });
  }

  return (
      <AuthContext.Provider
          value={{
            isAuthenticated: user !== null,
            isLoading,
            user,
            login,
            logout,
          }}
      >
        {children}
      </AuthContext.Provider>
  );
}

// Convertit le rôle reçu de l'API (chaîne) en valeur de l'enum Role
function normalizeUser(user: User): User {
  return {
    ...user,
    role: Role[user.role.toString().toUpperCase() as keyof typeof Role],
  };
}

export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }

  return context;
}
