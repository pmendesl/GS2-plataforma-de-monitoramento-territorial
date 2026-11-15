package servico;

import dominio.EstrategiaPrevisao;
import dominio.PrevisaoComparacao;
import dominio.PrevisaoMediaMovel;
import dominio.TipoEstrategia;
import excecao.DadosInvalidosException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

class FabricaPrevisaoTest {

    private ConfiguracaoSistema configuracao;

    @BeforeEach
    void preparar() {
        configuracao = ConfiguracaoSistema.getInstancia();
        configuracao.restaurarPadroes();
    }

    @Test
    void deveCriarCadaEstrategiaComAJanelaDaConfiguracao() throws DadosInvalidosException {
        // Arrange
        configuracao.setJanelaPrevisao(7);
        FabricaPrevisao fabrica = new FabricaPrevisao(configuracao);

        // Act
        EstrategiaPrevisao media = fabrica.criar(TipoEstrategia.MEDIA_MOVEL);
        EstrategiaPrevisao comparacao = fabrica.criar(TipoEstrategia.COMPARACAO);

        // Assert
        assertInstanceOf(PrevisaoMediaMovel.class, media);
        assertInstanceOf(PrevisaoComparacao.class, comparacao);
        assertEquals(7, media.getJanela());
        assertEquals(14, comparacao.leiturasNecessarias());
    }

    @Test
    void configuracaoDeveSerUmaInstanciaUnica() {
        // Arrange + Act
        ConfiguracaoSistema primeira = ConfiguracaoSistema.getInstancia();
        ConfiguracaoSistema segunda = ConfiguracaoSistema.getInstancia();

        // Assert
        assertSame(primeira, segunda);
    }
}
