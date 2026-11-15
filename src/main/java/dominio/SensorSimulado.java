package dominio;

import excecao.FonteIndisponivelException;

import java.util.Objects;
import java.util.Random;

/**
 * Base dos sensores simulados. O valor faz um "passeio aleatório" a partir da
 * última leitura (chuva não pula de 0 para 150 de uma vez), e de vez em quando
 * o sensor falha ou devolve uma leitura corrompida — como um equipamento real.
 */
public abstract class SensorSimulado implements Sensor {

    private static final double CHANCE_FALHA = 0.04;
    private static final double CHANCE_LEITURA_CORROMPIDA = 0.04;

    private final String id;
    private final Random random;
    private double ultimoValor;
    private boolean funcionando = true;

    protected SensorSimulado(String id, double valorInicial, Random random) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("sensor sem identificador");
        }
        this.id = id;
        this.random = Objects.requireNonNull(random, "random");
        this.ultimoValor = valorInicial;
    }

    /** Quanto o valor pode variar entre uma leitura e outra. */
    protected abstract double gerarVariacao(Random random);

    @Override
    public double lerValor() throws FonteIndisponivelException {
        if (!funcionando) {
            throw new FonteIndisponivelException("sensor " + id + " está em manutenção e não envia leituras");
        }
        double sorteio = random.nextDouble();
        if (sorteio < CHANCE_FALHA) {
            throw new FonteIndisponivelException("sensor " + id + " não respondeu (falha de comunicação)");
        }
        if (sorteio < CHANCE_FALHA + CHANCE_LEITURA_CORROMPIDA) {
            // ruído elétrico: devolve um valor fisicamente impossível, que a validação deve barrar
            return -1 - random.nextInt(10);
        }
        ultimoValor = getTipo().limitarNaFaixa(ultimoValor + gerarVariacao(random));
        return Math.round(ultimoValor * 100.0) / 100.0;
    }

    @Override
    public boolean estaFuncionando() {
        return funcionando;
    }

    public void setFuncionando(boolean funcionando) {
        this.funcionando = funcionando;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return String.format("%s (%s, %s)", id, getTipo().getDescricao(),
                funcionando ? "funcionando" : "em manutenção");
    }
}
