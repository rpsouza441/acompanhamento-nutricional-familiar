import { Check, Plus, Trash2 } from 'lucide-react';
import { useState } from 'react';
import PageHeader from '../components/PageHeader.jsx';
import StateBlock from '../components/StateBlock.jsx';
import { useAuth } from '../context/AuthContext.jsx';
import { api } from '../services/api.js';
import { useAsync } from '../hooks/useAsync.js';

const today = new Date().toISOString().slice(0, 10);

export default function MealLogPage() {
  const { usuario } = useAuth();
  const [version, setVersion] = useState(0);
  const [manual, setManual] = useState('');
  const [quantidade, setQuantidade] = useState('');
  const { loading, data, error } = useAsync(
    () => api.get('/registros', { params: { usuarioId: usuario.id, data: today } }).then((r) => r.data),
    [usuario.id, version],
  );

  async function addWater(amount) {
    await api.put(`/registros/${data.id}`, {
      aguaConsumidaMl: data.aguaConsumidaMl + amount,
      observacoesGerais: data.observacoesGerais,
    });
    setVersion((value) => value + 1);
  }

  async function conclude(refeicaoId) {
    await api.post(`/registros/${data.id}/refeicoes/${refeicaoId}/concluir`, {});
    setVersion((value) => value + 1);
  }

  async function addManual(refeicaoId) {
    if (!manual.trim()) return;
    await api.post(`/registros/${data.id}/refeicoes/${refeicaoId}/alimentos`, {
      descricaoManual: manual,
      quantidadePersonalizada: quantidade,
    });
    setManual('');
    setQuantidade('');
    setVersion((value) => value + 1);
  }

  async function removeAlimento(alimentoId) {
    await api.delete(`/registros/${data.id}/alimentos/${alimentoId}`);
    setVersion((value) => value + 1);
  }

  if (loading) return <StateBlock title="Carregando registro" />;
  if (error) return <StateBlock title="Registro indisponivel" description="Verifique o plano ativo." />;

  const aguaPct = Math.min(100, Math.round((data.aguaConsumidaMl / data.metaAguaDiariaMl) * 100));

  return (
    <>
      <PageHeader title="Registro de refeicao" description="Marque as refeicoes realizadas e ajuste a agua do dia." />
      <section className="surface mb-6 p-5">
        <div className="mb-3 flex items-center justify-between">
          <h2 className="font-bold">Agua consumida</h2>
          <span className="text-sm font-semibold">{data.aguaConsumidaMl} ml</span>
        </div>
        <div className="h-3 overflow-hidden rounded-full bg-line">
          <div className="water-track h-full" style={{ width: `${aguaPct}%` }} />
        </div>
        <div className="mt-4 flex flex-wrap gap-2">
          {[200, 300, 500].map((amount) => (
            <button key={amount} className="btn-secondary" onClick={() => addWater(amount)}>
              <Plus className="h-4 w-4" />
              {amount}ml
            </button>
          ))}
        </div>
      </section>

      <section className="grid gap-4">
        {data.refeicoes.map((refeicao) => (
          <article key={refeicao.id} className="surface p-5">
            <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
              <div>
                <p className="label">{refeicao.horarioSugerido || 'Horario livre'}</p>
                <h2 className="text-lg font-bold">{refeicao.nome}</h2>
                {refeicao.observacoes ? <p className="text-sm text-graphite">{refeicao.observacoes}</p> : null}
              </div>
              <button className="btn-primary" onClick={() => conclude(refeicao.refeicaoId)} disabled={refeicao.concluida}>
                <Check className="h-4 w-4" />
                {refeicao.concluida ? 'Realizada' : 'Marcar como realizada'}
              </button>
            </div>

            <div className="mt-4 grid gap-2">
              {refeicao.alimentos.map((alimento) => (
                <div key={alimento.id} className="flex items-center justify-between rounded-md bg-mist px-3 py-2 text-sm">
                  <span>{alimento.descricao}</span>
                  <button className="text-cranberry" onClick={() => removeAlimento(alimento.id)}>
                    <Trash2 className="h-4 w-4" />
                  </button>
                </div>
              ))}
            </div>

            <div className="mt-4 grid gap-2 sm:grid-cols-[1fr_160px_auto]">
              <input className="field" value={manual} onChange={(e) => setManual(e.target.value)} placeholder="Entrada manual" />
              <input className="field" value={quantidade} onChange={(e) => setQuantidade(e.target.value)} placeholder="Quantidade" />
              <button className="btn-secondary" onClick={() => addManual(refeicao.refeicaoId)}>
                <Plus className="h-4 w-4" />
                Adicionar
              </button>
            </div>
          </article>
        ))}
      </section>
    </>
  );
}
