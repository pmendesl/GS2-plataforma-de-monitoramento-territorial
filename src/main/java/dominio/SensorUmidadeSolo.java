package dominio;

import java.util.Random;

/** Mede a saturação do solo em % (solo encharcado = risco de deslizamento na encosta). */
public class SensorUmidadeSolo extends SensorSimulado {

    public SensorUmidadeSolo(String id, Random random) {
        super(id, 60.0, random);
    }

    @Override
    protected double gerarVariacao(Random random) {
        // o solo vai encharcando com a chuva acumulada
        return 1.2 + random.nextGaussian() * 5.0;
    }

    @Override
    public TipoSensor getTipo() {
        return TipoSensor.UMIDADE_SOLO;
    }
}
