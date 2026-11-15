# Territory Predictive Insights — Risco de Alagamento

Global Solution 2º/2026 · POO · Tema **CitizenTech**
Recorte escolhido: 🌊 **Clima local — chuva intensa e risco de alagamento em pontos vulneráveis.**

Plataforma de console que lê sensores (simulados) instalados em pontos vulneráveis de um bairro,
grava todo o histórico em banco de dados (H2 via JDBC), faz previsões simples sobre esse histórico
e emite alertas explicados (ATENÇÃO, ALERTA, CRÍTICO) quando a previsão estoura os limiares.

---

## 1. Público beneficiado

- **Quem sofre o problema:** moradores das áreas baixas e das encostas de um bairro sujeito a
  alagamentos e deslizamentos (aqui, a região fictícia *Vale do Córrego Fundo*), principalmente as
  famílias que moram na beira do córrego.
- **Quem usa o sistema:** a coordenação da associação de moradores / Núcleo Comunitário de Defesa Civil (NUDEC).
- **Qual decisão o sistema apoia:** *"precisamos agir hoje?"* — limpar bueiros e canaletas (ATENÇÃO),
  avisar a vizinhança e preparar o abrigo comunitário (ALERTA) ou acionar a Defesa Civil e retirar
  preventivamente as famílias da área de risco (CRÍTICO).

> O território é descrito apenas por região, pontos de monitoramento e números agregados.
> Nenhum dado pessoal (nome, CPF, endereço) é usado.

---

## 2. Como rodar

### Pré-requisitos
- **JDK 17 ou superior** (`java -version`)
- **Maven 3.8+** (`mvn -version`) — ou abrir o projeto no IntelliJ/Eclipse, que já trazem Maven

### Rodar os testes
```bash
mvn test
```

### Rodar o sistema (menu)
```bash
mvn -q compile exec:java
```

### Banco de dados
- Não precisa instalar nada: o **H2 roda embutido** e as tabelas são criadas automaticamente na primeira execução
  (`ConexaoBanco.inicializarEsquema()`, com `CREATE TABLE IF NOT EXISTS`).
- O banco fica no arquivo `./dados/territorio.mv.db`, por isso **o histórico sobrevive ao reiniciar o programa**.
- Para começar do zero, apague a pasta `dados/`.
- Para usar um banco só em memória (ex.: na correção): `mvn -q compile exec:java -Dbanco.url=jdbc:h2:mem:territorio`
  (nesse modo os dados somem ao fechar).

### Roteiro rápido de demonstração
1. Opção **2** logo de início → o sistema se cala, pois ainda não há histórico suficiente.
1. Opção **1** → digite `20` rodadas (gera leituras para todos os sensores; aparecem falhas e leituras descartadas de propósito).
   A simulação representa uma frente de chuva que vai se intensificando, por isso os valores tendem a subir.
2. Opção **2** → `3` (todas as estratégias): previsões, significado e alertas.
3. Opção **4** → alertas gravados. Opção **3** → histórico de um sensor.
4. Opção **0**, rode de novo e veja que o histórico e os alertas continuam lá.
5. Opção **5 → 4** para mudar limiares. **Código de acesso: `DC2026`**. Teste também com código errado.
6. Opção **7** exporta o histórico de um sensor para `exportacoes/historico-<sensor>.csv` (bônus).

---

## 3. Estrutura

```
src/main/java/
├── dominio/       ComponenteTerritorial (abstrata), PontoMonitoramento, StatusComponente,
│                  Sensor (interface), SensorSimulado, SensorPluviometro, SensorNivelCorrego,
│                  SensorUmidadeSolo, TipoSensor, DadosTerritorio, Limiar,
│                  EstrategiaPrevisao (abstrata), PrevisaoMediaMovel, PrevisaoComparacao,
│                  TipoEstrategia, ResultadoPrevisao, Medicao, Alerta, NivelAlerta
├── excecao/       DadosInvalidosException, FonteIndisponivelException, PersistenciaException
├── persistencia/  ConexaoBanco, MedicaoDAO, AlertaDAO, ExportadorCsv
├── servico/       ServicoMonitoramento, FabricaPrevisao, ConfiguracaoSistema
└── SistemaMonitoramento.java   (menu principal com Scanner)
src/test/java/
├── dominio/       MedicaoTest, PrevisaoMediaMovelTest, PrevisaoComparacaoTest, DadosTerritorioTest
└── servico/       ServicoMonitoramentoTest (Mockito), FabricaPrevisaoTest
```

