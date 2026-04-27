import { Upload } from 'lucide-react';
import { useState } from 'react';
import PageHeader from '../components/PageHeader.jsx';
import { useAuth } from '../context/AuthContext.jsx';
import { api } from '../services/api.js';

export default function AdminImportPlanPage() {
  const { usuario } = useAuth();
  const [usuarioId, setUsuarioId] = useState(usuario.id);
  const [file, setFile] = useState(null);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);

  async function submit(event) {
    event.preventDefault();
    setError(null);
    setResult(null);
    const form = new FormData();
    form.append('file', file);
    try {
      const { data } = await api.post('/planos/importar', form, {
        params: { usuarioId },
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      setResult(data);
    } catch (err) {
      setError(err.response?.data || { message: 'Falha ao importar plano' });
    }
  }

  return (
    <>
      <PageHeader title="Admin Importar Plano" description="Envie uma prescricao em JSON para criar o plano nutricional." />
      <section className="grid gap-6 lg:grid-cols-[0.8fr_1.2fr]">
        <form className="surface space-y-4 p-5" onSubmit={submit}>
          <label>
            <span className="label mb-2 block">Usuario ID</span>
            <input className="field" type="number" value={usuarioId} onChange={(e) => setUsuarioId(e.target.value)} />
          </label>
          <label className="block rounded-lg border border-dashed border-line bg-mist p-6 text-center">
            <Upload className="mx-auto mb-3 h-6 w-6 text-forest-700" />
            <span className="text-sm font-semibold">{file ? file.name : 'Selecionar JSON'}</span>
            <input className="hidden" type="file" accept="application/json,.json" onChange={(e) => setFile(e.target.files[0])} />
          </label>
          <button className="btn-primary" disabled={!file}>
            Importar plano
          </button>
        </form>

        <div className="surface p-5">
          <h2 className="mb-4 font-bold">Resultado</h2>
          {result ? (
            <div className="grid gap-2 text-sm">
              <p>Plano criado: {result.plano.id}</p>
              <p>Refeicoes: {result.refeicoes}</p>
              <p>Categorias: {result.categorias}</p>
              <p>Opcoes: {result.opcoes}</p>
            </div>
          ) : null}
          {error ? (
            <div className="rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-700">
              <p className="font-bold">{error.message}</p>
              {(error.errors || []).map((item) => (
                <p key={item.field}>
                  {item.field}: {item.message}
                </p>
              ))}
            </div>
          ) : null}
        </div>
      </section>
    </>
  );
}
