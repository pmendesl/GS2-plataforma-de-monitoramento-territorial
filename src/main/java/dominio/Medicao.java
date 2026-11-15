package dominio;

import excecao.DadosInvalidosException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Uma leitura de sensor. Só existe se for válida: o construtor
 * recusa valores fora da faixa física do tipo de sensor.
 */
public class Medicao {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private Long id;
    private final String sensorId;
    private final TipoSensor tipo;
    private final double valor;
    private final LocalDateTime dataHora;

    /** Nova medição, ainda não gravada, com a hora atual. */
    public Medicao(String sensorId, TipoSensor tipo, double valor) throws DadosInvalidosException {
        this(null, sensorId, tipo, valor, LocalDateTime.now());
    }

    /** Medição completa (usada ao carregar do banco). */
    public Medicao(Long id, String sensorId, TipoSensor tipo, double valor, LocalDateTime dataHora)
            throws DadosInvalidosException {
        if (sensorId == null || sensorId.isBlank()) {
            throw new DadosInvalidosException("medição sem identificação do sensor");
        }
        if (tipo == null) {
            throw new DadosInvalidosException("medição do sensor " + sensorId + " sem tipo de sensor");
        }
        if (!tipo.aceita(valor)) {
            throw new DadosInvalidosException(String.format(
                    "medição de %s fora da faixa no sensor %s: %.2f %s (faixa válida: %.1f a %.1f)",
                    tipo.getDescricao(), sensorId, valor, tipo.getUnidade(),
                    tipo.getValorMinimo(), tipo.getValorMaximo()));
        }
        if (dataHora == null) {
            throw new DadosInvalidosException("medição do sensor " + sensorId + " sem data/hora");
        }
        this.id = id;
        this.sensorId = sensorId;
        this.tipo = tipo;
        this.valor = valor;
        this.dataHora = dataHora;
    }

    /** Recebe o id gerado pelo banco. Só pode ser definido uma vez. */
    public void definirId(Long idGerado) {
        if (this.id != null) {
            throw new IllegalStateException("a medição " + this.id + " já possui id");
        }
        this.id = idGerado;
    }

    public Long getId() {
        return id;
    }

    public String getSensorId() {
        return sensorId;
    }

    public TipoSensor getTipo() {
        return tipo;
    }

    public double getValor() {
        return valor;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    @Override
    public String toString() {
        return String.format("%s | %-5s | %-17s | %7.2f %s",
                dataHora.format(FORMATO), sensorId, tipo.getDescricao(), valor, tipo.getUnidade());
    }
}
