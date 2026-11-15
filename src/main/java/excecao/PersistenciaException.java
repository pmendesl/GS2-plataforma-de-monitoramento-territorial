package excecao;

/**
 * Envolve erros de banco (SQLException) com uma mensagem que explica
 * qual operação falhou. É não verificada para não poluir as assinaturas
 * do domínio com detalhes de infraestrutura.
 */
public class PersistenciaException extends RuntimeException {

    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
