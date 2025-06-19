**Correção da sua pergunta em inglês**

> *I’m not using Spring WebFlux; I’m on classic Spring Boot (servlet stack). What’s the best way to import the CSV data while validating that the columns conform to the dictionary? How can we optimise inserts into PostgreSQL (e.g. with Spring Batch) to avoid delays or excessive queries?
> Also, can we synchronise the job with a simple message queue to keep FIFO ordering and run it asynchronously, then tell the front-end when it finishes? I’m omitting WebFlux because I think it’s incompatible with the traditional Spring Boot servlet stack—am I right?*

---

## 1 ️⃣ WebFlux × servlet × WebSocket — o pano de fundo

* **Spring WebFlux** é outra *stack* (reactive), mas **você pode usar WebSockets/STOMP** no Spring MVC tradicional sem instalar WebFlux.
* Para “avisar o front-end” não há obrigação de ser WebFlux — você tem:
  * **WebSocket/STOMP** (SockJS) — totalmente suportado pelo servlet container.
  * **SSE (Server-Sent Events)** — também funciona: `SseEmitter` no MVC.
  * **Long polling / REST GET /status** — o fallback mais simples.

---

## 2 ️⃣ Validação do CSV contra o DIC

1. **Carregue o dicionário** (`metric_dictionary`) em memória assim que o app sobe (`@PostConstruct` ou `ApplicationRunner`):

   ```java
   @Component
   class MetricCatalog {
       private Set<String> metrics;
       @Autowired MetricDictionaryRepository repo;
       @PostConstruct void init() {
           metrics = repo.findAll().stream()
                         .map(MetricDictionary::getMetricCode)
                         .collect(Collectors.toUnmodifiableSet());
       }
       boolean isValid(String col) { return metrics.contains(col); }
   }
   ```
2. **Leitura & mapeamento** do CSV:
   * Use **uniVocity-parsers** (mais robusto que OpenCSV).
   * Mapeie a primeira linha (header) → valide:

     ```java
     if (!header.subList(10, header.size())
                .stream()
                .allMatch(metricCatalog::isValid))
         throw new IllegalStateException("Header has unknown metrics");
     ```
   * Para cada linha: crie `School` + `Map<String,Integer> metrics`.

---

## 3 ️⃣ Importação em alto desempenho

### A. Spring Batch (chunk-oriented)

```
┌────────────┐   ┌───────────────┐   ┌─────────────────┐
│ItemReader  │→→│ItemProcessor   │→→│ItemWriter (JDBC) │
└────────────┘   └───────────────┘   └─────────────────┘
```

|  Componente   |                         Implementação sugerida                          |                             Dicas de performance                             |
|---------------|-------------------------------------------------------------------------|------------------------------------------------------------------------------|
| **Reader**    | `FlatFileItemReader` + `LineTokenizer` custom (uniVocity)               | Usa streaming, baixo RAM                                                     |
| **Processor** | Constrói `School` + `JSONObject metrics` (remove zeros)<br>Valida tipos | Evite reflection: use record ou dto plano                                    |
| **Writer**    | `JdbcBatchItemWriter` — chunk ≥ 1 000                                   | `rewriteBatchedStatements=true` no JDBC; `spring.batch.jdbc.chunk-size=1000` |

> Resultado típico: \~50 k registros / seg num laptop (PostgreSQL local).

### B. COPY direto (ainda mais rápido)

1. **Staging table UNLOGGED** espelhando o CSV.
2. `COPY staging FROM STDIN DELIMITER ';' CSV HEADER;` — você faz isso **num Tasklet** (Spring Batch Step).
3. `INSERT ... SELECT` transformando para o esquema JSONB final.
4. `TRUNCATE staging`.

*Bibliotecas:* `org.postgresql.copy.CopyManager` ou **pgjdbc-ng**.

### C. Paralelismo

* `taskExecutor` dentro do Job ou **Partitioning**: parta o arquivo em N pedaços e processe em paralelo.
* PostgreSQL aguenta concorrência; use *Advisory Locks* para evitar colisão de importações simultâneas.