### Território simulado
| Ponto | Local | Sensores |
|---|---|---|
| PT-01 | Baixada da Rua do Córrego | P-01 pluviômetro (mm/h), N-01 nível do córrego (m), U-01 umidade do solo (%) |
| PT-02 | Encosta do Morro Alto | P-02 pluviômetro (mm/h), U-02 umidade do solo (%) |

Limiares padrão (`ConfiguracaoSistema`):

| Tipo | ATENÇÃO | ALERTA | CRÍTICO |
|---|---|---|---|
| Pluviômetro (mm/h) | 20 | 35 | 50 |
| Nível do córrego (m) | 2,5 | 3,5 | 4,5 |
| Umidade do solo (%) | 70 | 85 | 95 |

---

## 4. Decisões da equipe

**Domínio e polimorfismo**
- `ComponenteTerritorial` é abstrata (id, nome, região, status) com o método abstrato `verificarStatus()`.
  `PontoMonitoramento` é a filha concreta: o status depende de quantos sensores instalados estão funcionando.
- `Sensor` é uma interface (`lerValor()`, `estaFuncionando()`, `getTipo()`) com 3 implementações coerentes com
  alagamento: pluviômetro, nível do córrego e umidade do solo. A classe abstrata `SensorSimulado` concentra o que
  elas têm em comum (passeio aleatório com `Random` e leve tendência de alta, simulando chuva que se intensifica; 4% de chance de falha de comunicação e 4% de leitura corrompida),
  e cada filha define só a sua variação e o seu tipo.
- `EstrategiaPrevisao` é abstrata; `PrevisaoMediaMovel` e `PrevisaoComparacao` sobrescrevem `prever()` e
  `leiturasNecessarias()`, chamam `super(nome, janela)` no construtor e `super.descrever()` ao se descrever.
  - **Média móvel:** média das últimas N leituras — estável, ignora picos isolados.
  - **Comparação de períodos:** compara a média das últimas N com a das N anteriores e projeta a variação
    (`recente + (recente − anterior)`) — reage mais rápido quando a chuva está piorando.
- A escolha da estratégia **não usa `if tipo ==` nem `instanceof`**: o menu escolhe um valor do enum
  `TipoEstrategia` e a `FabricaPrevisao` busca o construtor num `EnumMap`.

**Exceções**
- `DadosInvalidosException` (verificada): medição fora da faixa física (ex.: chuva negativa), população ou domicílios
  negativos, limiar fora de ordem, código de acesso errado e histórico insuficiente para prever.
  É lançada nos construtores/setters (`Medicao`, `Limiar`, `DadosTerritorio`, `ComponenteTerritorial`)
  e nos serviços (`EstrategiaPrevisao.validarHistorico`).
- `FonteIndisponivelException` (verificada): sensor em manutenção ou que não respondeu.
- `PersistenciaException` (não verificada): envolve `SQLException` dizendo qual operação de banco falhou.
- Todas são capturadas no menu com mensagens como `[DESCARTADA] leitura não gravada: medição de Pluviômetro fora da
  faixa no sensor P-01: -3,00 mm/h` ou `[FALHA] sensor N-01 não respondeu`. Não existe `catch` vazio.
- Leitura inválida **não é gravada** — só entra no banco o que passou na validação.

**Persistência (caminho JDBC)**
- `MedicaoDAO` e `AlertaDAO` usam somente `PreparedStatement` com parâmetros `?` (nenhum SQL concatenado) e
  `try-with-resources` para `Connection`, `PreparedStatement` e `ResultSet`.
- Escolhemos JDBC + H2 em arquivo porque é o caminho mais simples de rodar na correção (zero instalação de banco)
  e deixa visível o que o JPA faria por baixo dos panos.
- As previsões são feitas **sobre o histórico do banco** (`buscarUltimasPorSensor`), não sobre a memória.

**Padrões criacionais (justificativa)**
- **Factory — `FabricaPrevisao`:** criar uma estratégia exige saber qual classe instanciar e com qual janela
  (que vem da configuração). Colocamos isso em um único lugar para que o serviço e o menu nunca deem `new` em
  estratégia nem façam `if/switch` por tipo. Para adicionar uma terceira estratégia basta uma linha no mapa da fábrica.
  Não usamos Factory para `Medicao` ou `Alerta` porque eles têm um único tipo e o construtor já valida tudo — uma
  fábrica ali só adicionaria uma camada sem ganho.
