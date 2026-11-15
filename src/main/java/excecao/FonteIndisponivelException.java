package excecao;

/**
 * Lançada quando a fonte de dados (sensor ou gerador de dados) falha
 * e não entrega uma leitura.
 */
public class FonteIndisponivelException extends Exception {

    public FonteIndisponivelException(String mensagem) {
        super(mensagem);
    }
}
