package dominio;

import java.util.Random;

/** Mede a altura da água no córrego, em metros. */
public class SensorNivelCorrego extends SensorSimulado {

    public SensorNivelCorrego(String id, Random random) {
        super(id, 2.0, random);
    }

    @Override
    protected double gerarVariacao(Random random) {
        // o córrego tende a subir enquanto a chuva continua
        return 0.08 + random.nextGaussian() * 0.4;
    }

    @Override
    public TipoSensor getTipo() {
        return TipoSensor.NIVEL_CORREGO;
    }
}
