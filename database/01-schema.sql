CREATE TABLE IF NOT EXISTS usuarios (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(100) NOT NULL,
  email VARCHAR(150) NOT NULL UNIQUE,
  senha_hash VARCHAR(255) NOT NULL,
  role ENUM('ADMIN','USER') NOT NULL DEFAULT 'USER',
  ativo BOOLEAN NOT NULL DEFAULT TRUE,
  criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS planos_nutricionais (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  usuario_id BIGINT NOT NULL,
  profissional VARCHAR(200),
  objetivo TEXT,
  meta_agua_diaria_ml INT NOT NULL DEFAULT 3000,
  data_prescricao DATE,
  json_original JSON NOT NULL,
  ativo BOOLEAN NOT NULL DEFAULT TRUE,
  criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_planos_usuario (usuario_id),
  FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE TABLE IF NOT EXISTS refeicoes (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  plano_id BIGINT NOT NULL,
  identificador VARCHAR(50) NOT NULL,
  nome VARCHAR(100) NOT NULL,
  horario_sugerido TIME,
  ordem INT NOT NULL,
  INDEX idx_refeicoes_plano (plano_id),
  FOREIGN KEY (plano_id) REFERENCES planos_nutricionais(id)
);

CREATE TABLE IF NOT EXISTS categorias_refeicao (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  refeicao_id BIGINT NOT NULL,
  nome VARCHAR(100) NOT NULL,
  tipo_selecao ENUM('escolha_uma','escolha_multipla','livre') NOT NULL DEFAULT 'escolha_uma',
  obrigatorio BOOLEAN DEFAULT TRUE,
  INDEX idx_categorias_refeicao (refeicao_id),
  FOREIGN KEY (refeicao_id) REFERENCES refeicoes(id)
);

CREATE TABLE IF NOT EXISTS opcoes_alimento (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  categoria_id BIGINT NOT NULL,
  alimento VARCHAR(200) NOT NULL,
  porcao VARCHAR(100),
  peso_valor DECIMAL(8,2),
  unidade VARCHAR(20),
  INDEX idx_opcoes_categoria (categoria_id),
  FOREIGN KEY (categoria_id) REFERENCES categorias_refeicao(id)
);

CREATE TABLE IF NOT EXISTS registros_diarios (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  usuario_id BIGINT NOT NULL,
  plano_id BIGINT NOT NULL,
  data_registro DATE NOT NULL,
  agua_consumida_ml INT NOT NULL DEFAULT 0,
  observacoes_gerais TEXT,
  criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_usuario_data (usuario_id, data_registro),
  INDEX idx_usuario_data (usuario_id, data_registro),
  INDEX idx_registros_plano (plano_id),
  FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
  FOREIGN KEY (plano_id) REFERENCES planos_nutricionais(id)
);

CREATE TABLE IF NOT EXISTS refeicoes_registradas (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  registro_diario_id BIGINT NOT NULL,
  refeicao_id BIGINT NOT NULL,
  horario_realizado TIME,
  concluida BOOLEAN NOT NULL DEFAULT FALSE,
  observacoes TEXT,
  INDEX idx_refeicoes_registro (registro_diario_id),
  INDEX idx_refeicoes_registradas_refeicao (refeicao_id),
  FOREIGN KEY (registro_diario_id) REFERENCES registros_diarios(id),
  FOREIGN KEY (refeicao_id) REFERENCES refeicoes(id)
);

CREATE TABLE IF NOT EXISTS alimentos_consumidos (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  refeicao_registrada_id BIGINT NOT NULL,
  opcao_id BIGINT,
  descricao_manual VARCHAR(300),
  quantidade_personalizada VARCHAR(100),
  INDEX idx_refeicao (refeicao_registrada_id),
  INDEX idx_alimentos_opcao (opcao_id),
  FOREIGN KEY (refeicao_registrada_id) REFERENCES refeicoes_registradas(id),
  FOREIGN KEY (opcao_id) REFERENCES opcoes_alimento(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS conquistas (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  codigo VARCHAR(50) NOT NULL UNIQUE,
  nome VARCHAR(100) NOT NULL,
  descricao TEXT,
  icone VARCHAR(10),
  tipo ENUM('dias_consecutivos','dias_totais','adesao_percentual','agua_diaria') NOT NULL,
  valor_meta INT NOT NULL
);

CREATE TABLE IF NOT EXISTS usuario_conquistas (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  usuario_id BIGINT NOT NULL,
  conquista_id BIGINT NOT NULL,
  desbloqueada_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_usuario_conquista (usuario_id, conquista_id),
  INDEX idx_usuario_conquistas_usuario (usuario_id),
  FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
  FOREIGN KEY (conquista_id) REFERENCES conquistas(id)
);
