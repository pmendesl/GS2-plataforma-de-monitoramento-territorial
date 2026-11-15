package dominio;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Alerta emitido quando uma previsão estoura um limiar.
 * Guarda tudo o que explica o porquê: sensor, valor previsto, limite e estratégia.
 */
public class Alerta {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private Long id;
    private final String sensorId;
    private final TipoSensor tipoSensor;
    private final NivelAlerta nivel;
    private final double valorPrevisto;
    private final double limite;
    private final String estrategia;
    private final String mensagem;
    private final LocalDateTime dataHora;

    public Alerta(String sensorId, TipoSensor tipoSensor, NivelAlerta nivel, double valorPrevisto,
                  double limite, String estrategia, String mensagem) {
        this(null, sensorId, tipoSensor, nivel, valorPrevisto, limite, estrategia, mensagem, LocalDateTime.now());
    }

    public Alerta(Long id, String sensorId, TipoSensor tipoSensor, NivelAlerta nivel, double valorPrevisto,
                  double limite, String estrategia, String mensagem, LocalDateTime dataHora) {
        this.id = id;
        this.sensorId = Objects.requireNonNull(sensorId, "sensorId");
        this.tipoSensor = Objects.requireNonNull(tipoSensor, "tipoSensor");
        this.nivel = Objects.requireNonNull(nivel, "nivel");
        this.valorPrevisto = valorPrevisto;
        this.limite = limite;
        this.estrategia = Objects.requireNonNull(estrategia, "estrategia");
        this.mensagem = Objects.requireNonNull(mensagem, "mensagem");
        this.dataHora = Objects.requireNonNull(dataHora, "dataHora");
    }

    public void definirId(Long idGerado) {
        if (this.id != null) {
            throw new IllegalStateException("o alerta " + this.id + " já possui id");
        }
        this.id = idGerado;
    }

    public Long getId() {
        return id;
    }

    public String getSensorId() {
        return sensorId;
    }

    public TipoSensor getTipoSensor() {
        return tipoSensor;
    }

    public NivelAlerta getNivel() {
        return nivel;
    }

    public double getValorPrevisto() {
        return valorPrevisto;
    }

    public double getLimite() {
        return limite;
    }

    public String getEstrategia() {
        return estrategia;
    }

    public String getMensagem() {
        return mensagem;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    @Override
    public String toString() {
        return String.format("%s [%s] %s", dataHora.format(FORMATO), nivel.getRotulo(), mensagem);
    }
}
