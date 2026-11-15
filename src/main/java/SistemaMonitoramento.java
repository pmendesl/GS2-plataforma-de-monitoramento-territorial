import dominio.Alerta;
import dominio.DadosTerritorio;
import dominio.Medicao;
import dominio.PontoMonitoramento;
import dominio.ResultadoPrevisao;
import dominio.Sensor;
import dominio.SensorNivelCorrego;
import dominio.SensorPluviometro;
import dominio.SensorSimulado;
import dominio.SensorUmidadeSolo;
import dominio.StatusComponente;
import dominio.TipoEstrategia;
import dominio.TipoSensor;
import excecao.DadosInvalidosException;
import excecao.FonteIndisponivelException;
import excecao.PersistenciaException;
import persistencia.AlertaDAO;
import persistencia.ConexaoBanco;
import persistencia.ExportadorCsv;
import persistencia.MedicaoDAO;
import servico.ConfiguracaoSistema;
import servico.FabricaPrevisao;
import servico.ServicoMonitoramento;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;

/**
 * Menu principal (console). Monta os objetos, mostra as opções e captura
 * as exceções com mensagens claras para quem está operando o sistema.
 */
public class SistemaMonitoramento {

    private static final String CODIGO_ACESSO = "DC2026";

    private final Scanner entrada = new Scanner(System.in);
    private final ServicoMonitoramento servico;
    private final ConfiguracaoSistema configuracao;
    private final DadosTerritorio territorio;
    private final List<PontoMonitoramento> pontos;
    private final List<SensorSimulado> sensores = new ArrayList<>();

    public SistemaMonitoramento(ServicoMonitoramento servico, ConfiguracaoSistema configuracao,
                                DadosTerritorio territorio, List<PontoMonitoramento> pontos) {
        this.servico = servico;
        this.configuracao = configuracao;
        this.territorio = territorio;
        this.pontos = pontos;
    }

    public static void main(String[] args) {
        try {
            ConfiguracaoSistema configuracao = ConfiguracaoSistema.getInstancia();

            ConexaoBanco banco = new ConexaoBanco(configuracao.getUrlBanco());
            banco.inicializarEsquema();

            ServicoMonitoramento servico = new ServicoMonitoramento(
                    new MedicaoDAO(banco), new AlertaDAO(banco), configuracao, new FabricaPrevisao(configuracao));

            DadosTerritorio territorio = new DadosTerritorio(
                    "Vale do Córrego Fundo (região fictícia)", 12500, 340, CODIGO_ACESSO, configuracao);

            Random random = new Random();
            PontoMonitoramento baixada = new PontoMonitoramento("PT-01", "Baixada da Rua do Córrego", "Vale do Córrego - setor baixo");
            PontoMonitoramento encosta = new PontoMonitoramento("PT-02", "Encosta do Morro Alto", "Vale do Córrego - setor encosta");

            SistemaMonitoramento sistema = new SistemaMonitoramento(servico, configuracao, territorio, List.of(baixada, encosta));
            sistema.instalar(baixada, new SensorPluviometro("P-01", random));
            sistema.instalar(baixada, new SensorNivelCorrego("N-01", random));
            sistema.instalar(baixada, new SensorUmidadeSolo("U-01", random));
            sistema.instalar(encosta, new SensorPluviometro("P-02", random));
            sistema.instalar(encosta, new SensorUmidadeSolo("U-02", random));

            sistema.executar();
        } catch (PersistenciaException e) {
            System.out.println("Não foi possível iniciar o banco de dados: " + e.getMessage());
            System.out.println("Causa técnica: " + e.getCause().getMessage());
        } catch (DadosInvalidosException e) {
            System.out.println("Configuração inicial do território inválida: " + e.getMessage());
        }
    }

    private void instalar(PontoMonitoramento ponto, SensorSimulado sensor) {
        ponto.adicionarSensor(sensor);
        sensores.add(sensor);
    }

    // ------------------------------------------------------------------ menu principal

