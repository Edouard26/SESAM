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

export function AuthProvider({ children }: { children: ReactNode }) {
  const navigate = useNavigate();
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    async function restoreSession() {
      if (!localStorage.getItem(TOKEN_KEY)) {
        setIsLoading(false);
        return;
      }

      try {
        setUser(await authApi.refresh());
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
    setUser({
      ...response.userDto,
      role: Role[response.userDto.role.toString().toUpperCase() as keyof typeof Role],
    });

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

export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }

  return context;
}