-- V11: Carregar dicionário completo de métricas baseado no arquivo DIC_06_Escolas_Dependencias.csv
-- Todos os campos de infraestrutura e recursos das escolas

-- Ajusta constraint para aceitar STRING além de INT
ALTER TABLE school.metric_dictionary
    DROP CONSTRAINT IF EXISTS metric_dictionary_data_type_check;

ALTER TABLE school.metric_dictionary
    ADD CONSTRAINT metric_dictionary_data_type_check
    CHECK (data_type IN ('INT', 'STRING'));

-- Adiciona colunas de auditoria se ainda não existirem
ALTER TABLE school.metric_dictionary
    ADD COLUMN IF NOT EXISTS created_at timestamp NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS updated_at timestamp NOT NULL DEFAULT NOW();

INSERT INTO school.metric_dictionary (metric_code, metric_name, data_type, created_at, updated_at) VALUES
-- Informações básicas da escola
('NOMEDEP', 'Nome da rede de ensino', 'STRING', NOW(), NOW()),
('DE', 'Nome da diretoria de ensino', 'STRING', NOW(), NOW()),
('MUN', 'Nome do município', 'STRING', NOW(), NOW()),
('DISTR', 'Nome do distrito', 'STRING', NOW(), NOW()),
('CODESC', 'Código da escola', 'STRING', NOW(), NOW()),
('NOMESC', 'Nome da escola', 'STRING', NOW(), NOW()),
('TIPOESC', 'Tipo da escola', 'INT', NOW(), NOW()),
('TIPOESC_DESC', 'Tipo descrição escola', 'STRING', NOW(), NOW()),
('CODSIT', 'Situação escola', 'STRING', NOW(), NOW()),

-- Salas de aula e educação
('SALAS_AULA', 'Sala de aula', 'INT', NOW(), NOW()),
('SALAS_ED_INF', 'Sala de educação infantil', 'INT', NOW(), NOW()),
('SALAS_ED_ESP', 'Sala de educação especial', 'INT', NOW(), NOW()),
('SALAS_ED_ART', 'Sala de educação artística', 'INT', NOW(), NOW()),
('SALA_RECURSO', 'Sala recurso', 'INT', NOW(), NOW()),
('TOT_SALAS_AULA', 'Total salas aula', 'INT', NOW(), NOW()),

-- Espaços culturais e eventos
('AUDITORIO', 'Auditório', 'INT', NOW(), NOW()),
('ANFITEATRO', 'Anfiteatro', 'INT', NOW(), NOW()),
('TEATRO', 'Teatro', 'INT', NOW(), NOW()),

-- Alimentação
('CANTINA', 'Cantina', 'INT', NOW(), NOW()),
('COPA', 'Copa', 'INT', NOW(), NOW()),
('COZINHA', 'Cozinha', 'INT', NOW(), NOW()),
('REFEITORIO', 'Refeitório', 'INT', NOW(), NOW()),
('DEPOSITO_ALIMENTOS', 'Depósito de alimentos', 'INT', NOW(), NOW()),
('DESPENSA', 'Despensa', 'INT', NOW(), NOW()),
('TOT_DESPENSA', 'Total despensa', 'INT', NOW(), NOW()),

-- Biblioteca e leitura
('SALA_LEITURA', 'Sala de leitura', 'INT', NOW(), NOW()),
('BIBLIOTECA', 'Biblioteca', 'INT', NOW(), NOW()),
('TOT_SALA_LEITURA', 'Total sala leitura', 'INT', NOW(), NOW()),

-- Esportes
('QUADRA_COBERTA', 'Quadra coberta', 'INT', NOW(), NOW()),
('QUADRA_DESCOBERTA', 'Quadra descoberta', 'INT', NOW(), NOW()),
('GINASIO', 'Ginásio de esportes', 'INT', NOW(), NOW()),
('TOT_QUADRA', 'Total quadra', 'INT', NOW(), NOW()),
('QUADRA_AREIA', 'Quadra de areia - futebol/vôlei', 'INT', NOW(), NOW()),
('QUADRA_GRAMA', 'Quadra de grama', 'INT', NOW(), NOW()),
('CAMPO_FUTEBOL', 'Campo de futebol', 'INT', NOW(), NOW()),