- **Singleton — `ConfiguracaoSistema`:** limiares e janela de previsão precisam ser **os mesmos** para o serviço,
  para a fábrica e para o menu. Com construtor privado e `getInstancia()`, quando a coordenação muda um limiar
  (via `DadosTerritorio.alterarLimiar` com código de acesso) todos passam a usar o novo valor na hora.
  Não usamos Singleton nos DAOs ou no serviço porque eles recebem dependências pelo construtor —
  isso é o que permite trocá-los por mocks nos testes.

**Testes (JUnit 5 + Mockito)** — `mvn test`
- 23 testes no padrão AAA (Arrange, Act, Assert).
- `assertThrows` nas exceções customizadas: chuva negativa, umidade acima de 100%, limiar fora de ordem,
  código de acesso errado, população negativa, histórico insuficiente, falha de sensor.
- `ServicoMonitoramentoTest` usa **Mockito** para mockar `MedicaoDAO`, `AlertaDAO` e `Sensor`:
  testa a lógica de alerta (CRÍTICO, ATENÇÃO, nenhum alerta) sem banco, verificando com `verify`/`ArgumentCaptor`
  que o alerta foi gravado — ou que **nada** foi gravado quando a leitura é inválida.
- Os testes conferem valores calculados (ex.: média de 10, 20, 30 = 20; comparação 8,12 → 18,22 prevê 30):
  se o cálculo errar, o teste fica vermelho.

**Bônus:** exportação do histórico consultado para CSV (opção 7 do menu, classe `ExportadorCsv`).

---

## 5. Reflexão crítica

**Quando o sistema deve se calar?**
Com poucas leituras, qualquer "previsão" é chute. Por isso a média móvel exige pelo menos 5 leituras do sensor no
banco (a janela padrão) e a comparação de períodos exige o dobro (10), porque precisa de dois períodos para comparar.
Abaixo disso o sistema responde *"sistema em silêncio — histórico insuficiente"* em vez de mostrar um número.
Mesmo acima do mínimo, as leituras simuladas são próximas no tempo; em uso real seria preciso também considerar
há quanto tempo foi a última leitura — um sensor parado há 6 horas não descreve a chuva de agora — e se calar quando
o sensor está em manutenção ou falhando muito.

**O que os dados não representam?**
Um pluviômetro mede a chuva em um ponto; o alagamento depende de bueiro entupido, lixo no córrego, impermeabilização
do solo e da chuva que caiu rio acima, fora do bairro. Um sensor por ponto diz pouco sobre a rua ao lado. As médias
também escondem extremos: 5 leituras de 10 mm/h e 1 de 80 mm/h dão a mesma média que uma chuva constante moderada.
E os valores aqui são simulados com `Random`: servem para exercitar o sistema, não para descrever o clima real do território.

**Por que o alerta apoia a decisão, em vez de decidir?**
Retirar famílias de casa tem custo humano e social alto; não agir também. Quem conhece o território — onde a água
sobe primeiro, quem tem mobilidade reduzida, se o abrigo está disponível — é a coordenação comunitária e a
Defesa Civil, não o programa. Por isso cada alerta **explica o porquê** (qual sensor, qual valor previsto, qual
estratégia, qual limite foi estourado) e traz uma ação *sugerida*, deixando a pessoa conferir, discordar e decidir.
Os limiares também ficam sob controle humano: só quem tem o código de acesso pode alterá-los.

---

## 6. Uso de IA

Usamos um assistente de IA (Claude, da Anthropic) como apoio para montar a estrutura inicial do projeto, sugerir
a organização em pacotes, rascunhar os testes e revisar textos deste README.
**Como revisamos:** cada integrante leu e executou a parte pela qual ficou responsável (seção 7), rodou
`mvn test` e o roteiro de demonstração, ajustou nomes, mensagens e limiares para o nosso recorte e conferiu
os requisitos da GS um a um. Todos os integrantes conseguem explicar qualquer trecho do código.

> ⚠️ *Equipe: ajustem esta seção para refletir exatamente como vocês usaram a IA e revisaram o código.*

---

## 7. Divisão de tarefas

| Integrante (RM) | Responsabilidade |
|---|---|
| _Nome 1 — RM00000_ | Domínio (`ComponenteTerritorial`, `Sensor` e implementações, `DadosTerritorio`) e exceções |
| _Nome 2 — RM00000_ | Persistência (`ConexaoBanco`, `MedicaoDAO`, `AlertaDAO`, CSV) |
| _Nome 3 — RM00000_ | Previsões, alertas, padrões (`FabricaPrevisao`, `ConfiguracaoSistema`) e testes |
