package dominio;

import java.util.Random;

/** Mede a intensidade da chuva em mm/h. */
public class SensorPluviometro extends SensorSimulado {

    public SensorPluviometro(String id, Random random) {
        super(id, 15.0, random);
    }

    @Override
    protected double gerarVariacao(Random random) {
        // +1,5 simula uma frente de chuva que vai se intensificando ao longo das leituras
        return 1.5 + random.nextGaussian() * 8.0;
    }

    @Override
    public TipoSensor getTipo() {
        return TipoSensor.PLUVIOMETRO;
    }
}
