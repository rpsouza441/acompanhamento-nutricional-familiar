import {
  Award,
  BarChart3,
  CalendarDays,
  ClipboardList,
  FileJson,
  Home,
  LogOut,
  Menu,
  UserCog,
  Utensils,
} from 'lucide-react';
import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

const links = [
  { to: '/', label: 'Dashboard', icon: Home },
  { to: '/registro', label: 'Registro', icon: Utensils },
  { to: '/historico', label: 'Historico', icon: CalendarDays },
  { to: '/conquistas', label: 'Conquistas', icon: Award },
  { to: '/relatorios', label: 'Relatorios', icon: BarChart3 },
  { to: '/admin/usuarios', label: 'Usuarios', icon: UserCog },
  { to: '/admin/importar-plano', label: 'Importar', icon: FileJson },
  { to: '/admin/plano-manual', label: 'Plano manual', icon: ClipboardList },
];

export default function AppShell() {
  const { usuario, logout } = useAuth();

  return (
    <div className="min-h-screen text-ink">
      <aside className="fixed inset-y-0 left-0 hidden w-72 border-r border-line bg-mist/80 px-4 py-5 lg:block">
        <div className="mb-7 flex items-center gap-3 px-2">
          <div className="grid h-10 w-10 place-items-center rounded-lg bg-forest-600 text-sm font-bold text-white">
            NT
          </div>
          <div>
            <p className="text-base font-bold">NutriTracker</p>
            <p className="text-xs text-graphite">Rotina nutricional familiar</p>
          </div>
        </div>

        <nav className="space-y-1">
          {links.map((item) => (
            <NavItem key={item.to} {...item} />
          ))}
        </nav>
      </aside>

      <div className="lg:pl-72">
        <header className="sticky top-0 z-20 border-b border-line bg-mist/90 backdrop-blur">
          <div className="flex h-16 items-center justify-between px-4 sm:px-6">
            <div className="flex items-center gap-3 lg:hidden">
              <Menu className="h-5 w-5 text-graphite" />
              <span className="font-bold">NutriTracker</span>
            </div>
            <div className="hidden lg:block">
              <p className="text-sm font-semibold">{usuario?.nome}</p>
              <p className="text-xs text-graphite">{new Date().toLocaleDateString('pt-BR')}</p>
            </div>
            <button className="btn-secondary" onClick={logout}>
              <LogOut className="h-4 w-4" />
              Sair
            </button>
          </div>
          <nav className="flex gap-1 overflow-x-auto px-3 pb-3 lg:hidden">
            {links.slice(0, 5).map((item) => (
              <NavItem key={item.to} compact {...item} />
            ))}
          </nav>
        </header>
        <main className="mx-auto max-w-7xl px-4 py-6 sm:px-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

function NavItem({ to, label, icon: Icon, compact }) {
  return (
    <NavLink
      to={to}
      end={to === '/'}
      className={({ isActive }) =>
        [
          'flex items-center gap-3 rounded-md px-3 py-2 text-sm font-semibold transition',
          compact ? 'min-w-fit border border-line bg-white' : '',
          isActive ? 'bg-forest-100 text-forest-700' : 'text-graphite hover:bg-white hover:text-ink',
        ].join(' ')
      }
    >
      <Icon className="h-4 w-4" />
      {label}
    </NavLink>
  );
}
