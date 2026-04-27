import { Droplets, Trophy, Utensils } from 'lucide-react';
import PageHeader from '../components/PageHeader.jsx';
import StateBlock from '../components/StateBlock.jsx';
import { useAuth } from '../context/AuthContext.jsx';
import { api } from '../services/api.js';
import { useAsync } from '../hooks/useAsync.js';

const today = new Date().toISOString().slice(0, 10);

export default function DashboardPage() {
  const { usuario } = useAuth();
  const registro = useAsync(
    () => api.get('/registros', { params: { usuarioId: usuario.id, data: today } }).then((r) => r.data),
    [usuario.id],
  );
  const conquistas = useAsync(
    () => api.get(`/conquistas/usuario/${usuario.id}`).then((r) => r.data),
    [usuario.id],
  );

  if (registro.loading) {
    return <StateBlock title="Carregando dashboard" />;
  }

  if (registro.error) {
    return (
      <StateBlock
        title="Nao foi possivel abrir o registro do dia"
        description="Verifique se o usuario possui um plano ativo."
      />
    );
  }

  const data = registro.data;
  const total = data.refeicoes.length;
  const concluidas = data.refeicoes.filter((refeicao) => refeicao.concluida).length;
  const adesao = total ? Math.round((concluidas / total) * 100) : 0;
  const agua = Math.min(100, Math.round((data.aguaConsumidaMl / data.metaAguaDiariaMl) * 100));

  return (
    <>
      <PageHeader title={`Ola, ${usuario.nome}`} description="Resumo do plano nutricional de hoje." />

      <section className="grid gap-4 md:grid-cols-3">
        <Metric icon={Droplets} label="Agua" value={`${data.aguaConsumidaMl} ml`} detail={`${agua}% da meta`} />
        <Metric icon={Utensils} label="Refeicoes" value={`${concluidas}/${total}`} detail={`${adesao}% de adesao`} />
        <Metric
          icon={Trophy}
          label="Conquistas"
          value={(conquistas.data || []).filter((item) => item.desbloqueada).length}
          detail="desbloqueadas"
        />
      </section>

      <section className="mt-6 grid gap-6 lg:grid-cols-[1.3fr_0.7fr]">
        <div className="surface p-5">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="font-bold">Refeicoes de hoje</h2>
            <span className="rounded-full bg-forest-100 px-3 py-1 text-xs font-bold text-forest-700">{adesao}%</span>
          </div>
          <div className="grid gap-3">
            {data.refeicoes.map((refeicao) => (
              <div key={refeicao.id} className="rounded-md border border-line bg-mist px-4 py-3">
                <div className="flex items-center justify-between gap-3">
                  <div>
                    <p className="font-semibold">{refeicao.nome}</p>
                    <p className="text-xs text-graphite">{refeicao.horarioSugerido || 'Horario livre'}</p>
                  </div>
                  <span
                    className={[
                      'rounded-full px-3 py-1 text-xs font-bold',
                      refeicao.concluida ? 'bg-forest-100 text-forest-700' : 'bg-amber-100 text-amber-700',
                    ].join(' ')}
                  >
                    {refeicao.concluida ? 'Realizada' : 'Pendente'}
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="surface p-5">
          <h2 className="mb-4 font-bold">Agua</h2>
          <div className="h-3 overflow-hidden rounded-full bg-line">
            <div className="water-track h-full" style={{ width: `${agua}%` }} />
          </div>
          <p className="mt-3 text-sm text-graphite">
            Meta do plano ativo: <strong className="text-ink">{data.metaAguaDiariaMl} ml</strong>
          </p>
        </div>
      </section>
    </>
  );
}

function Metric({ icon: Icon, label, value, detail }) {
  return (
    <div className="surface p-5">
      <div className="mb-4 flex h-10 w-10 items-center justify-center rounded-lg bg-forest-50 text-forest-700">
        <Icon className="h-5 w-5" />
      </div>
      <p className="label">{label}</p>
      <p className="mt-1 text-2xl font-bold">{value}</p>
      <p className="text-sm text-graphite">{detail}</p>
    </div>
  );
}
