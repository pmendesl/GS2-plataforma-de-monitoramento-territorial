package excecao;

/**
 * Lançada quando um dado do território não pode ser aceito:
 * medição fora da faixa física, população negativa, limiar inválido,
 * histórico insuficiente para previsão, código de acesso incorreto etc.
 */
public class DadosInvalidosException extends Exception {

    public DadosInvalidosException(String mensagem) {
        super(mensagem);
    }
}
