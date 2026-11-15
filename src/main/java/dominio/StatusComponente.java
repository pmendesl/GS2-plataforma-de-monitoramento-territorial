package dominio;

public enum StatusComponente {

    DESCONHECIDO("ainda não verificado"),
    OPERANTE("todos os sensores funcionando"),
    PARCIAL("parte dos sensores fora do ar"),
    INOPERANTE("nenhum sensor funcionando");

    private final String descricao;

    StatusComponente(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
