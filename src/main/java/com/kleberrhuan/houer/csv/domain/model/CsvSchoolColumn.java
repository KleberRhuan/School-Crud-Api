/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.domain.model;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;

@Getter
public enum CsvSchoolColumn {
  NOMEDEP(new ColumnMetadata(true, false, "Nome da rede de ensino", false)),
  DE(new ColumnMetadata(true, false, "Nome da diretoria de ensino", false)),
  MUN(new ColumnMetadata(true, false, "Nome do município", false)),
  DISTR(new ColumnMetadata(true, false, "Nome do distrito", false)),
  CODESC(new ColumnMetadata(true, false, "Código da escola", false)),
  NOMESC(new ColumnMetadata(true, false, "Nome da escola", false)),
  TIPOESC(new ColumnMetadata(true, false, "Tipo da escola (numérico)", true)),
  TIPOESC_DESC(new ColumnMetadata(true, false, "Tipo descrição escola", false)),
  CODSIT(new ColumnMetadata(true, false, "Código situação escola", false)),
  SALAS_AULA(new ColumnMetadata(true, true, "Sala de aula", true)),
  SALAS_ED_INF(
    new ColumnMetadata(true, true, "Sala de educação infantil", true)
  ),
  SALAS_ED_ESP(
    new ColumnMetadata(true, true, "Sala de educação especial", true)
  ),
  SALAS_ED_ART(
    new ColumnMetadata(true, true, "Sala de educação artística", true)
  ),
  SALA_RECURSO(new ColumnMetadata(true, true, "Sala recurso", true)),
  TOT_SALAS_AULA(new ColumnMetadata(true, true, "Total salas aula", true)),
  AUDITORIO(new ColumnMetadata(true, true, "Auditório", true)),
  ANFITEATRO(new ColumnMetadata(true, true, "Anfiteatro", true)),
  TEATRO(new ColumnMetadata(true, true, "Teatro", true)),
  CANTINA(new ColumnMetadata(true, true, "Cantina", true)),
  COPA(new ColumnMetadata(true, true, "Copa", true)),
  COZINHA(new ColumnMetadata(true, true, "Cozinha", true)),
  REFEITORIO(new ColumnMetadata(true, true, "Refeitório", true)),
  DEPOSITO_ALIMENTOS(
    new ColumnMetadata(true, true, "Depósito de alimentos", true)
  ),
  DESPENSA(new ColumnMetadata(true, true, "Despensa", true)),
  TOT_DESPENSA(new ColumnMetadata(true, true, "Total despensa", true)),
  SALA_LEITURA(new ColumnMetadata(true, true, "Sala de leitura", true)),
  BIBLIOTECA(new ColumnMetadata(true, true, "Biblioteca", true)),
  TOT_SALA_LEITURA(new ColumnMetadata(true, true, "Total sala leitura", true)),
  QUADRA_COBERTA(new ColumnMetadata(true, true, "Quadra coberta", true)),
  QUADRA_DESCOBERTA(new ColumnMetadata(true, true, "Quadra descoberta", true)),
  GINASIO(new ColumnMetadata(true, true, "Ginásio", true)),
  TOT_QUADRA(new ColumnMetadata(true, true, "Total quadras", true)),
  QUADRA_AREIA(new ColumnMetadata(true, true, "Quadra de areia", true)),
  QUADRA_GRAMA(new ColumnMetadata(true, true, "Quadra de grama", true)),
  CAMPO_FUTEBOL(new ColumnMetadata(true, true, "Campo de futebol", true)),
  GABINETE_DENTARIO(new ColumnMetadata(true, true, "Gabinete dentário", true)),
  CONSULTORIO_MEDICO(
    new ColumnMetadata(true, true, "Consultório médico", true)
  ),
  ENFERMARIA(new ColumnMetadata(true, true, "Enfermaria", true)),
  AMBULATORIO(new ColumnMetadata(true, true, "Ambulatório", true)),
  ALMOXARIFADO(new ColumnMetadata(true, true, "Almoxarifado", true)),
  ARQUIVO(new ColumnMetadata(true, true, "Arquivo", true)),
  REPROGRAFIA(new ColumnMetadata(true, true, "Reprografia", true)),
  SALA_GREMIO(new ColumnMetadata(true, true, "Sala do grêmio", true)),
  DIRETORIA(new ColumnMetadata(true, true, "Diretoria", true)),
  VICEDIRETORIA(new ColumnMetadata(true, true, "Vice-diretoria", true)),
  SALA_PROF(new ColumnMetadata(true, true, "Sala dos professores", true)),
  SECRETARIA(new ColumnMetadata(true, true, "Secretaria", true)),
  SALA_ORIENT_ED(
    new ColumnMetadata(true, true, "Sala orientação educacional", true)
  ),
  SALA_COORD_PEDAG(
    new ColumnMetadata(true, true, "Sala coordenação pedagógica", true)
  ),
  PATIO_COBERTO(new ColumnMetadata(true, true, "Pátio coberto", true)),
  PATIO_DESCOBERTO(new ColumnMetadata(true, true, "Pátio descoberto", true)),
  ZELADORIA(new ColumnMetadata(true, true, "Zeladoria", true)),
  VESTIARIO_FEM(new ColumnMetadata(true, true, "Vestiário feminino", true)),
  VESTIARIO_MASC(new ColumnMetadata(true, true, "Vestiário masculino", true)),
  TOT_VESTIARIO(new ColumnMetadata(true, true, "Total vestiários", true)),
  VIDEOTECA(new ColumnMetadata(true, true, "Videoteca", true)),
  SALA_TV(new ColumnMetadata(true, true, "Sala de TV", true)),
  LAB_INFO(new ColumnMetadata(true, true, "Laboratório de informática", true)),
  LAB_CIENCIAS(new ColumnMetadata(true, true, "Laboratório de ciências", true)),
  LAB_FISICA(new ColumnMetadata(true, true, "Laboratório de física", true)),
  LAB_QUIMICA(new ColumnMetadata(true, true, "Laboratório de química", true)),
  LAB_BIOLOGIA(new ColumnMetadata(true, true, "Laboratório de biologia", true)),
  LAB_CIENCIA_FISICA_BIOLOGICA(
    new ColumnMetadata(true, true, "Lab. ciência física biológica", true)
  ),
  TOT_LAB_CIENCIA(new ColumnMetadata(true, true, "Total lab. ciências", true)),
  LAB_LINGUAS(new ColumnMetadata(true, true, "Laboratório de línguas", true)),
  LAB_MULTIUSO(new ColumnMetadata(true, true, "Laboratório multiuso", true)),
  OFICINA(new ColumnMetadata(true, true, "Oficina", true)),
  PLAYGROUND(new ColumnMetadata(true, true, "Playground", true)),
  DORMITORIO(new ColumnMetadata(true, true, "Dormitório", true)),
  BERCARIO(new ColumnMetadata(true, true, "Berçário", true)),
  SANITARIO_ADEQ_PRE(
    new ColumnMetadata(true, true, "Sanitário adequado pré-escola", true)
  ),
  SANITARIO_ADEQ_PRE_FEM(
    new ColumnMetadata(
      true,
      true,
      "Sanitário adequado pré-escola feminino",
      true
    )
  ),
  SANITARIO_ADEQ_PRE_MASC(
    new ColumnMetadata(
      true,
      true,
      "Sanitário adequado pré-escola masculino",
      true
    )
  ),
  SANITARIO_ADEQ_DEF(
    new ColumnMetadata(true, true, "Sanitário adequado deficientes", true)
  ),
  SANITARIO_ADEQ_DEF_MASC(
    new ColumnMetadata(
      true,
      true,
      "Sanitário adequado deficientes masculino",
      true
    )
  ),
  SANITARIO_ADEQ_DEF_FEM(
    new ColumnMetadata(
      true,
      true,
      "Sanitário adequado deficientes feminino",
      true
    )
  ),
  SANITARIO_AL_MASC(
    new ColumnMetadata(true, true, "Sanitário alunos masculino", true)
  ),
  SANITARIO_AL_FEM(
    new ColumnMetadata(true, true, "Sanitário alunos feminino", true)
  ),
  TOT_SANITARIO_AL(
    new ColumnMetadata(true, true, "Total sanitários alunos", true)
  ),
  SANITARIO_FUNC_FEM(
    new ColumnMetadata(true, true, "Sanitário funcionários feminino", true)
  ),
  SANITARIO_FUNC_MASC(
    new ColumnMetadata(true, true, "Sanitário funcionários masculino", true)
  ),
  TOT_SANITARIO_FUNC(
    new ColumnMetadata(true, true, "Total sanitários funcionários", true)
  ),

