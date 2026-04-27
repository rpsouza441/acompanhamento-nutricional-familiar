export default function StateBlock({ title = 'Nenhum dado encontrado', description, action }) {
  return (
    <div className="surface p-6 text-center">
      <p className="font-semibold">{title}</p>
      {description ? <p className="mt-1 text-sm text-graphite">{description}</p> : null}
      {action ? <div className="mt-4">{action}</div> : null}
    </div>
  );
}