    public void executar() {
        System.out.println("=====================================================");
        System.out.println(" TERRITORY PREDICTIVE INSIGHTS - Risco de Alagamento");
        System.out.println(" " + territorio.getRegiao());
        System.out.println("=====================================================");

        boolean continuar = true;
        while (continuar) {
            System.out.println();
            System.out.println("1. Verificar sensores (ler e gravar medições)");
            System.out.println("2. Rodar previsões e alertas");
            System.out.println("3. Ver histórico de medições por sensor");
            System.out.println("4. Ver alertas emitidos");
            System.out.println("5. Gerenciar dados do território");
            System.out.println("6. Status completo");
            System.out.println("7. Exportar histórico de um sensor para CSV (bônus)");
            System.out.println("0. Sair");
            int opcao = lerInteiro("Escolha uma opção: ");
            try {
                switch (opcao) {
                    case 1 -> verificarSensores();
                    case 2 -> rodarPrevisoes();
                    case 3 -> verHistorico();
                    case 4 -> verAlertas();
                    case 5 -> gerenciarTerritorio();
                    case 6 -> statusCompleto();
                    case 7 -> exportarCsv();
                    case 0 -> continuar = false;
                    default -> System.out.println("Opção inválida. Digite um número de 0 a 7.");
                }
            } catch (PersistenciaException e) {
                System.out.println("[ERRO NO BANCO] " + e.getMessage()
                        + (e.getCause() != null ? " (causa: " + e.getCause().getMessage() + ")" : ""));
            }
        }
        System.out.println("Sistema encerrado. O histórico continua salvo no banco.");
    }

    // ------------------------------------------------------------------ 1. sensores

    private void verificarSensores() {
        int rodadas = lerInteiroNaFaixa("Quantas rodadas de leitura? (1 a 24, cada rodada lê todos os sensores): ", 1, 24);
        int gravadas = 0;
        int falhas = 0;
        int descartadas = 0;
        for (int rodada = 1; rodada <= rodadas; rodada++) {
            System.out.println("--- Rodada " + rodada + " ---");
            for (Sensor sensor : sensores) {
                try {
                    Medicao medicao = servico.registrarLeitura(sensor);
                    System.out.println("  [OK]         " + medicao);
                    gravadas++;
                } catch (FonteIndisponivelException e) {
                    System.out.println("  [FALHA]      " + e.getMessage());
                    falhas++;
                } catch (DadosInvalidosException e) {
                    System.out.println("  [DESCARTADA] leitura não gravada: " + e.getMessage());
                    descartadas++;
                }
            }
        }
        System.out.printf("Resumo: %d gravadas, %d falhas de sensor, %d leituras inválidas descartadas.%n",
                gravadas, falhas, descartadas);
    }

    // ------------------------------------------------------------------ 2. previsões

    private void rodarPrevisoes() {
        TipoEstrategia[] tipos = TipoEstrategia.values();
        System.out.println("Estratégias disponíveis:");
        for (int i = 0; i < tipos.length; i++) {
            System.out.printf("  %d. %s%n", i + 1, tipos[i].getDescricao());
        }
        System.out.printf("  %d. Todas%n", tipos.length + 1);
        int escolha = lerInteiroNaFaixa("Escolha: ", 1, tipos.length + 1);
        List<TipoEstrategia> selecionadas = escolha == tipos.length + 1 ? List.of(tipos) : List.of(tipos[escolha - 1]);

        int alertasEmitidos = 0;
        for (Sensor sensor : sensores) {
            System.out.println();
            System.out.println("Sensor " + sensor.getId() + " - " + sensor.getTipo().getDescricao());
            for (TipoEstrategia tipo : selecionadas) {
                try {
                    ResultadoPrevisao resultado = servico.preverParaSensor(sensor.getId(), tipo);
                    System.out.printf("  %s: previsto %.2f %s%n", resultado.getEstrategia(),
                            resultado.getValorPrevisto(), resultado.getTipoSensor().getUnidade());
                    System.out.println("    Como: " + resultado.getExplicacao());
                    System.out.println("    Significado: " + servico.interpretar(resultado));
                    Optional<Alerta> alerta = servico.avaliarAlerta(resultado);
                    if (alerta.isPresent()) {
                        System.out.println("    >>> Alerta de nível " + alerta.get().getNivel().getRotulo() + " emitido e gravado no banco.");
                        alertasEmitidos++;
                    }
                } catch (DadosInvalidosException e) {
                    System.out.println("  " + tipo.getDescricao() + ": sistema em silêncio - " + e.getMessage()
                            + ". Use a opção 1 para coletar mais leituras.");
                }
            }
        }
        System.out.println();
        System.out.println("Previsões concluídas. Alertas emitidos agora: " + alertasEmitidos);
    }

