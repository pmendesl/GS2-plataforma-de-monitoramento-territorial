package dominio;

/** Estratégias de previsão disponíveis (a FabricaPrevisao sabe criar cada uma). */
public enum TipoEstrategia {

    MEDIA_MOVEL("Média Móvel"),
    COMPARACAO("Comparação de Períodos");

    private final String descricao;

    TipoEstrategia(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
