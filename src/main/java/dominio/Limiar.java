package dominio;

import excecao.DadosInvalidosException;

import java.util.Optional;

/**
 * Os três limites de um tipo de sensor. Garante que
 * 0 &lt;= ATENÇÃO &lt; ALERTA &lt; CRÍTICO.
 */
public class Limiar {

    private final double atencao;
    private final double alerta;
    private final double critico;

    public Limiar(double atencao, double alerta, double critico) throws DadosInvalidosException {
        if (!Double.isFinite(atencao) || !Double.isFinite(alerta) || !Double.isFinite(critico)) {
            throw new DadosInvalidosException("limiar inválido: todos os valores precisam ser números");
        }
        if (atencao < 0) {
            throw new DadosInvalidosException("limiar inválido: o limite de ATENÇÃO não pode ser negativo");
        }
        if (!(atencao < alerta && alerta < critico)) {
            throw new DadosInvalidosException(String.format(
                    "limiar inválido: é preciso ATENÇÃO < ALERTA < CRÍTICO (recebido %.2f / %.2f / %.2f)",
                    atencao, alerta, critico));
        }
        this.atencao = atencao;
        this.alerta = alerta;
        this.critico = critico;
    }

    /** Devolve o nível mais grave atingido pelo valor, ou vazio se estiver abaixo de ATENÇÃO. */
    public Optional<NivelAlerta> classificar(double valor) {
        if (valor >= critico) {
            return Optional.of(NivelAlerta.CRITICO);
        }
        if (valor >= alerta) {
            return Optional.of(NivelAlerta.ALERTA);
        }
        if (valor >= atencao) {
            return Optional.of(NivelAlerta.ATENCAO);
        }
        return Optional.empty();
    }

    public double limiteDe(NivelAlerta nivel) {
        return switch (nivel) {
            case ATENCAO -> atencao;
            case ALERTA -> alerta;
            case CRITICO -> critico;
        };
    }

    public double getAtencao() {
        return atencao;
    }

    public double getAlerta() {
        return alerta;
    }

    public double getCritico() {
        return critico;
    }

    @Override
    public String toString() {
        return String.format("ATENÇÃO >= %.1f | ALERTA >= %.1f | CRÍTICO >= %.1f", atencao, alerta, critico);
    }
}