-- Saúde
('GABINETE_DENTARIO', 'Gabinete dentário', 'INT', NOW(), NOW()),
('CONSULTORIO_MEDICO', 'Consultório médico', 'INT', NOW(), NOW()),
('ENFERMARIA', 'Enfermaria', 'INT', NOW(), NOW()),
('AMBULATORIO', 'Ambulatório', 'INT', NOW(), NOW()),

-- Administração
('ALMOXARIFADO', 'Almoxarifado', 'INT', NOW(), NOW()),
('ARQUIVO', 'Arquivo', 'INT', NOW(), NOW()),
('REPROGRAFIA', 'Reprografia/xerox', 'INT', NOW(), NOW()),
('SALA_GREMIO', 'Sala do grêmio', 'INT', NOW(), NOW()),
('DIRETORIA', 'Diretoria', 'INT', NOW(), NOW()),
('VICEDIRETORIA', 'Vice diretoria', 'INT', NOW(), NOW()),
('SALA_PROF', 'Sala de professores', 'INT', NOW(), NOW()),
('SECRETARIA', 'Secretaria', 'INT', NOW(), NOW()),
('SALA_ORIENT_ED', 'Sala orientação educacional', 'INT', NOW(), NOW()),
('SALA_COORD_PEDAG', 'Sala de coordenador pedagógico', 'INT', NOW(), NOW()),

-- Áreas comuns
('PATIO_COBERTO', 'Pátio coberto', 'INT', NOW(), NOW()),
('PATIO_DESCOBERTO', 'Pátio descoberto', 'INT', NOW(), NOW()),
('ZELADORIA', 'Zeladoria', 'INT', NOW(), NOW()),

-- Vestiários
('VESTIARIO_FEM', 'Vestiário feminino', 'INT', NOW(), NOW()),
('VESTIARIO_MASC', 'Vestiário masculino', 'INT', NOW(), NOW()),
('TOT_VESTIARIO', 'Total vestiário', 'INT', NOW(), NOW()),

-- Tecnologia e mídia
('VIDEOTECA', 'Videoteca', 'INT', NOW(), NOW()),
('SALA_TV', 'Sala TV', 'INT', NOW(), NOW()),
('LAB_INFO', 'Laboratório de informática', 'INT', NOW(), NOW()),

-- Laboratórios de ciências
('LAB_CIENCIAS', 'Laboratório de ciências', 'INT', NOW(), NOW()),
('LAB_FISICA', 'Laboratório de física', 'INT', NOW(), NOW()),
('LAB_QUIMICA', 'Laboratório de química', 'INT', NOW(), NOW()),
('LAB_BIOLOGIA', 'Laboratório de biologia', 'INT', NOW(), NOW()),
('LAB_CIENCIA_FISICA_BIOLOGICA', 'Laboratório de ciências física e biológica', 'INT', NOW(), NOW()),
('TOT_LAB_CIENCIA', 'Total laboratório ciência', 'INT', NOW(), NOW()),

-- Outros laboratórios
('LAB_LINGUAS', 'Laboratório de línguas', 'INT', NOW(), NOW()),
('LAB_MULTIUSO', 'Laboratório multiuso', 'INT', NOW(), NOW()),

-- Oficinas e desenvolvimento
('OFICINA', 'Oficina', 'INT', NOW(), NOW()),
('PLAYGROUND', 'Playground', 'INT', NOW(), NOW()),

-- Dormitórios e cuidados infantis
('DORMITORIO', 'Dormitório', 'INT', NOW(), NOW()),
('BERCARIO', 'Berçário', 'INT', NOW(), NOW()),

