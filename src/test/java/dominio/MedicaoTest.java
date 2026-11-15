package dominio;

import excecao.DadosInvalidosException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MedicaoTest {

    @Test
    void deveCriarMedicaoValidaDentroDaFaixa() throws DadosInvalidosException {
        // Arrange
        double chuva = 42.5;

        // Act
        Medicao medicao = new Medicao("P-01", TipoSensor.PLUVIOMETRO, chuva);

        // Assert
        assertEquals("P-01", medicao.getSensorId());
        assertEquals(TipoSensor.PLUVIOMETRO, medicao.getTipo());
        assertEquals(42.5, medicao.getValor(), 0.0001);
        assertNull(medicao.getId(), "medição nova ainda não tem id do banco");
    }

    @Test
    void deveRecusarMedicaoDeChuvaNegativa() {
        // Arrange
        double chuvaNegativa = -3.0;

        // Act
        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class,
                () -> new Medicao("P-01", TipoSensor.PLUVIOMETRO, chuvaNegativa));

        // Assert
        assertTrue(erro.getMessage().contains("fora da faixa"));
        assertTrue(erro.getMessage().contains("P-01"));
    }

    @Test
    void deveRecusarUmidadeAcimaDeCemPorCento() {
        // Arrange
        double umidadeImpossivel = 130.0;

        // Act + Assert
        assertThrows(DadosInvalidosException.class,
                () -> new Medicao("U-01", TipoSensor.UMIDADE_SOLO, umidadeImpossivel));
    }

    @Test
    void deveRecusarMedicaoSemSensor() {
        // Arrange
        String sensorVazio = " ";

        // Act + Assert
        assertThrows(DadosInvalidosException.class,
                () -> new Medicao(sensorVazio, TipoSensor.NIVEL_CORREGO, 1.0));
    }
}
