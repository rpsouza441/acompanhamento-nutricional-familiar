import { Plus, Save, Trash2 } from 'lucide-react';
import { useState } from 'react';
import PageHeader from '../components/PageHeader.jsx';
import { useAuth } from '../context/AuthContext.jsx';
import { api } from '../services/api.js';

function initial(usuarioId) {
  return {
    usuarioId,
    profissional: '',
    objetivo: '',
    metaAguaDiariaMl: 3000,
    dataPrescricao: new Date().toISOString().slice(0, 10),
    ativo: true,
    refeicoes: [
      {
        identificador: 'desjejum',
        nome: 'Desjejum',
        horarioSugerido: '06:20',
        ordem: 1,
        categorias: [
          {
            nome: 'Proteina',
            tipoSelecao: 'escolha_uma',
            obrigatorio: true,
            opcoes: [{ alimento: '', porcao: '', pesoValor: '', unidade: 'g' }],
          },
        ],
      },
    ],
  };
}

export default function AdminManualPlanPage() {
  const { usuario } = useAuth();
  const [form, setForm] = useState(initial(usuario.id));
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const [saving, setSaving] = useState(false);

  async function submit(event) {
    event.preventDefault();
    const localError = validateForm(form);
    if (localError) {
      setError({ message: localError, errors: [] });
      setResult(null);
      return;
    }

    const payload = {
      ...form,
      metaAguaDiariaMl: Number(form.metaAguaDiariaMl),
      refeicoes: form.refeicoes.map((refeicao) => ({
        ...refeicao,
        ordem: Number(refeicao.ordem),
        categorias: refeicao.categorias.map((categoria) => ({
          ...categoria,
          opcoes: categoria.opcoes.map((opcao) => ({
            ...opcao,
            pesoValor: opcao.pesoValor === '' ? null : Number(opcao.pesoValor),
          })),
        })),
      })),
    };
    setSaving(true);
    setError(null);
    setResult(null);
    try {
      const { data } = await api.post('/planos/manual', payload);
      setResult(data);
    } catch (requestError) {
      setError(normalizeError(requestError));
    } finally {
      setSaving(false);
    }
  }

  function update(path, value) {
    setForm((current) => {
      const next = structuredClone(current);
      let ref = next;
      for (let i = 0; i < path.length - 1; i++) ref = ref[path[i]];
      ref[path.at(-1)] = value;
      return next;
    });
  }

  function addMeal() {
    setForm((current) => ({
      ...current,
      refeicoes: [
        ...current.refeicoes,
        {
          identificador: `refeicao_${current.refeicoes.length + 1}`,
          nome: '',
          horarioSugerido: '',
          ordem: current.refeicoes.length + 1,
          categorias: [
            {
              nome: '',
              tipoSelecao: 'escolha_uma',
              obrigatorio: true,
              opcoes: [{ alimento: '', porcao: '', pesoValor: '', unidade: 'g' }],
            },
          ],
        },
      ],
    }));
  }

  function removeMeal(mealIndex) {
    setForm((current) => {
      if (current.refeicoes.length <= 1) return current;
      const refeicoes = current.refeicoes
        .filter((_, index) => index !== mealIndex)
        .map((refeicao, index) => ({ ...refeicao, ordem: index + 1 }));
      return { ...current, refeicoes };
    });
  }

  function addCategory(mealIndex) {
    setForm((current) => {
      const next = structuredClone(current);
      next.refeicoes[mealIndex].categorias.push({
        nome: '',
        tipoSelecao: 'escolha_uma',
        obrigatorio: true,
        opcoes: [{ alimento: '', porcao: '', pesoValor: '', unidade: 'g' }],
      });
      return next;
    });
  }

  function removeCategory(mealIndex, categoryIndex) {
    setForm((current) => {
      const next = structuredClone(current);
      const categorias = next.refeicoes[mealIndex].categorias;
      if (categorias.length <= 1) return current;
      categorias.splice(categoryIndex, 1);
      return next;
    });
  }

  function addOption(mealIndex, categoryIndex) {
    setForm((current) => {
      const next = structuredClone(current);
      next.refeicoes[mealIndex].categorias[categoryIndex].opcoes.push({ alimento: '', porcao: '', pesoValor: '', unidade: 'g' });
      return next;
    });
  }

  function removeOption(mealIndex, categoryIndex, optionIndex) {
    setForm((current) => {
      const next = structuredClone(current);
      const opcoes = next.refeicoes[mealIndex].categorias[categoryIndex].opcoes;
      if (opcoes.length <= 1) return current;
      opcoes.splice(optionIndex, 1);
      return next;
    });
  }

  return (
    <>
      <PageHeader title="Admin Plano Manual" description="Cadastre refeicoes, categorias e opcoes sem depender de JSON." />
      <form className="grid gap-6" onSubmit={submit}>
        <section className="surface grid gap-4 p-5 md:grid-cols-2">
          <input className="field" placeholder="Profissional" value={form.profissional} onChange={(e) => update(['profissional'], e.target.value)} />
          <input className="field" type="number" placeholder="Meta de agua" value={form.metaAguaDiariaMl} onChange={(e) => update(['metaAguaDiariaMl'], e.target.value)} />
          <input className="field" type="date" value={form.dataPrescricao} onChange={(e) => update(['dataPrescricao'], e.target.value)} />
          <input className="field" placeholder="Objetivo" value={form.objetivo} onChange={(e) => update(['objetivo'], e.target.value)} />
        </section>

        {form.refeicoes.map((refeicao, mealIndex) => (
          <section key={mealIndex} className="surface p-5">
            <div className="mb-3 flex justify-end">
              <button
                className="btn-secondary"
                type="button"
                onClick={() => removeMeal(mealIndex)}
                disabled={form.refeicoes.length <= 1}
              >
                <Trash2 className="h-4 w-4" />
                Refeicao
              </button>
            </div>
            <div className="grid gap-3 md:grid-cols-4">
              <input className="field" placeholder="Identificador" value={refeicao.identificador} onChange={(e) => update(['refeicoes', mealIndex, 'identificador'], e.target.value)} />
              <input className="field" placeholder="Nome" value={refeicao.nome} onChange={(e) => update(['refeicoes', mealIndex, 'nome'], e.target.value)} />
              <input className="field" type="time" value={refeicao.horarioSugerido} onChange={(e) => update(['refeicoes', mealIndex, 'horarioSugerido'], e.target.value)} />
              <input className="field" type="number" value={refeicao.ordem} onChange={(e) => update(['refeicoes', mealIndex, 'ordem'], e.target.value)} />
            </div>
            <div className="mt-4 grid gap-4">
              {refeicao.categorias.map((categoria, categoryIndex) => (
                <div key={categoryIndex} className="rounded-lg border border-line bg-mist p-4">
                  <div className="grid gap-3 md:grid-cols-3">
                    <input className="field" placeholder="Categoria" value={categoria.nome} onChange={(e) => update(['refeicoes', mealIndex, 'categorias', categoryIndex, 'nome'], e.target.value)} />
                    <select className="field" value={categoria.tipoSelecao} onChange={(e) => update(['refeicoes', mealIndex, 'categorias', categoryIndex, 'tipoSelecao'], e.target.value)}>
                      <option value="escolha_uma">Escolha uma</option>
                      <option value="escolha_multipla">Escolha multipla</option>
                      <option value="livre">Livre</option>
                    </select>
                    <button className="btn-secondary" type="button" onClick={() => addOption(mealIndex, categoryIndex)}>
                      <Plus className="h-4 w-4" />
                      Opcao
                    </button>
                  </div>
                  <div className="mt-3 flex justify-end">
                    <button
                      className="btn-secondary"
                      type="button"
                      onClick={() => removeCategory(mealIndex, categoryIndex)}
                      disabled={refeicao.categorias.length <= 1}
                    >
                      <Trash2 className="h-4 w-4" />
                      Categoria
                    </button>
                  </div>
                  <div className="mt-3 grid gap-2">
                    {categoria.opcoes.map((opcao, optionIndex) => (
                      <div key={optionIndex} className="grid gap-2 md:grid-cols-[1fr_1fr_100px_90px_auto]">
                        <input className="field" placeholder="Alimento" value={opcao.alimento} onChange={(e) => update(['refeicoes', mealIndex, 'categorias', categoryIndex, 'opcoes', optionIndex, 'alimento'], e.target.value)} />
                        <input className="field" placeholder="Porcao" value={opcao.porcao} onChange={(e) => update(['refeicoes', mealIndex, 'categorias', categoryIndex, 'opcoes', optionIndex, 'porcao'], e.target.value)} />
                        <input className="field" type="number" placeholder="Peso" value={opcao.pesoValor} onChange={(e) => update(['refeicoes', mealIndex, 'categorias', categoryIndex, 'opcoes', optionIndex, 'pesoValor'], e.target.value)} />
                        <input className="field" placeholder="Unid." value={opcao.unidade} onChange={(e) => update(['refeicoes', mealIndex, 'categorias', categoryIndex, 'opcoes', optionIndex, 'unidade'], e.target.value)} />
                        <button
                          className="btn-secondary px-3"
                          type="button"
                          onClick={() => removeOption(mealIndex, categoryIndex, optionIndex)}
                          disabled={categoria.opcoes.length <= 1}
                          title="Remover opcao"
                          aria-label="Remover opcao"
                        >
                          <Trash2 className="h-4 w-4" />
                        </button>
                      </div>
                    ))}
                  </div>
                </div>
              ))}
            </div>
            <button className="btn-secondary mt-4" type="button" onClick={() => addCategory(mealIndex)}>
              <Plus className="h-4 w-4" />
              Categoria
            </button>
          </section>
        ))}

        <div className="flex flex-wrap gap-2">
          <button className="btn-secondary" type="button" onClick={addMeal}>
            <Plus className="h-4 w-4" />
            Refeicao
          </button>
          <button className="btn-primary" disabled={saving}>
            <Save className="h-4 w-4" />
            {saving ? 'Salvando...' : 'Salvar plano'}
          </button>
        </div>
        {error ? <ErrorBlock error={error} /> : null}
        {result ? <div className="surface p-4 text-sm font-semibold">Plano {result.plano.id} salvo com sucesso.</div> : null}
      </form>
    </>
  );
}