  DEPEND_ADEQ_DEF(
    new ColumnMetadata(true, true, "Dependências adequadas deficientes", true)
  ),
  SALA_ED_FISICA(new ColumnMetadata(true, true, "Sala educação física", true)),
  PISCINA(new ColumnMetadata(true, true, "Piscina", true)),
  PORTARIA(new ColumnMetadata(true, true, "Portaria", true)),

  SALA_PROG_ESC_FAMILIA(
    new ColumnMetadata(true, true, "Sala programa escola-família", true)
  ),
  BRINQUEDOTECA(new ColumnMetadata(true, true, "Brinquedoteca", true)),
  FRALDARIO(new ColumnMetadata(true, true, "Fraldário", true)),
  LACTARIO(new ColumnMetadata(true, true, "Lactário", true)),
  LAVANDERIA(new ColumnMetadata(true, true, "Lavanderia", true)),
  SOLARIO(new ColumnMetadata(true, true, "Solário", true)),

  SALA_ESPERA(new ColumnMetadata(true, true, "Sala de espera", true)),
  SALA_INSPETOR(new ColumnMetadata(true, true, "Sala do inspetor", true)),
  SALA_REUNIAO(new ColumnMetadata(true, true, "Sala de reunião", true)),
  TESOURARIA(new ColumnMetadata(true, true, "Tesouraria", true)),
  SALA_REFORCO(new ColumnMetadata(true, true, "Sala de reforço", true)),
  SALA_DIRETOR_TECNICO(
    new ColumnMetadata(true, true, "Sala diretor técnico", true)
  ),
  GARAGEM_ONIBUS(new ColumnMetadata(true, true, "Garagem ônibus", true)),

