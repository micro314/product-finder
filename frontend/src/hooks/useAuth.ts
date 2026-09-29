import { useEffect, useState } from "react";
import {
  AUTH_EXPIRED_EVENT,
  AUTH_TOKEN_UPDATED_EVENT,
  clearStoredToken,
  exchangeCode,
  getStoredToken,
  logout,
  refreshAccessTokenIfNeeded,
  startAuth,
} from "../services/auth";
import { ApiError, getCurrentUser } from "../services/api";
import type { User } from "../types/auth";

export function useAuth() {
  const [token, setToken] = useState(getStoredToken);
  const [user, setUser] = useState<User | null>(null);
  const [authMessage, setAuthMessage] = useState("");

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const code = params.get("code");
    const callbackError =
      params.get("error_description") || params.get("error");
    if (callbackError) {
      setAuthMessage(callbackError);
      window.history.replaceState({}, "", "/");
      return;
    }
    if (!code) return;
    exchangeCode(code, params.get("state"))
      .then((newToken) => {
        setToken(newToken);
        window.history.replaceState({}, "", "/");
      })
      .catch((reason: Error) => {
        setAuthMessage(reason.message);
        window.history.replaceState({}, "", "/");
      });
  }, []);

  useEffect(() => {
    if (!token) return;
    getCurrentUser(token)
      .then(setUser)
      .catch((reason: Error) => {
        if (reason instanceof ApiError && reason.status !== 401) {
          setAuthMessage(reason.message);
          return;
        }
        clearStoredToken();
        setToken(null);
        setUser(null);
        setAuthMessage(
          reason instanceof ApiError && reason.status === 401
            ? "Your session expired. Please sign in again."
            : reason.message,
        );
      });
  }, [token]);

  useEffect(() => {
    const onTokenUpdated = (event: Event) => {
      const updatedToken = (event as CustomEvent<string>).detail;
      if (updatedToken) setToken(updatedToken);
    };
    const onExpired = () => {
      setToken(null);
      setUser(null);
      setAuthMessage("Your session expired. Please sign in again.");
    };
    window.addEventListener(AUTH_TOKEN_UPDATED_EVENT, onTokenUpdated);
    window.addEventListener(AUTH_EXPIRED_EVENT, onExpired);
    return () => {
      window.removeEventListener(AUTH_TOKEN_UPDATED_EVENT, onTokenUpdated);
      window.removeEventListener(AUTH_EXPIRED_EVENT, onExpired);
    };
  }, []);

  useEffect(() => {
    if (!token) return;
    const refresh = () => {
      void refreshAccessTokenIfNeeded().catch(() => undefined);
    };
    refresh();
    const timer = window.setInterval(refresh, 30_000);
    return () => window.clearInterval(timer);
  }, [token]);

  const signOut = () => {
    setToken(null);
    setUser(null);
    logout();
  };
  return {
    token,
    user,
    authMessage,
    signIn: () => startAuth("auth"),
    register: () => startAuth("registrations"),
    signOut,
  };
}