function validateForm(form) {
  if (!form.refeicoes.length) return 'Inclua pelo menos uma refeicao.';
  for (const refeicao of form.refeicoes) {
    if (!refeicao.identificador.trim() || !refeicao.nome.trim()) {
      return 'Preencha identificador e nome de todas as refeicoes.';
    }
    if (!refeicao.categorias.length) return 'Cada refeicao deve ter pelo menos uma categoria.';
    for (const categoria of refeicao.categorias) {
      if (!categoria.nome.trim()) return 'Preencha o nome de todas as categorias.';
      if (!categoria.opcoes.length) return 'Cada categoria deve ter pelo menos uma opcao.';
      if (categoria.opcoes.some((opcao) => !opcao.alimento.trim())) {
        return 'Preencha o alimento de todas as opcoes.';
      }
    }
  }
  return null;
}

function normalizeError(error) {
  const data = error.response?.data;
  return {
    message: data?.message || 'Nao foi possivel salvar o plano.',
    errors: data?.errors || [],
  };
}

function ErrorBlock({ error }) {
  return (
    <div className="surface border-red-200 bg-red-50 p-4 text-sm text-red-900">
      <p className="font-semibold">{error.message}</p>
      {error.errors.length ? (
        <ul className="mt-2 list-disc space-y-1 pl-5">
          {error.errors.map((item) => (
            <li key={`${item.field}-${item.message}`}>
              {item.field}: {item.message}
            </li>
          ))}
        </ul>
      ) : null}
    </div>
  );
}