  SALA_FISIOTERAPIA(
    new ColumnMetadata(true, true, "Sala de fisioterapia", true)
  ),
  SALA_PSICOLOGIA(new ColumnMetadata(true, true, "Sala de psicologia", true)),
  SALA_FONOAUDIOLOGIA(
    new ColumnMetadata(true, true, "Sala de fonoaudiologia", true)
  ),
  SALA_EVENTOS(new ColumnMetadata(true, true, "Sala de eventos", true)),
  SALA_ASSIST_SOCIAL(
    new ColumnMetadata(true, true, "Sala assistência social", true)
  ),
  SALA_TERAPIA_EDUC(
    new ColumnMetadata(true, true, "Sala terapia educacional", true)
  ),

  ABATEDOURO(new ColumnMetadata(true, true, "Abatedouro", true)),
  ALOJAMENTO_FEM(new ColumnMetadata(true, true, "Alojamento feminino", true)),
  ALOJAMENTO_MASC(new ColumnMetadata(true, true, "Alojamento masculino", true)),
  TOT_ALOJAMENTO(new ColumnMetadata(true, true, "Total alojamentos", true)),
  AREA_SERVICO(new ColumnMetadata(true, true, "Área de serviço", true)),
  BAZAR(new ColumnMetadata(true, true, "Bazar", true)),
  CASA_MAQUINA(new ColumnMetadata(true, true, "Casa de máquina", true)),
  CASA_FUNC(new ColumnMetadata(true, true, "Casa de funcionários", true)),
  CHURRASQUEIRA(new ColumnMetadata(true, true, "Churrasqueira", true)),
  DEPOSITOS_CEREAIS(
    new ColumnMetadata(true, true, "Depósitos de cereais", true)
  ),
  ELEVADOR(new ColumnMetadata(true, true, "Elevador", true)),
  ESTACIONAMENTO(new ColumnMetadata(true, true, "Estacionamento", true)),
  ESTUFA(new ColumnMetadata(true, true, "Estufa", true)),

