package dominio;

/**
 * O que uma estratégia devolve: o valor previsto e como ela chegou nele.
 */
public class ResultadoPrevisao {

    private final String sensorId;
    private final TipoSensor tipoSensor;
    private final String estrategia;
    private final double valorPrevisto;
    private final int leiturasUsadas;
    private final String explicacao;

    public ResultadoPrevisao(String sensorId, TipoSensor tipoSensor, String estrategia,
                             double valorPrevisto, int leiturasUsadas, String explicacao) {
        this.sensorId = sensorId;
        this.tipoSensor = tipoSensor;
        this.estrategia = estrategia;
        this.valorPrevisto = valorPrevisto;
        this.leiturasUsadas = leiturasUsadas;
        this.explicacao = explicacao;
    }

    public String getSensorId() {
        return sensorId;
    }

    public TipoSensor getTipoSensor() {
        return tipoSensor;
    }

    public String getEstrategia() {
        return estrategia;
    }

    public double getValorPrevisto() {
        return valorPrevisto;
    }

    public int getLeiturasUsadas() {
        return leiturasUsadas;
    }

    public String getExplicacao() {
        return explicacao;
    }

    @Override
    public String toString() {
        return String.format("%s | %s: previsto %.2f %s (%s)",
                sensorId, estrategia, valorPrevisto, tipoSensor.getUnidade(), explicacao);
    }
}
