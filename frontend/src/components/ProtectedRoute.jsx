import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

export default function ProtectedRoute({ children, roles }) {
  const { isAuthenticated, ready, usuario } = useAuth();

  if (!ready) {
    return <div className="p-6 text-sm text-graphite">Carregando...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (roles?.length && !roles.includes(usuario?.role)) {
    return <Navigate to="/acesso-negado" replace />;
  }

  return children;
}
