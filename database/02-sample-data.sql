INSERT INTO usuarios (nome, email, senha_hash, role, ativo)
VALUES (
  'Administrador',
  'admin@nutritracker.local',
  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
  'ADMIN',
  TRUE
)
ON DUPLICATE KEY UPDATE email = email;

INSERT INTO conquistas (codigo, nome, descricao, icone, tipo, valor_meta) VALUES
('primeira_semana', 'Primeira Semana', '7 dias consecutivos seguindo o plano', 'leaf', 'dias_consecutivos', 7),
('disciplinado', 'Disciplinado', '14 dias consecutivos', 'flex', 'dias_consecutivos', 14),
('primeiro_mes', 'Primeiro Mes', '30 dias totais registrados', 'medal', 'dias_totais', 30),
('mestre_hidratacao', 'Mestre da Hidratacao', 'Meta de agua atingida por 30 dias', 'water', 'agua_diaria', 30),
('campeao', 'Campeao', '90 dias totais registrados', 'cup', 'dias_totais', 90)
ON DUPLICATE KEY UPDATE codigo = codigo;
