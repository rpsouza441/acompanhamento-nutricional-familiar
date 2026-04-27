import PageHeader from '../components/PageHeader.jsx';
import StateBlock from '../components/StateBlock.jsx';
import { useAuth } from '../context/AuthContext.jsx';
import { api } from '../services/api.js';
import { useAsync } from '../hooks/useAsync.js';

export default function AchievementsPage() {
  const { usuario } = useAuth();
  const { loading, data, error } = useAsync(() => api.get(`/conquistas/usuario/${usuario.id}`).then((r) => r.data), [
    usuario.id,
  ]);

  if (loading) return <StateBlock title="Carregando conquistas" />;
  if (error) return <StateBlock title="Nao foi possivel carregar conquistas" />;

  return (
    <>
      <PageHeader title="Conquistas" description="Progresso de consistencia e hidratacao." />
      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        {data.map((item) => {
          const pct = Math.min(100, Math.round((item.progresso / item.valorMeta) * 100));
          return (
            <article key={item.codigo} className="surface p-5">
              <div className="mb-4 flex items-center justify-between">
                <div>
                  <p className="label">{item.tipo}</p>
                  <h2 className="font-bold">{item.nome}</h2>
                </div>
                <span className={item.desbloqueada ? 'text-forest-700' : 'text-graphite'}>
                  {item.desbloqueada ? 'Desbloqueada' : 'Em progresso'}
                </span>
              </div>
              <p className="min-h-10 text-sm text-graphite">{item.descricao}</p>
              <div className="mt-4 h-2 overflow-hidden rounded-full bg-line">
                <div className="h-full rounded-full bg-forest-600" style={{ width: `${pct}%` }} />
              </div>
              <p className="mt-2 text-sm font-semibold">
                {item.progresso}/{item.valorMeta}
              </p>
            </article>
          );
        })}
      </section>
    </>
  );
}