-- Sanitários
('SANITARIO_ADEQ_PRE', 'Sanitário adequado à pré escola', 'INT', NOW(), NOW()),
('SANITARIO_ADEQ_PRE_FEM', 'Sanitário adequado à pré escola feminino', 'INT', NOW(), NOW()),
('SANITARIO_ADEQ_PRE_MASC', 'Sanitário adequado à pré escola masculino', 'INT', NOW(), NOW()),
('SANITARIO_ADEQ_DEF', 'Sanitário adequado à portadores de deficiência', 'INT', NOW(), NOW()),
('SANITARIO_ADEQ_DEF_MASC', 'Sanitário adequado à portadores de deficiência masculino', 'INT', NOW(), NOW()),
('SANITARIO_AL_MASC', 'Sanitário aluno masculino', 'INT', NOW(), NOW()),
('SANITARIO_AL_FEM', 'Sanitário aluno feminino', 'INT', NOW(), NOW()),
('TOT_SANITARIO_AL', 'Total sanitário alunos', 'INT', NOW(), NOW()),
('SANITARIO_FUNC_FEM', 'Sanitário funcionário feminino', 'INT', NOW(), NOW()),
('SANITARIO_FUNC_MASC', 'Sanitário funcionário masculino', 'INT', NOW(), NOW()),
('TOT_SANITARIO_FUNC', 'Total sanitário funcionário', 'INT', NOW(), NOW()),

-- Acessibilidade
('DEPEND_ADEQ_DEF', 'Dependência e via adequada à deficientes', 'INT', NOW(), NOW()),

-- Educação física e recreação
('SALA_ED_FISICA', 'Sala de educação física', 'INT', NOW(), NOW()),
('PISCINA', 'Piscina', 'INT', NOW(), NOW()),

-- Segurança e acesso
('PORTARIA', 'Portaria', 'INT', NOW(), NOW()),

-- Programas especiais
('SALA_PROG_ESC_FAMILIA', 'Sala do programa escola da família', 'INT', NOW(), NOW()),

-- Cuidados infantis especializados
('BRINQUEDOTECA', 'Brinquedoteca', 'INT', NOW(), NOW()),
('FRALDARIO', 'Fraldário', 'INT', NOW(), NOW()),
('LACTARIO', 'Lactário', 'INT', NOW(), NOW()),
('LAVANDERIA', 'Lavanderia', 'INT', NOW(), NOW()),
('SOLARIO', 'Solário', 'INT', NOW(), NOW()),

-- Salas administrativas especializadas
('SALA_ESPERA', 'Sala de espera', 'INT', NOW(), NOW()),
('SALA_INSPETOR', 'Sala de inspetor de aluno', 'INT', NOW(), NOW()),
('SALA_REUNIAO', 'Sala de reunião', 'INT', NOW(), NOW()),
('TESOURARIA', 'Tesouraria', 'INT', NOW(), NOW()),
('SALA_REFORCO', 'Sala de reforço', 'INT', NOW(), NOW()),
('SALA_DIRETOR_TECNICO', 'Sala de diretor técnico', 'INT', NOW(), NOW()),

-- Transporte
('GARAGEM_ONIBUS', 'Garagem de ônibus', 'INT', NOW(), NOW()),

-- Saúde especializada
('SALA_FISIOTERAPIA', 'Sala de fisioterapia', 'INT', NOW(), NOW()),
('SALA_PSICOLOGIA', 'Sala de psicologia', 'INT', NOW(), NOW()),
('SALA_FONOAUDIOLOGIA', 'Sala de fonoaudiologia', 'INT', NOW(), NOW()),

-- Eventos e assistência
('SALA_EVENTOS', 'Sala eventos', 'INT', NOW(), NOW()),
('SALA_ASSIST_SOCIAL', 'Sala assistente social', 'INT', NOW(), NOW()),
('SALA_TERAPIA_EDUC', 'Sala terapia educacional', 'INT', NOW(), NOW()),

-- Agropecuária e agricultura
('ABATEDOURO', 'Abatedouro', 'INT', NOW(), NOW()),

-- Alojamentos
('ALOJAMENTO_FEM', 'Alojamento feminino', 'INT', NOW(), NOW()),
('ALOJAMENTO_MASC', 'Alojamento masculino', 'INT', NOW(), NOW()),
('TOT_ALOJAMENTO', 'Total alojamento', 'INT', NOW(), NOW()),

