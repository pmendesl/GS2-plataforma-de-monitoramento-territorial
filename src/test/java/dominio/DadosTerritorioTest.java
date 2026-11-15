package dominio;

import excecao.DadosInvalidosException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import servico.ConfiguracaoSistema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DadosTerritorioTest {

    private ConfiguracaoSistema configuracao;
    private DadosTerritorio territorio;

    @BeforeEach
    void preparar() throws DadosInvalidosException {
        configuracao = ConfiguracaoSistema.getInstancia();
        configuracao.restaurarPadroes();
        territorio = new DadosTerritorio("Região Teste", 1000, 50, "SEGREDO", configuracao);
    }

    @Test
    void deveAlterarLimiarComCodigoCorreto() throws DadosInvalidosException {
        // Arrange
        String codigo = "SEGREDO";

        // Act
        territorio.alterarLimiar(codigo, TipoSensor.PLUVIOMETRO, 10, 20, 30);

        // Assert: a mudança vale para o Singleton, ou seja, para todo o sistema
        assertEquals(30.0, configuracao.getLimiar(TipoSensor.PLUVIOMETRO).getCritico(), 0.0001);
    }

    @Test
    void deveRecusarAlteracaoComCodigoErradoEManterLimiarAntigo() {
        // Arrange
        double criticoAntes = configuracao.getLimiar(TipoSensor.PLUVIOMETRO).getCritico();

        // Act
        assertThrows(DadosInvalidosException.class,
                () -> territorio.alterarLimiar("chute", TipoSensor.PLUVIOMETRO, 1, 2, 3));

        // Assert
        assertEquals(criticoAntes, configuracao.getLimiar(TipoSensor.PLUVIOMETRO).getCritico(), 0.0001);
    }

    @Test
    void deveRecusarLimiarForaDeOrdem() {
        // Arrange: ALERTA menor que ATENÇÃO
        double atencao = 30;
        double alerta = 10;
        double critico = 50;

        // Act + Assert
        assertThrows(DadosInvalidosException.class,
                () -> territorio.alterarLimiar("SEGREDO", TipoSensor.PLUVIOMETRO, atencao, alerta, critico));
    }

    @Test
    void deveRecusarPopulacaoNegativa() {
        // Arrange
        int populacaoNegativa = -1;

        // Act
        assertThrows(DadosInvalidosException.class, () -> territorio.setPopulacaoEstimada(populacaoNegativa));

        // Assert: valor anterior preservado
        assertEquals(1000, territorio.getPopulacaoEstimada());
    }
}
