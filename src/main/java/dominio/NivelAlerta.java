package dominio;

/**
 * Níveis de alerta, do mais leve ao mais grave, cada um com a ação
 * recomendada para a organização comunitária.
 */
public enum NivelAlerta {

    ATENCAO("ATENÇÃO", "Monitorar o ponto e desobstruir bueiros e canaletas da rua."),
    ALERTA("ALERTA", "Avisar a vizinhança da área de risco e preparar o abrigo comunitário."),
    CRITICO("CRÍTICO", "Acionar a Defesa Civil e iniciar a retirada preventiva da área de risco.");

    private final String rotulo;
    private final String recomendacao;

    NivelAlerta(String rotulo, String recomendacao) {
        this.rotulo = rotulo;
        this.recomendacao = recomendacao;
    }

    public String getRotulo() {
        return rotulo;
    }

    public String getRecomendacao() {
        return recomendacao;
    }
}