  GALPAO_AVES_CORTE(
    new ColumnMetadata(true, true, "Galpão aves de corte", true)
  ),
  GALPAO_AVES_POSTURA(
    new ColumnMetadata(true, true, "Galpão aves de postura", true)
  ),
  GALPAO_BOVINOS_LEITE(
    new ColumnMetadata(true, true, "Galpão bovinos de leite", true)
  ),
  GALPAO_CUNICULTURA(
    new ColumnMetadata(true, true, "Galpão cunicultura", true)
  ),
  GALPAO_MAQ_AGRICOLA(
    new ColumnMetadata(true, true, "Galpão máquinas agrícolas", true)
  ),
  GALPAO_OVINOS_CAPRINOS(
    new ColumnMetadata(true, true, "Galpão ovinos e caprinos", true)
  ),
  GALPAO_SUINO(new ColumnMetadata(true, true, "Galpão suíno", true)),

  GRAFICA(new ColumnMetadata(true, true, "Gráfica", true)),
  HORTA(new ColumnMetadata(true, true, "Horta", true)),
  LAB_DIDATICA(new ColumnMetadata(true, true, "Laboratório de didática", true)),
  LAB_JUNIOR(new ColumnMetadata(true, true, "Laboratório júnior", true)),
  LAB_ENFERMAGEM(
    new ColumnMetadata(true, true, "Laboratório de enfermagem", true)
  ),
  LAB_ESTETICA(new ColumnMetadata(true, true, "Laboratório de estética", true)),
  LAB_PSICOPEDAGOGIA(
    new ColumnMetadata(true, true, "Laboratório de psicopedagogia", true)
  ),
  LAB_TURISMO(new ColumnMetadata(true, true, "Laboratório de turismo", true)),
  LAVATORIO(new ColumnMetadata(true, true, "Lavatório", true)),
  MANGUEIRA(new ColumnMetadata(true, true, "Mangueira", true)),
  MINHOCARIO(new ColumnMetadata(true, true, "Minhocário", true)),
  PACKING_HOUSE(new ColumnMetadata(true, true, "Packing house", true)),
  POMAR(new ColumnMetadata(true, true, "Pomar", true)),
  PSICULTURA(new ColumnMetadata(true, true, "Piscicultura", true)),

