import { Download } from 'lucide-react';
import { useState } from 'react';
import { Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import PageHeader from '../components/PageHeader.jsx';
import StateBlock from '../components/StateBlock.jsx';
import { useAuth } from '../context/AuthContext.jsx';
import { api, downloadUrl } from '../services/api.js';
import { useAsync } from '../hooks/useAsync.js';

export default function ReportsPage() {
  const { usuario } = useAuth();
  const [inicio, setInicio] = useState(new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().slice(0, 10));
  const [fim, setFim] = useState(new Date().toISOString().slice(0, 10));
  const { loading, data, error } = useAsync(
    () => api.get('/relatorios', { params: { usuarioId: usuario.id, inicio, fim } }).then((r) => r.data),
    [usuario.id, inicio, fim],
  );

  function pdfHref() {
    return downloadUrl(`/relatorios/pdf?usuarioId=${usuario.id}&inicio=${inicio}&fim=${fim}`);
  }

  return (
    <>
      <PageHeader
        title="Relatorios"
        description="Analise de periodo com grafico de agua e download do PDF."
        actions={
          <a className="btn-primary" href={pdfHref()} target="_blank" rel="noreferrer">
            <Download className="h-4 w-4" />
            Gerar PDF
          </a>
        }
      />
      <section className="surface mb-6 grid gap-3 p-5 sm:grid-cols-2">
        <label>
          <span className="label mb-2 block">Inicio</span>
          <input className="field" type="date" value={inicio} onChange={(e) => setInicio(e.target.value)} />
        </label>
        <label>
          <span className="label mb-2 block">Fim</span>
          <input className="field" type="date" value={fim} onChange={(e) => setFim(e.target.value)} />
        </label>
      </section>

      {loading ? <StateBlock title="Carregando relatorio" /> : null}
      {error ? <StateBlock title="Nao foi possivel gerar o relatorio" /> : null}
      {data ? (
        <section className="grid gap-6">
          <div className="grid gap-4 md:grid-cols-4">
            <Stat label="Adesao" value={`${Math.round(data.adesaoGeralPercentual)}%`} />
            <Stat label="Dias" value={data.diasRegistrados} />
            <Stat label="Agua" value={data.diasMetaAgua} />
            <Stat label="Sequencia" value={`${data.maiorSequenciaDias}d`} />
          </div>
          <div className="surface p-5">
            <h2 className="mb-4 font-bold">Agua no periodo</h2>
            <div className="h-72">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={data.dias}>
                  <XAxis dataKey="data" tick={{ fontSize: 12 }} />
                  <YAxis tick={{ fontSize: 12 }} />
                  <Tooltip />
                  <Line type="monotone" dataKey="aguaConsumidaMl" stroke="#096430" strokeWidth={2} dot={false} />
                  <Line type="monotone" dataKey="metaAguaMl" stroke="#0058be" strokeWidth={2} dot={false} />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </div>
        </section>
      ) : null}
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