    // ------------------------------------------------------------------ 3 e 4. histórico

    private void verHistorico() {
        Sensor sensor = escolherSensor();
        List<Medicao> historico = servico.buscarHistoricoPorSensor(sensor.getId());
        if (historico.isEmpty()) {
            System.out.println("Nenhuma medição gravada para " + sensor.getId() + " ainda.");
            return;
        }
        System.out.println("Histórico de " + sensor.getId() + " (" + historico.size() + " medições):");
        historico.forEach(m -> System.out.println("  " + m));
    }

    private void verAlertas() {
        List<Alerta> alertas = servico.listarAlertas();
        if (alertas.isEmpty()) {
            System.out.println("Nenhum alerta emitido até agora.");
            return;
        }
        System.out.println("Alertas emitidos (" + alertas.size() + "), do mais recente ao mais antigo:");
        alertas.forEach(a -> System.out.println("  " + a));
    }

    // ------------------------------------------------------------------ 5. território

    private void gerenciarTerritorio() {
        boolean voltar = false;
        while (!voltar) {
            System.out.println();
            System.out.println("--- Gerenciar dados do território ---");
            System.out.println("1. Ver dados e limiares");
            System.out.println("2. Alterar população estimada");
            System.out.println("3. Alterar domicílios em área de risco");
            System.out.println("4. Alterar limiares de um tipo de sensor (exige código de acesso)");
            System.out.println("5. Alterar janela de previsão");
            System.out.println("6. Colocar/retirar sensor de manutenção");
            System.out.println("0. Voltar");
            int opcao = lerInteiro("Escolha: ");
            try {
                switch (opcao) {
                    case 1 -> mostrarDadosTerritorio();
                    case 2 -> {
                        territorio.setPopulacaoEstimada(lerInteiro("Nova população estimada: "));
                        System.out.println("População atualizada.");
                    }
                    case 3 -> {
                        territorio.setDomiciliosEmAreaDeRisco(lerInteiro("Novo número de domicílios em área de risco: "));
                        System.out.println("Domicílios atualizados.");
                    }
                    case 4 -> alterarLimiar();
                    case 5 -> {
                        configuracao.setJanelaPrevisao(lerInteiro("Nova janela (quantas leituras a previsão usa): "));
                        System.out.println("Janela atualizada para " + configuracao.getJanelaPrevisao() + " leituras.");
                    }
                    case 6 -> alternarManutencao();
                    case 0 -> voltar = true;
                    default -> System.out.println("Opção inválida.");
                }
            } catch (DadosInvalidosException e) {
                System.out.println("[DADO RECUSADO] " + e.getMessage());
            }
        }
    }

    private void mostrarDadosTerritorio() {
        System.out.println("Região: " + territorio.getRegiao());
        System.out.println("População estimada: " + territorio.getPopulacaoEstimada());
        System.out.println("Domicílios em área de risco: " + territorio.getDomiciliosEmAreaDeRisco());
        System.out.println("Janela de previsão: " + configuracao.getJanelaPrevisao() + " leituras");
        System.out.println("Limiares:");
        for (TipoSensor tipo : TipoSensor.values()) {
            System.out.printf("  %-17s (%s): %s%n", tipo.getDescricao(), tipo.getUnidade(), territorio.consultarLimiar(tipo));
        }
    }