-- Serviços gerais
('AREA_SERVICO', 'Área de serviço', 'INT', NOW(), NOW()),
('BAZAR', 'Bazar', 'INT', NOW(), NOW()),
('CASA_MAQUINA', 'Casa máquina', 'INT', NOW(), NOW()),
('CASA_FUNC', 'Casa de funcionários', 'INT', NOW(), NOW()),
('CHURRASQUEIRA', 'Churrasqueira', 'INT', NOW(), NOW()),

-- Agropecuária especializada
('DEPOSITOS_CEREAIS', 'Depósito para cereais e outros produtos de colheita', 'INT', NOW(), NOW()),
('ELEVADOR', 'Elevador', 'INT', NOW(), NOW()),
('ESTACIONAMENTO', 'Estacionamento', 'INT', NOW(), NOW()),
('ESTUFA', 'Estufa', 'INT', NOW(), NOW()),

-- Galpões agropecuários
('GALPAO_AVES_CORTE', 'Galpão para aves de corte', 'INT', NOW(), NOW()),
('GALPAO_AVES_POSTURA', 'Galpão para aves de postura', 'INT', NOW(), NOW()),
('GALPAO_BOVINOS_LEITE', 'Galpão para bovinos de leite', 'INT', NOW(), NOW()),
('GALPAO_CUNICULTURA', 'Galpão para cunicultura', 'INT', NOW(), NOW()),
('GALPAO_MAQ_AGRICOLA', 'Galpão para máquinas e veículos agrícolas', 'INT', NOW(), NOW()),
('GALPAO_OVINOS_CAPRINOS', 'Galpão para ovinos/caprinos', 'INT', NOW(), NOW()),
('GALPAO_SUINO', 'Galpão para suínos', 'INT', NOW(), NOW()),

-- Produção e processamento
('GRAFICA', 'Gráfica', 'INT', NOW(), NOW()),
('HORTA', 'Horta', 'INT', NOW(), NOW()),

-- Laboratórios especializados
('LAB_DIDATICA', 'Laboratório de didática', 'INT', NOW(), NOW()),
('LAB_JUNIOR', 'Laboratório junior', 'INT', NOW(), NOW()),
('LAB_ENFERMAGEM', 'Laboratório de enfermagem', 'INT', NOW(), NOW()),
('LAB_ESTETICA', 'Laboratório de estética', 'INT', NOW(), NOW()),
('LAB_PSICOPEDAGOGIA', 'Laboratório de psicopedagogia', 'INT', NOW(), NOW()),
('LAB_TURISMO', 'Laboratório de turismo', 'INT', NOW(), NOW()),

-- Infraestrutura rural
('LAVATORIO', 'Lavatório', 'INT', NOW(), NOW()),
('MANGUEIRA', 'Mangueira para manejo de bovinos de corte', 'INT', NOW(), NOW()),
('MINHOCARIO', 'Minhocário', 'INT', NOW(), NOW()),
('PACKING_HOUSE', 'Packing house', 'INT', NOW(), NOW()),
('POMAR', 'Pomar', 'INT', NOW(), NOW()),
('PSICULTURA', 'Piscicultura', 'INT', NOW(), NOW()),

-- Atendimento e recepção
('RECEPCAO', 'Recepção', 'INT', NOW(), NOW()),

-- Salas de atendimento especializadas
('SALA_ATENDIMENTO', 'Sala de atendimento', 'INT', NOW(), NOW()),
('SALA_ATEND_PSICOLOGICO', 'Sala de atendimento psicológico', 'INT', NOW(), NOW()),
('SALA_AUX_COORDENACAO', 'Sala de auxiliar de coordenação', 'INT', NOW(), NOW()),
('SALA_DADOS', 'Sala de dados', 'INT', NOW(), NOW()),
('SALA_DEP_PESSOAL', 'Sala de departamento pessoal', 'INT', NOW(), NOW()),
('SALA_ED_RELIGIOSA', 'Sala de educação religiosa', 'INT', NOW(), NOW()),
('SALA_ENERGIA_ELETRICA', 'Sala de energia elétrica', 'INT', NOW(), NOW()),
('SALA_ENTRETENIMENTO', 'Sala de entretenimentos', 'INT', NOW(), NOW()),
('SALA_ESTAGIO', 'Sala de estágio', 'INT', NOW(), NOW()),
('SALA_GINASTICA', 'Sala de ginástica', 'INT', NOW(), NOW()),

