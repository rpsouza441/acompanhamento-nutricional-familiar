import { useState } from 'react';
import { Leaf, Lock, Mail } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

export default function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [email, setEmail] = useState('admin@nutritracker.local');
  const [senha, setSenha] = useState('password');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(email, senha);
      navigate('/', { replace: true });
    } catch {
      setError('E-mail ou senha invalidos.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="grid min-h-screen place-items-center px-4 py-10">
      <section className="grid w-full max-w-5xl gap-6 lg:grid-cols-[1.1fr_0.9fr]">
        <div className="surface p-8 sm:p-10">
          <div className="mb-10 flex items-center gap-3">
            <div className="grid h-11 w-11 place-items-center rounded-lg bg-forest-600 text-white">
              <Leaf className="h-5 w-5" />
            </div>
            <div>
              <h1 className="text-2xl font-bold">NutriTracker</h1>
              <p className="text-sm text-graphite">Acompanhamento nutricional familiar</p>
            </div>
          </div>

          <form className="space-y-4" onSubmit={handleSubmit}>
            <label className="block">
              <span className="label mb-2 block">E-mail</span>
              <span className="relative block">
                <Mail className="pointer-events-none absolute left-3 top-2.5 h-4 w-4 text-graphite" />
                <input
                  className="field pl-9"
                  type="email"
                  value={email}
                  onChange={(event) => setEmail(event.target.value)}
                  required
                />
              </span>
            </label>
            <label className="block">
              <span className="label mb-2 block">Senha</span>
              <span className="relative block">
                <Lock className="pointer-events-none absolute left-3 top-2.5 h-4 w-4 text-graphite" />
                <input
                  className="field pl-9"
                  type="password"
                  value={senha}
                  onChange={(event) => setSenha(event.target.value)}
                  required
                />
              </span>
            </label>
            {error ? (
              <div className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
                {error}
              </div>
            ) : null}
            <button className="btn-primary w-full" disabled={loading}>
              {loading ? 'Entrando...' : 'Entrar'}
            </button>
          </form>
        </div>

        <aside className="surface flex flex-col justify-between bg-forest-50 p-8">
          <div>
            <p className="label mb-3">Rotina do dia</p>
            <h2 className="text-3xl font-bold leading-tight">Plano, agua e refeicoes em um so lugar.</h2>
            <p className="mt-4 text-sm leading-6 text-graphite">
              Acompanhe a adesao familiar com registros simples, conquistas e relatorios para a consulta.
            </p>
          </div>
          <div className="mt-8 grid gap-3 text-sm">
            {['Meta de agua dinamica', 'Importacao JSON ou plano manual', 'PDF profissional'].map((item) => (
              <div key={item} className="rounded-md border border-forest-100 bg-white/70 px-3 py-2 font-semibold">
                {item}
              </div>
            ))}
          </div>
        </aside>
      </section>
    </main>
  );
}
