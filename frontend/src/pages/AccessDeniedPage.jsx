import { Link } from 'react-router-dom';
import { ShieldAlert } from 'lucide-react';
import StateBlock from '../components/StateBlock.jsx';

export default function AccessDeniedPage() {
  return (
    <StateBlock
      title="Acesso negado"
      description="Seu usuario nao tem permissao para abrir esta area."
      action={
        <Link className="btn-primary inline-flex items-center justify-center gap-2" to="/">
          <ShieldAlert className="h-4 w-4" />
          Voltar ao dashboard
        </Link>
      }
    />
  );
}
