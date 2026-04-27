import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

export const api = axios.create({
  baseURL: API_BASE_URL,
});

let onUnauthorized = () => {};
let refreshPromise = null;

export function configureAuthInterceptor({ getTokens, setAccessToken, logout }) {
  onUnauthorized = logout;

  api.interceptors.request.use((config) => {
    const { accessToken } = getTokens();
    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`;
    }
    return config;
  });

  api.interceptors.response.use(
    (response) => response,
    async (error) => {
      const original = error.config;
      const status = error.response?.status;
      const { refreshToken } = getTokens();

      if (status !== 401 || original?._retry || !refreshToken) {
        return Promise.reject(error);
      }

      original._retry = true;
      try {
        refreshPromise =
          refreshPromise ||
          axios
            .post(`${API_BASE_URL}/auth/refresh`, { refreshToken })
            .then((response) => response.data.accessToken)
            .finally(() => {
              refreshPromise = null;
            });
        const newAccessToken = await refreshPromise;
        setAccessToken(newAccessToken);
        original.headers.Authorization = `Bearer ${newAccessToken}`;
        return api(original);
      } catch (refreshError) {
        onUnauthorized();
        return Promise.reject(refreshError);
      }
    },
  );
}

export function downloadUrl(path) {
  return `${API_BASE_URL}${path}`;
}
