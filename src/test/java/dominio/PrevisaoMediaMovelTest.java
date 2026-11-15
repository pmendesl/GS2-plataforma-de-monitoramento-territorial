package dominio;

import excecao.DadosInvalidosException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrevisaoMediaMovelTest {

    static List<Medicao> chuvas(double... valores) throws DadosInvalidosException {
        List<Medicao> lista = new ArrayList<>();
        for (double v : valores) {
            lista.add(new Medicao("P-01", TipoSensor.PLUVIOMETRO, v));
        }
        return lista;
    }

    @Test
    void deveCalcularAMediaDasUltimasLeituras() throws DadosInvalidosException {
        // Arrange
        PrevisaoMediaMovel estrategia = new PrevisaoMediaMovel(3);
        List<Medicao> historico = chuvas(10, 20, 30);

        // Act
        ResultadoPrevisao resultado = estrategia.prever(historico);

        // Assert
        assertEquals(20.0, resultado.getValorPrevisto(), 0.0001);
        assertEquals("P-01", resultado.getSensorId());
        assertEquals(3, resultado.getLeiturasUsadas());
    }

    @Test
    void deveUsarSomenteAJanelaMaisRecente() throws DadosInvalidosException {
        // Arrange: as duas leituras antigas (100, 100) não podem entrar na conta
        PrevisaoMediaMovel estrategia = new PrevisaoMediaMovel(3);
        List<Medicao> historico = chuvas(100, 100, 6, 9, 12);

        // Act
        ResultadoPrevisao resultado = estrategia.prever(historico);

        // Assert
        assertEquals(9.0, resultado.getValorPrevisto(), 0.0001);
    }

    @Test
    void deveSeCalarQuandoHistoricoForMenorQueAJanela() throws DadosInvalidosException {
        // Arrange
        PrevisaoMediaMovel estrategia = new PrevisaoMediaMovel(5);
        List<Medicao> historico = chuvas(10, 20, 30);

        // Act
        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class,
                () -> estrategia.prever(historico));

        // Assert
        assertTrue(erro.getMessage().contains("histórico insuficiente"));
    }
}