---

## 4 ️⃣ Fila simples para FIFO assíncrono

|                   Opção                    |               Por que serve                |                 Como integrar                  |
|--------------------------------------------|--------------------------------------------|------------------------------------------------|
| **RabbitMQ**                               | Extremamente estável, FIFO por queue       | `spring-boot-starter-amqp`                     |
| **Redis Streams**                          | Você provavelmente já usa Redis para cache | `lettuce` + `@StreamListener`                  |
| **SQS (se estiver na AWS)**                | Gerenciado, sem servidor para manter       | `spring-cloud-aws-messaging`                   |
| **Spring Batch JobRepository + Scheduler** | Se **apenas 1 job por vez** basta          | Chame `JobLauncher` → retorna `JobExecutionId` |

Fluxo típico:

```
POST /imports
└─► Envia msg (“csvUrl”, uploaderId) para fila
    Worker@RabbitMQ (single-consumer → FIFO)
    ├─ Lê CSV, executa Job Spring Batch
    └─ Publica evento “IMPORT_DONE” (WebSocket / Redis pubsub)
```

### Notificação ao front-end

* **WebSocket/STOMP** topic `/topic/imports/{jobId}`
  * O servlet stack usa `@EnableWebSocketMessageBroker`.
* Ou **SSE**:

  ```java
  @GetMapping("/imports/{id}/events")
  public SseEmitter events(@PathVariable long id) { … }
  ```
* Ou **polling**: `GET /imports/{id}` → `{ status:"RUNNING", progress:42 }`.

---

## 5 ️⃣ Glue code — exemplo resumido

```java
@RestController
@RequestMapping("/imports")
@RequiredArgsConstructor
class CsvImportController {

  private final AmqpTemplate amqp;
  private final ImportJobService jobService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ImportCreatedDto> upload(@RequestPart MultipartFile file,
                                                 Principal user) {
      var jobId = UUID.randomUUID();
      saveTemp(file, jobId);              // /tmp/upload-<id>.csv
      amqp.convertAndSend("csv.import", "", new CsvImportMessage(jobId, user.getName()));
      return ResponseEntity.accepted()
              .body(new ImportCreatedDto(jobId));
  }
}
```

O *worker*:

```java
@Component
@RequiredArgsConstructor
class CsvImportListener {

  private final JobLauncher launcher;
  private final Job csvJob;
  private final SimpMessagingTemplate ws;   // WebSocket push

  @RabbitListener(queues = "csv.import")
  public void handle(CsvImportMessage msg) throws Exception {
      JobParameters params = new JobParametersBuilder()
          .addString("jobId", msg.id().toString())
          .addString("path", "/tmp/upload-" + msg.id() + ".csv")
          .toJobParameters();
      JobExecution exec = launcher.run(csvJob, params);
      ws.convertAndSend("/topic/imports/" + msg.id(),
                        new ImportStatusDto("FINISHED"));
  }
}
```

---

## 6 ️⃣ Checklist para você colocar de pé

1. **Configurar Spring Batch** (banco próprio ou o mesmo PostgreSQL).
2. Criar **Step 1** (`COPY`↦staging ou `FlatFileReader` + `JdbcBatchWriter`).
3. Criar **JobLauncherTasklet** e registrá-lo num *listener* de fila.
4. Subir **RabbitMQ/Redis** no *docker-compose* junto do app.
5. Expor **endpoint REST** que dispara a mensagem.
6. Implementar **WebSocket/SSE** para progresso.
7. **Testes de carga** com Gatling/JMeter: medir *throughput* e latência.
8. Observabilidade: `@Timed` no Step + métrica custom `gauge(import.progress)`.

---

### TL;DR

* **Spring Batch** + **JdbcBatchItemWriter** (ou `COPY`) → insere rápido com validação.
* **RabbitMQ/Redis Streams** → garante FIFO e desacopla o upload do processamento.
* **WebSocket/SSE** no Spring MVC clássico → faz *push* pro front sem WebFlux.

