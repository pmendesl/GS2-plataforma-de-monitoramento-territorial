package servico;

import dominio.Alerta;
import dominio.EstrategiaPrevisao;
import dominio.Limiar;
import dominio.Medicao;
import dominio.NivelAlerta;
import dominio.ResultadoPrevisao;
import dominio.Sensor;
import dominio.TipoEstrategia;
import dominio.TipoSensor;
import excecao.DadosInvalidosException;
import excecao.FonteIndisponivelException;
import persistencia.AlertaDAO;
import persistencia.MedicaoDAO;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Regras de negócio: ler e gravar medições, prever a partir do histórico
 * do banco e emitir alertas comparando a previsão com os limiares.
 * Não conhece Scanner nem System.out, por isso pode ser testado com mocks.
 */
public class ServicoMonitoramento {

    private final MedicaoDAO medicaoDAO;
    private final AlertaDAO alertaDAO;
    private final ConfiguracaoSistema configuracao;
    private final FabricaPrevisao fabricaPrevisao;

    public ServicoMonitoramento(MedicaoDAO medicaoDAO, AlertaDAO alertaDAO,
                                ConfiguracaoSistema configuracao, FabricaPrevisao fabricaPrevisao) {
        this.medicaoDAO = Objects.requireNonNull(medicaoDAO, "medicaoDAO");
        this.alertaDAO = Objects.requireNonNull(alertaDAO, "alertaDAO");
        this.configuracao = Objects.requireNonNull(configuracao, "configuracao");
        this.fabricaPrevisao = Objects.requireNonNull(fabricaPrevisao, "fabricaPrevisao");
    }

    /**
     * Lê o sensor, valida e grava. Leitura inválida NÃO é gravada.
     */
    public Medicao registrarLeitura(Sensor sensor) throws FonteIndisponivelException, DadosInvalidosException {
        double valor = sensor.lerValor();
        Medicao medicao = new Medicao(sensor.getId(), sensor.getTipo(), valor);
        medicaoDAO.salvar(medicao);
        return medicao;
    }

    /**
     * Roda a estratégia escolhida sobre o histórico gravado do sensor.
     * Se o histórico for curto demais, lança DadosInvalidosException (o sistema "se cala").
     */
    public ResultadoPrevisao preverParaSensor(String sensorId, TipoEstrategia tipoEstrategia)
            throws DadosInvalidosException {
        EstrategiaPrevisao estrategia = fabricaPrevisao.criar(tipoEstrategia);
        List<Medicao> historico = medicaoDAO.buscarUltimasPorSensor(sensorId, estrategia.leiturasNecessarias());
        return estrategia.prever(historico);
    }

    /**
     * Compara a previsão com os limiares do tipo de sensor. Se algum foi estourado,
     * cria o alerta com a explicação, grava no banco e devolve.
     */
    public Optional<Alerta> avaliarAlerta(ResultadoPrevisao resultado) {
        Limiar limiar = configuracao.getLimiar(resultado.getTipoSensor());
        return limiar.classificar(resultado.getValorPrevisto())
                .map(nivel -> criarAlerta(resultado, nivel, limiar.limiteDe(nivel)))
                .map(alerta -> {
                    alertaDAO.salvar(alerta);
                    return alerta;
                });
    }

    /** Explica em palavras o que o valor previsto significa para o território. */
    public String interpretar(ResultadoPrevisao resultado) {
        TipoSensor tipo = resultado.getTipoSensor();
        Limiar limiar = configuracao.getLimiar(tipo);
        return limiar.classificar(resultado.getValorPrevisto())
                .map(nivel -> String.format("atinge o nível %s (limite %.1f %s). Ação sugerida: %s",
                        nivel.getRotulo(), limiar.limiteDe(nivel), tipo.getUnidade(), nivel.getRecomendacao()))
                .orElse(String.format("abaixo do limite de ATENÇÃO (%.1f %s): situação dentro do esperado.",
                        limiar.getAtencao(), tipo.getUnidade()));
    }

    private Alerta criarAlerta(ResultadoPrevisao resultado, NivelAlerta nivel, double limite) {
        TipoSensor tipo = resultado.getTipoSensor();
        String mensagem = String.format(
                "Sensor %s (%s): valor previsto %.2f %s pela %s estourou o limite de %s (%.1f %s). %s",
                resultado.getSensorId(), tipo.getDescricao(), resultado.getValorPrevisto(), tipo.getUnidade(),
                resultado.getEstrategia(), nivel.getRotulo(), limite, tipo.getUnidade(), nivel.getRecomendacao());
        return new Alerta(resultado.getSensorId(), tipo, nivel, resultado.getValorPrevisto(),
                limite, resultado.getEstrategia(), mensagem);
    }

    public List<Medicao> buscarHistoricoPorSensor(String sensorId) {
        return medicaoDAO.buscarPorSensor(sensorId);
    }

    public List<Alerta> listarAlertas() {
        return alertaDAO.listarTodos();
    }

    public int contarLeituras(String sensorId) {
        return medicaoDAO.contarPorSensor(sensorId);
    }
}