    private void alterarLimiar() throws DadosInvalidosException {
        TipoSensor[] tipos = TipoSensor.values();
        for (int i = 0; i < tipos.length; i++) {
            System.out.printf("  %d. %s (%s) - atual: %s%n", i + 1, tipos[i].getDescricao(),
                    tipos[i].getUnidade(), configuracao.getLimiar(tipos[i]));
        }
        TipoSensor tipo = tipos[lerInteiroNaFaixa("Tipo de sensor: ", 1, tipos.length) - 1];
        String codigo = lerLinha("Código de acesso da coordenação: ");
        double atencao = lerDouble("Limite de ATENÇÃO: ");
        double alerta = lerDouble("Limite de ALERTA: ");
        double critico = lerDouble("Limite CRÍTICO: ");
        territorio.alterarLimiar(codigo, tipo, atencao, alerta, critico);
        System.out.println("Limiares de " + tipo.getDescricao() + " atualizados: " + configuracao.getLimiar(tipo));
    }

    private void alternarManutencao() {
        for (int i = 0; i < sensores.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, sensores.get(i));
        }
        SensorSimulado sensor = sensores.get(lerInteiroNaFaixa("Sensor: ", 1, sensores.size()) - 1);
        sensor.setFuncionando(!sensor.estaFuncionando());
        System.out.println("Agora: " + sensor);
    }

    // ------------------------------------------------------------------ 6. status

    private void statusCompleto() {
        System.out.println("=========== STATUS COMPLETO ===========");
        System.out.println("Gerado em " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        mostrarDadosTerritorio();
        System.out.println();
        for (PontoMonitoramento ponto : pontos) {
            StatusComponente status = ponto.verificarStatus();
            System.out.printf("[%s] %s - %s: %s (%s)%n", ponto.getId(), ponto.getNome(), ponto.getRegiao(),
                    status, status.getDescricao());
            for (Sensor sensor : ponto.getSensores()) {
                System.out.printf("    %-5s %-17s %-14s %d leituras no banco%n", sensor.getId(),
                        sensor.getTipo().getDescricao(), sensor.estaFuncionando() ? "funcionando" : "em manutenção",
                        servico.contarLeituras(sensor.getId()));
            }
        }
        List<Alerta> alertas = servico.listarAlertas();
        System.out.println();
        System.out.println("Total de alertas emitidos: " + alertas.size());
        alertas.stream().limit(3).forEach(a -> System.out.println("  " + a));
        System.out.println("=======================================");
    }

    // ------------------------------------------------------------------ 7. CSV

    private void exportarCsv() {
        Sensor sensor = escolherSensor();
        List<Medicao> historico = servico.buscarHistoricoPorSensor(sensor.getId());
        if (historico.isEmpty()) {
            System.out.println("Nada para exportar: " + sensor.getId() + " ainda não tem medições.");
            return;
        }
        Path destino = Path.of("exportacoes", "historico-" + sensor.getId() + ".csv");
        try {
            Path arquivo = new ExportadorCsv().exportar(historico, destino);
            System.out.println(historico.size() + " medições exportadas para " + arquivo);
        } catch (IOException e) {
            System.out.println("Não foi possível gravar o arquivo CSV em " + destino + ": " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ leitura do teclado

    private Sensor escolherSensor() {
        for (int i = 0; i < sensores.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, sensores.get(i));
        }
        return sensores.get(lerInteiroNaFaixa("Sensor: ", 1, sensores.size()) - 1);
    }

    private String lerLinha(String mensagem) {
        System.out.print(mensagem);
        if (!entrada.hasNextLine()) {
            System.out.println();
            System.out.println("Entrada encerrada. Saindo do sistema.");
            System.exit(0);
        }
        return entrada.nextLine().trim();
    }

    private int lerInteiro(String mensagem) {
        while (true) {
            String texto = lerLinha(mensagem);
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("'" + texto + "' não é um número inteiro. Tente novamente.");
            }
        }
    }

    private int lerInteiroNaFaixa(String mensagem, int minimo, int maximo) {
        while (true) {
            int valor = lerInteiro(mensagem);
            if (valor >= minimo && valor <= maximo) {
                return valor;
            }
            System.out.printf("Digite um valor entre %d e %d.%n", minimo, maximo);
        }
    }

    private double lerDouble(String mensagem) {
        while (true) {
            String texto = lerLinha(mensagem).replace(',', '.');
            try {
                return Double.parseDouble(texto);
            } catch (NumberFormatException e) {
                System.out.println("'" + texto + "' não é um número. Use, por exemplo, 35 ou 35,5.");
            }
        }
    }
}