-- Salas agropecuárias
('SALA_INSUMO_AGRICOLA', 'Sala de insumos agrícolas', 'INT', NOW(), NOW()),
('SALA_INSUMO_VETERINARIO', 'Sala de insumos veterinários', 'INT', NOW(), NOW()),

-- Salas administrativas avançadas
('SALA_MARKETING', 'Sala de marketing', 'INT', NOW(), NOW()),
('SALA_MATRICULA', 'Sala de matrícula', 'INT', NOW(), NOW()),
('SALA_MUSICA', 'Sala de música', 'INT', NOW(), NOW()),
('SALA_POS_GRADUACAO', 'Sala de núcleo de pós-graduação', 'INT', NOW(), NOW()),

-- Salas agropecuárias especializadas
('SALA_ORDENHA', 'Sala de ordenha', 'INT', NOW(), NOW()),
('SALA_PROC_PROD_AGROPECUARIOS', 'Sala de processamento de produtos agropecuários', 'INT', NOW(), NOW()),

-- Salas de serviços
('SALA_SEGURANCA', 'Sala de segurança', 'INT', NOW(), NOW()),
('SALA_TELEFONIA', 'Sala de telefonia', 'INT', NOW(), NOW()),
('SALA_FINANCEIRO', 'Sala do financeiro', 'INT', NOW(), NOW()),
('SALA_PASTORAL', 'Sala pastoral', 'INT', NOW(), NOW()),
('SALA_RESERV_AGUA', 'Sala reservatório de água', 'INT', NOW(), NOW()),

-- Infraestrutura tecnológica
('SERVIDOR', 'Servidor', 'INT', NOW(), NOW()),
('SILO', 'Silo', 'INT', NOW(), NOW()),

-- Áreas externas
('VARANDA', 'Varanda', 'INT', NOW(), NOW()),
('VIVEIRO', 'Viveiro', 'INT', NOW(), NOW()),

-- Salas terapêuticas especializadas
('SALA_REORG_NEURO', 'Sala de reorganização neurológica', 'INT', NOW(), NOW()),
('SALA_TERAPIA_OCUP', 'Sala de terapia ocupacional', 'INT', NOW(), NOW()),

-- Salas de produção
('SALA_SERIGRAFIA', 'Sala de serigrafia', 'INT', NOW(), NOW()),
('SALA_MARCENARIA', 'Sala de marcenaria', 'INT', NOW(), NOW()),

-- Estruturas externas
('QUIOSQUE', 'Quiosques', 'INT', NOW(), NOW())

ON CONFLICT (metric_code) DO UPDATE SET
  metric_name = EXCLUDED.metric_name,
  data_type = EXCLUDED.data_type,
  updated_at = NOW();

-- Criar índices otimizados para as métricas mais comuns
CREATE INDEX IF NOT EXISTS idx_metrics_salas_aula_v11 
  ON school.school_metrics_jsonb (((metrics->>'SALAS_AULA')::bigint));

CREATE INDEX IF NOT EXISTS idx_metrics_biblioteca_v11 
  ON school.school_metrics_jsonb (((metrics->>'BIBLIOTECA')::bigint));

CREATE INDEX IF NOT EXISTS idx_metrics_lab_info_v11 
  ON school.school_metrics_jsonb (((metrics->>'LAB_INFO')::bigint));

CREATE INDEX IF NOT EXISTS idx_metrics_quadra_coberta_v11 
  ON school.school_metrics_jsonb (((metrics->>'QUADRA_COBERTA')::bigint));

-- Comentário para histórico
COMMENT ON TABLE school.metric_dictionary IS 'Dicionário completo de métricas baseado no arquivo DIC_06_Escolas_Dependencias.csv - V11 - Todos os 173 campos de infraestrutura escolar'; 