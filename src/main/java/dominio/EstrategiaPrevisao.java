package dominio;

import excecao.DadosInvalidosException;

import java.util.List;

/**
 * Estratégia de previsão aplicada sobre o histórico de UM sensor
 * (lista em ordem cronológica, da leitura mais antiga para a mais recente).
 */
public abstract class EstrategiaPrevisao {

    private final String nome;
    private final int janela;

    protected EstrategiaPrevisao(String nome, int janela) {
        if (janela < 1) {
            throw new IllegalArgumentException("a janela de previsão precisa ser de pelo menos 1 leitura");
        }
        this.nome = nome;
        this.janela = janela;
    }

    /** Calcula a previsão para a próxima leitura a partir do histórico. */
    public abstract ResultadoPrevisao prever(List<Medicao> historico) throws DadosInvalidosException;

    /** Quantas leituras do banco a estratégia precisa para não "chutar". */
    public abstract int leiturasNecessarias();

    /** Descrição curta exibida no menu; as filhas complementam com super.descrever(). */
    public String descrever() {
        return nome + " (janela de " + janela + " leituras)";
    }

    /**
     * Regra comum a todas as estratégias: o sistema se cala se o histórico for curto demais.
     */
    protected void validarHistorico(List<Medicao> historico) throws DadosInvalidosException {
        int quantidade = historico == null ? 0 : historico.size();
        if (quantidade < leiturasNecessarias()) {
            throw new DadosInvalidosException(String.format(
                    "histórico insuficiente para '%s': %d leitura(s) no banco, mínimo de %d para uma previsão confiável",
                    nome, quantidade, leiturasNecessarias()));
        }
    }

    /** Média dos valores de um trecho do histórico [inicio, fim). */
    protected static double media(List<Medicao> medicoes, int inicio, int fim) {
        double soma = 0;
        for (int i = inicio; i < fim; i++) {
            soma += medicoes.get(i).getValor();
        }
        return soma / (fim - inicio);
    }

    public String getNome() {
        return nome;
    }

    public int getJanela() {
        return janela;
    }
}
