import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api, configureAuthInterceptor } from '../services/api.js';

const AuthContext = createContext(null);

const STORAGE_KEY = 'nutritracker.auth';

function readStoredAuth() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY)) || {};
  } catch {
    return {};
  }
}

export function AuthProvider({ children }) {
  const navigate = useNavigate();
  const [auth, setAuth] = useState(readStoredAuth);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    configureAuthInterceptor({
      getTokens: () => ({
        accessToken: readStoredAuth().accessToken,
        refreshToken: readStoredAuth().refreshToken,
      }),
      setAccessToken: (accessToken) => {
        const current = readStoredAuth();
        const next = { ...current, accessToken };
        localStorage.setItem(STORAGE_KEY, JSON.stringify(next));
        setAuth(next);
      },
      logout: () => {
        localStorage.removeItem(STORAGE_KEY);
        setAuth({});
        navigate('/login', { replace: true });
      },
    });
    setReady(true);
  }, [navigate]);

  const value = useMemo(
    () => ({
      ready,
      usuario: auth.usuario,
      accessToken: auth.accessToken,
      isAuthenticated: Boolean(auth.accessToken && auth.usuario),
      async login(email, senha) {
        const { data } = await api.post('/auth/login', { email, senha });
        localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
        setAuth(data);
        return data;
      },
      logout() {
        localStorage.removeItem(STORAGE_KEY);
        setAuth({});
        navigate('/login', { replace: true });
      },
    }),
    [auth, navigate, ready],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth deve ser usado dentro de AuthProvider');
  }
  return context;
}