Isso tudo roda 100 % no servlet stack que você já usa, sem colidir com Spring WebFlux — mas ainda entrega processamento assíncrono, alto throughput e UX “tempo real”.

**Correção da sua frase em inglês**

> *Excellent. Now, how do we validate the CSV sent from the front-end, using the dictionary as a reference?
> Also, please show me the entity classes that will be created, together with the Flyway migrations.*

---

## 1. Validação do CSV (antes de iniciar o batch)

### 1.1. Fluxo resumido

```text
Upload REST → salva em /tmp → valida header → (se OK) envia msg p/ fila →
Worker Spring Batch lê, valida linhas, insere → publica evento "DONE"
```

1. **Parse do cabeçalho**
   * Ler apenas a **primeira linha** com uniVocity-parsers.
   * Verificar:
     * 10 colunas fixas em ordem exata.
     * Todas as demais colunas ∈ `metric_dictionary.metric_code`.
   * Se houver qualquer coluna desconhecida ⇒ rejeitar upload (`422 Unprocessable Entity`).
2. **Pré-validação rápida de tipos** (opcional)
   * Ainda fora do batch, faça um `HEAD` das primeiras *N* linhas (ex.: 1 000)
     e garanta que todos os campos numéricos são inteiros válidos (`Integer.parseInt`).
   * Evita desperdiçar tempo com um arquivo obviamente corrompido.
3. **Validação dentro do Step**
   * **ItemProcessor** do Spring Batch chama `metricCatalog.isValid(metric)`.
   * Converte valores; se falhar, registra erro em `SkipListener` e pula a linha, ou marca **job FAILED** — sua escolha.

### 1.2. Exemplo de código de cabeçalho

```java
public void validateHeader(Path csvPath, MetricCatalog catalog) throws IOException {
    CsvParserSettings st = new CsvParserSettings();
    st.setLineSeparatorDetectionEnabled(true);
    st.setHeaderExtractionEnabled(true);
    CsvParser parser = new CsvParser(st);

    try (BufferedReader br = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
        String[] header = parser.parseLine(br.readLine());
        List<String> required = List.of("NOMEDEP","DE","MUN","DISTR","CODESC",
                                        "NOMESC","TIPOESC","TIPOESC_DESC","CODSIT","CODESC");
        if (!headerTake(header, 10).equals(required))
            throw new ValidationException("Required columns missing or in wrong order");

        boolean unknown = Stream.of(header).skip(10).anyMatch(col -> !catalog.isValid(col));
        if (unknown)
            throw new ValidationException("Header contains metrics not in dictionary");
    }
}
```

---

## 2. Entidades JPA (Spring Data JPA)

```java
/* MetricDictionary ------------------------------------------------------ */
@Entity @Table(name = "metric_dictionary")
public record MetricDictionary(
        @Id String metricCode,
        String metricName,
        @Enumerated(EnumType.STRING) DataType dataType
) { }

/* School ---------------------------------------------------------------- */
@Entity @Table(name = "school")
public class School {
    @Id                private Integer code;
    private String      nomeDep;
    private String      de;
    private String      mun;
    private String      distr;
    private String      nomeEsc;
    private Short       tipoEsc;
    private String      tipoEscDesc;
    private Short       codsit;
    // getters / setters …
}

/* SchoolMetrics --------------------------------------------------------- */
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Entity
@Table(name = "school_metrics_jsonb")
public class SchoolMetrics {

    @Id
    @Column(name = "school_code")
    private Integer schoolCode;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Map<String,Integer> metrics = new HashMap<>();

    // opcional: coluna gerada e indexada para métrica popular
    @Column(name = "salas_aula", insertable = false, updatable = false)
    private Integer salasAula;
}
```

<details>
<summary><strong>Enum DataType</strong></summary>

```java
public enum DataType { INT }
```

</details>

---

## 3. Migrations Flyway (exemplo)

