package dominio;

import excecao.DadosInvalidosException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static dominio.PrevisaoMediaMovelTest.chuvas;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrevisaoComparacaoTest {

    @Test
    void deveProjetarATendenciaDeAlta() throws DadosInvalidosException {
        // Arrange: período anterior média 10, recente média 20 -> variação +10 -> previsto 30
        PrevisaoComparacao estrategia = new PrevisaoComparacao(2);
        List<Medicao> historico = chuvas(8, 12, 18, 22);

        // Act
        ResultadoPrevisao resultado = estrategia.prever(historico);

        // Assert
        assertEquals(30.0, resultado.getValorPrevisto(), 0.0001);
        assertTrue(resultado.getExplicacao().contains("alta"));
    }

    @Test
    void naoDevePreverValorAbaixoDoMinimoFisico() throws DadosInvalidosException {
        // Arrange: anterior 20, recente 2 -> projeção -16, mas chuva não pode ser negativa
        PrevisaoComparacao estrategia = new PrevisaoComparacao(2);
        List<Medicao> historico = chuvas(20, 20, 2, 2);

        // Act
        ResultadoPrevisao resultado = estrategia.prever(historico);

        // Assert
        assertEquals(0.0, resultado.getValorPrevisto(), 0.0001);
    }

    @Test
    void devePrecisarDoDobroDaJanela() throws DadosInvalidosException {
        // Arrange
        PrevisaoComparacao estrategia = new PrevisaoComparacao(3);
        List<Medicao> historico = chuvas(1, 2, 3, 4, 5);

        // Act + Assert
        assertEquals(6, estrategia.leiturasNecessarias());
        assertThrows(DadosInvalidosException.class, () -> estrategia.prever(historico));
    }
}
