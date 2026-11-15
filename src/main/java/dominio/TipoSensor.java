package dominio;

/**
 * Tipos de sensor usados no recorte "chuva intensa e risco de alagamento".
 * Cada tipo conhece sua unidade e a faixa física aceitável de leitura.
 */
public enum TipoSensor {

    PLUVIOMETRO("Pluviômetro", "mm/h", 0.0, 200.0),
    NIVEL_CORREGO("Nível do córrego", "m", 0.0, 10.0),
    UMIDADE_SOLO("Umidade do solo", "%", 0.0, 100.0);

    private final String descricao;
    private final String unidade;
    private final double valorMinimo;
    private final double valorMaximo;

    TipoSensor(String descricao, String unidade, double valorMinimo, double valorMaximo) {
        this.descricao = descricao;
        this.unidade = unidade;
        this.valorMinimo = valorMinimo;
        this.valorMaximo = valorMaximo;
    }

    public boolean aceita(double valor) {
        return Double.isFinite(valor) && valor >= valorMinimo && valor <= valorMaximo;
    }

    /** Mantém um valor calculado dentro da faixa física do sensor. */
    public double limitarNaFaixa(double valor) {
        return Math.max(valorMinimo, Math.min(valorMaximo, valor));
    }

    public String getDescricao() {
        return descricao;
    }

    public String getUnidade() {
        return unidade;
    }

    public double getValorMinimo() {
        return valorMinimo;
    }

    public double getValorMaximo() {
        return valorMaximo;
    }
}