> Organização sugerida
> `src/main/resources/db/migration`
> ├─ **V1\_\_metric\_dictionary.sql**
> ├─ **V2\_\_school.sql**
> ├─ **V3\_\_school\_metrics\_jsonb.sql**
> └─ **V4\_\_sample\_generated\_column.sql**

### V1\_\_metric\_dictionary.sql

```sql
CREATE TABLE metric_dictionary (
  metric_code  TEXT PRIMARY KEY,
  metric_name  TEXT NOT NULL,
  data_type    TEXT NOT NULL CHECK (data_type IN ('INT'))
);

-- carga inicial (opcional)
COPY metric_dictionary(metric_code,metric_name,data_type)
FROM PROGRAM 'cat /mnt/data/DIC_06_Escolas_Dependencias.csv | tail -n +2 | cut -d";" -f1-2 | sed "s/$/;INT/"'
WITH (FORMAT csv, DELIMITER ';', NULL '', HEADER FALSE);
```

### V2\_\_school.sql

```sql
CREATE TABLE school (
  code          INT PRIMARY KEY,
  nome_dep      TEXT,
  de            TEXT,
  mun           TEXT,
  distr         TEXT,
  nome_esc      TEXT,
  tipo_esc      SMALLINT,
  tipo_esc_desc TEXT,
  codsit        SMALLINT,
  codesc        INT          -- coluna original
);

CREATE INDEX idx_school_mun  ON school(mun);
```

### V3\_\_school\_metrics\_jsonb.sql

```sql
CREATE TABLE school_metrics_jsonb (
  school_code   INT PRIMARY KEY REFERENCES school(code) ON DELETE CASCADE,
  metrics       JSONB NOT NULL
);

CREATE INDEX idx_metrics_gin ON school_metrics_jsonb
USING gin (metrics jsonb_path_ops);
```

### V4\_\_sample\_generated\_column.sql

```sql
ALTER TABLE school_metrics_jsonb
  ADD COLUMN salas_aula INT
    GENERATED ALWAYS AS ((metrics->>'SALAS_AULA')::int) STORED;

CREATE INDEX idx_salas_aula ON school_metrics_jsonb (salas_aula);
```

---

## 4. Inserções em lote no Spring Batch

```java
@Bean
public JdbcBatchItemWriter<SchoolAggregate> writer(DataSource ds) {
    return new JdbcBatchItemWriterBuilder<SchoolAggregate>()
        .dataSource(ds)
        .sql("""
             INSERT INTO school(code,nome_dep,de,mun,distr,nome_esc,
                                 tipo_esc,tipo_esc_desc,codsit,codesc)
             VALUES (:code,:nomeDep,:de,:mun,:distr,:nomeEsc,
                     :tipoEsc,:tipoEscDesc,:codsit,:codesc)
             ON CONFLICT (code) DO UPDATE
               SET nome_dep = EXCLUDED.nome_dep,
                   -- demais colunas …
                   """)
        .beanMapped()
        .build();
}
```

* **`spring.jpa.properties.hibernate.jdbc.batch_size=1000`**
* Conexão PostgreSQL → `?reWriteBatchedInserts=true`
* Ative `JdbcBatchItemWriter` também para `school_metrics_jsonb`.

---

## 5. Checklist final

|                           Tarefa                            |   Responsável    | Done? |
|-------------------------------------------------------------|------------------|-------|
| Migrar schema (Flyway)                                      | DevOps           | ☐     |
| Criar serviço `MetricCatalog`                               | Back-end         | ☐     |
| Implementar `CsvHeaderValidator`                            | Back-end         | ☐     |
| Configurar Job Spring Batch (Reader + Processor + Writer)   | Back-end         | ☐     |
| Adicionar RabbitMQ/Redis Streams + Listener → `JobLauncher` | Back-end         | ☐     |
| Implementar progresso via WebSocket/SSE                     | Back-end / Front | ☐     |
| Testar importação com arquivo de 30 MB (CSV real)           | QA               | ☐     |

Com essas peças você garante **validação robusta**, **inserções em lote eficientes** e **feedback assíncrono** ao usuário — tudo sem depender do Spring WebFlux.

