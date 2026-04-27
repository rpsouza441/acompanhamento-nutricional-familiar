import { Power, Save, UserPlus } from 'lucide-react';
import { useState } from 'react';
import PageHeader from '../components/PageHeader.jsx';
import StateBlock from '../components/StateBlock.jsx';
import { api } from '../services/api.js';
import { useAsync } from '../hooks/useAsync.js';

const empty = { nome: '', email: '', senha: '', role: 'USER', ativo: true };

export default function AdminUsersPage() {
  const [form, setForm] = useState(empty);
  const [editingId, setEditingId] = useState(null);
  const [version, setVersion] = useState(0);
  const { loading, data, error } = useAsync(() => api.get('/usuarios').then((r) => r.data), [version]);

  function edit(usuario) {
    setEditingId(usuario.id);
    setForm({ nome: usuario.nome, email: usuario.email, senha: '', role: usuario.role, ativo: usuario.ativo });
  }

  async function save(event) {
    event.preventDefault();
    const payload = { ...form, senha: form.senha || null };
    if (editingId) {
      await api.put(`/usuarios/${editingId}`, payload);
    } else {
      await api.post('/usuarios', payload);
    }
    setForm(empty);
    setEditingId(null);
    setVersion((value) => value + 1);
  }

  async function toggle(usuario) {
    await api.patch(`/usuarios/${usuario.id}/ativo`, { ativo: !usuario.ativo });
    setVersion((value) => value + 1);
  }

  return (
    <>
      <PageHeader title="Admin Usuarios" description="Gerencie os membros da familia e permissao administrativa." />
      <section className="grid gap-6 lg:grid-cols-[0.8fr_1.2fr]">
        <form className="surface space-y-4 p-5" onSubmit={save}>
          <h2 className="font-bold">{editingId ? 'Editar usuario' : 'Novo usuario'}</h2>
          <input className="field" placeholder="Nome" value={form.nome} onChange={(e) => setForm({ ...form, nome: e.target.value })} required />
          <input className="field" placeholder="E-mail" type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required />
          <input className="field" placeholder="Senha" type="password" value={form.senha} onChange={(e) => setForm({ ...form, senha: e.target.value })} required={!editingId} />
          <select className="field" value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value })}>
            <option value="USER">Usuario</option>
            <option value="ADMIN">Admin</option>
          </select>
          <label className="flex items-center gap-2 text-sm font-semibold">
            <input type="checkbox" checked={form.ativo} onChange={(e) => setForm({ ...form, ativo: e.target.checked })} />
            Ativo
          </label>
          <button className="btn-primary">
            {editingId ? <Save className="h-4 w-4" /> : <UserPlus className="h-4 w-4" />}
            Salvar
          </button>
        </form>

        <div className="surface overflow-hidden">
          {loading ? <StateBlock title="Carregando usuarios" /> : null}
          {error ? <StateBlock title="Nao foi possivel carregar usuarios" /> : null}
          {data ? (
            <div className="divide-y divide-line">
              {data.map((usuario) => (
                <div key={usuario.id} className="grid gap-3 p-4 md:grid-cols-[1fr_120px_120px_auto] md:items-center">
                  <button className="text-left" onClick={() => edit(usuario)}>
                    <p className="font-semibold">{usuario.nome}</p>
                    <p className="text-sm text-graphite">{usuario.email}</p>
                  </button>
                  <span className="text-sm font-semibold">{usuario.role}</span>
                  <span className={usuario.ativo ? 'text-sm font-semibold text-forest-700' : 'text-sm font-semibold text-cranberry'}>
                    {usuario.ativo ? 'Ativo' : 'Inativo'}
                  </span>
                  <button className="btn-secondary" onClick={() => toggle(usuario)}>
                    <Power className="h-4 w-4" />
                    Alternar
                  </button>
                </div>
              ))}
            </div>
          ) : null}
        </div>
      </section>
    </>
  );
}
