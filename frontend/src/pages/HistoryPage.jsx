import PageHeader from '../components/PageHeader.jsx';
import StateBlock from '../components/StateBlock.jsx';
import { useAuth } from '../context/AuthContext.jsx';
import { api } from '../services/api.js';
import { useAsync } from '../hooks/useAsync.js';

function monthRange() {
  const now = new Date();
  const start = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().slice(0, 10);
  const end = new Date(now.getFullYear(), now.getMonth() + 1, 0).toISOString().slice(0, 10);
  return { start, end };
}

export default function HistoryPage() {
  const { usuario } = useAuth();
  const { start, end } = monthRange();
  const { loading, data, error } = useAsync(
    () => api.get('/relatorios', { params: { usuarioId: usuario.id, inicio: start, fim: end } }).then((r) => r.data),
    [usuario.id],
  );

  if (loading) return <StateBlock title="Carregando historico" />;
  if (error) return <StateBlock title="Historico indisponivel" />;

  return (
    <>
      <PageHeader title="Historico" description="Indicadores mensais de adesao e agua." />
      <section className="grid gap-4 md:grid-cols-4">
        <Stat label="Dias registrados" value={data.diasRegistrados} />
        <Stat label="Adesao geral" value={`${Math.round(data.adesaoGeralPercentual)}%`} />
        <Stat label="Meta de agua" value={data.diasMetaAgua} />
        <Stat label="Sequencia" value={`${data.maiorSequenciaDias}d`} />
      </section>
      <section className="surface mt-6 p-5">
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-4 lg:grid-cols-7">
          {data.dias.map((dia) => (
            <div key={dia.data} className="rounded-md border border-line bg-mist p-3">
              <p className="text-xs font-semibold text-graphite">{dia.data}</p>
              <div
                className={[
                  'mt-3 h-2 rounded-full',
                  dia.adesaoPercentual >= 80
                    ? 'bg-forest-600'
                    : dia.adesaoPercentual >= 40
                      ? 'bg-amber-400'
                      : 'bg-red-500',
                ].join(' ')}
              />
              <p className="mt-2 text-sm font-bold">{Math.round(dia.adesaoPercentual)}%</p>
            </div>
          ))}
        </div>
      </section>
    </>
  );
}

function Stat({ label, value }) {
  return (
    <div className="surface p-4">
      <p className="label">{label}</p>
      <p className="mt-1 text-2xl font-bold">{value}</p>
    </div>
  );
}
