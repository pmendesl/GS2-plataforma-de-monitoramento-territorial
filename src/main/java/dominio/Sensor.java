package dominio;

import excecao.FonteIndisponivelException;

/**
 * Contrato de qualquer sensor do território.
 */
public interface Sensor {

    String getId();

    /** Lê o valor atual. Lança exceção se o sensor não responder. */
    double lerValor() throws FonteIndisponivelException;

    boolean estaFuncionando();

    TipoSensor getTipo();
}