  RECEPCAO(new ColumnMetadata(true, true, "Recepção", true)),
  SALA_ATENDIMENTO(new ColumnMetadata(true, true, "Sala de atendimento", true)),
  SALA_ATEND_PSICOLOGICO(
    new ColumnMetadata(true, true, "Sala atendimento psicológico", true)
  ),
  SALA_AUX_COORDENACAO(
    new ColumnMetadata(true, true, "Sala auxiliar coordenação", true)
  ),
  SALA_DADOS(new ColumnMetadata(true, true, "Sala de dados", true)),
  SALA_DEP_PESSOAL(
    new ColumnMetadata(true, true, "Sala departamento pessoal", true)
  ),
  SALA_ED_RELIGIOSA(
    new ColumnMetadata(true, true, "Sala educação religiosa", true)
  ),
  SALA_ENERGIA_ELETRICA(
    new ColumnMetadata(true, true, "Sala energia elétrica", true)
  ),
  SALA_ENTRETENIMENTO(
    new ColumnMetadata(true, true, "Sala de entretenimento", true)
  ),
  SALA_ESTAGIO(new ColumnMetadata(true, true, "Sala de estágio", true)),
  SALA_GINASTICA(new ColumnMetadata(true, true, "Sala de ginástica", true)),
  SALA_INSUMO_AGRICOLA(
    new ColumnMetadata(true, true, "Sala insumo agrícola", true)
  ),
  SALA_INSUMO_VETERINARIO(
    new ColumnMetadata(true, true, "Sala insumo veterinário", true)
  ),
  SALA_MARKETING(new ColumnMetadata(true, true, "Sala de marketing", true)),
  SALA_MATRICULA(new ColumnMetadata(true, true, "Sala de matrícula", true)),
  SALA_MUSICA(new ColumnMetadata(true, true, "Sala de música", true)),
  SALA_POS_GRADUACAO(
    new ColumnMetadata(true, true, "Sala pós-graduação", true)
  ),
  SALA_ORDENHA(new ColumnMetadata(true, true, "Sala de ordenha", true)),
  SALA_PROC_PROD_AGROPECUARIOS(
    new ColumnMetadata(
      true,
      true,
      "Sala processamento produtos agropecuários",
      true
    )
  ),
  SALA_SEGURANCA(new ColumnMetadata(true, true, "Sala de segurança", true)),
  SALA_TELEFONIA(new ColumnMetadata(true, true, "Sala de telefonia", true)),
  SALA_FINANCEIRO(new ColumnMetadata(true, true, "Sala financeiro", true)),
  SALA_PASTORAL(new ColumnMetadata(true, true, "Sala pastoral", true)),
  SALA_RESERV_AGUA(
    new ColumnMetadata(true, true, "Sala reservatório água", true)
  ),

  SERVIDOR(new ColumnMetadata(true, true, "Servidor", true)),
  SILO(new ColumnMetadata(true, true, "Silo", true)),
  VARANDA(new ColumnMetadata(true, true, "Varanda", true)),
  VIVEIRO(new ColumnMetadata(true, true, "Viveiro", true)),

  SALA_REORG_NEURO(
    new ColumnMetadata(true, true, "Sala reorganização neurológica", true)
  ),
  SALA_TERAPIA_OCUP(
    new ColumnMetadata(true, true, "Sala terapia ocupacional", true)
  ),
  SALA_SERIGRAFIA(new ColumnMetadata(true, true, "Sala de serigrafia", true)),
  SALA_MARCENARIA(new ColumnMetadata(true, true, "Sala de marcenaria", true)),
  QUIOSQUE(new ColumnMetadata(true, true, "Quiosque", true));

  private record ColumnMetadata(
    boolean required,
    boolean metric,
    String description,
    boolean numeric
  ) {}

  private final ColumnMetadata metadata;

  CsvSchoolColumn(ColumnMetadata metadata) {
    this.metadata = metadata;
  }

  public boolean isMetric() {
    return metadata.metric();
  }

  public String getDescription() {
    return metadata.description();
  }

  public boolean isNumeric() {
    return metadata.numeric();
  }

  public boolean isRequired() {
    return metadata.required();
  }

  public static Set<CsvSchoolColumn> getRequiredHeaders() {
    return Arrays
      .stream(values())
      .filter(column -> column.isRequired() && !column.isMetric()) // Apenas colunas obrigatórias e não métricas
      .collect(Collectors.toUnmodifiableSet());
  }

  public static Set<CsvSchoolColumn> getMetricColumns() {
    return Stream
      .of(values())
      .filter(CsvSchoolColumn::isMetric)
      .collect(Collectors.toUnmodifiableSet());
  }

  public static Set<CsvSchoolColumn> getAllColumns() {
    return Stream.of(values()).collect(Collectors.toUnmodifiableSet());
  }
}
