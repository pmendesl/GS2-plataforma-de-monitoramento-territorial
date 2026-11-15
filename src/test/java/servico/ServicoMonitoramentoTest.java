package servico;

import dominio.Alerta;
import dominio.Medicao;
import dominio.NivelAlerta;
import dominio.ResultadoPrevisao;
import dominio.Sensor;
import dominio.TipoEstrategia;
import dominio.TipoSensor;
import excecao.DadosInvalidosException;
import excecao.FonteIndisponivelException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import persistencia.AlertaDAO;
import persistencia.MedicaoDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testa a lógica de leitura, previsão e alerta SEM banco:
 * os DAOs e o sensor são mocks do Mockito.
 */
@ExtendWith(MockitoExtension.class)
class ServicoMonitoramentoTest {

    @Mock
    private MedicaoDAO medicaoDAO;
    @Mock
    private AlertaDAO alertaDAO;
    @Mock
    private Sensor sensor;

    private ServicoMonitoramento servico;

    @BeforeEach
    void preparar() {
        ConfiguracaoSistema configuracao = ConfiguracaoSistema.getInstancia();
        configuracao.restaurarPadroes(); // pluviômetro: 20 / 35 / 50 mm/h, janela 5
        servico = new ServicoMonitoramento(medicaoDAO, alertaDAO, configuracao, new FabricaPrevisao(configuracao));
    }

    private static List<Medicao> chuvas(double... valores) throws DadosInvalidosException {
        List<Medicao> lista = new ArrayList<>();
        for (double v : valores) {
            lista.add(new Medicao("P-01", TipoSensor.PLUVIOMETRO, v));
        }
        return lista;
    }

    @Test
    void deveEmitirEGravarAlertaCriticoQuandoPrevisaoPassaDoLimite() throws DadosInvalidosException {
        // Arrange: média das 5 últimas = 55 mm/h, acima do crítico (50)
        when(medicaoDAO.buscarUltimasPorSensor("P-01", 5)).thenReturn(chuvas(55, 60, 52, 58, 50));

        // Act
        ResultadoPrevisao resultado = servico.preverParaSensor("P-01", TipoEstrategia.MEDIA_MOVEL);
        Optional<Alerta> alerta = servico.avaliarAlerta(resultado);

        // Assert
        assertTrue(alerta.isPresent());
        ArgumentCaptor<Alerta> gravado = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaDAO).salvar(gravado.capture());
        assertEquals(NivelAlerta.CRITICO, gravado.getValue().getNivel());
        assertEquals(55.0, gravado.getValue().getValorPrevisto(), 0.0001);
        assertEquals(50.0, gravado.getValue().getLimite(), 0.0001);
        assertTrue(gravado.getValue().getMensagem().contains("P-01"));
    }

    @Test
    void deveClassificarComoAtencaoQuandoPassaSoDoPrimeiroLimite() throws DadosInvalidosException {
        // Arrange: média = 25 mm/h -> entre ATENÇÃO (20) e ALERTA (35)
        when(medicaoDAO.buscarUltimasPorSensor("P-01", 5)).thenReturn(chuvas(20, 25, 30, 25, 25));

        // Act
        Optional<Alerta> alerta = servico.avaliarAlerta(servico.preverParaSensor("P-01", TipoEstrategia.MEDIA_MOVEL));

        // Assert
        assertEquals(NivelAlerta.ATENCAO, alerta.orElseThrow().getNivel());
    }

    @Test
    void naoDeveEmitirAlertaQuandoPrevisaoEstaAbaixoDosLimiares() throws DadosInvalidosException {
        // Arrange: média = 5 mm/h
        when(medicaoDAO.buscarUltimasPorSensor("P-01", 5)).thenReturn(chuvas(4, 5, 6, 5, 5));

        // Act
        Optional<Alerta> alerta = servico.avaliarAlerta(servico.preverParaSensor("P-01", TipoEstrategia.MEDIA_MOVEL));

        // Assert
        assertFalse(alerta.isPresent());
        verify(alertaDAO, never()).salvar(any());
    }

    @Test
    void deveSeCalarComMenosDeCincoLeiturasNoBanco() throws DadosInvalidosException {
        // Arrange: só 3 leituras gravadas
        when(medicaoDAO.buscarUltimasPorSensor("P-01", 5)).thenReturn(chuvas(80, 90, 95));

        // Act + Assert
        assertThrows(DadosInvalidosException.class,
                () -> servico.preverParaSensor("P-01", TipoEstrategia.MEDIA_MOVEL));
        verify(alertaDAO, never()).salvar(any());
    }

    @Test
    void deveGravarLeituraValidaDoSensor() throws Exception {
        // Arrange
        when(sensor.lerValor()).thenReturn(12.5);
        when(sensor.getId()).thenReturn("P-01");
        when(sensor.getTipo()).thenReturn(TipoSensor.PLUVIOMETRO);

        // Act
        Medicao medicao = servico.registrarLeitura(sensor);

        // Assert
        verify(medicaoDAO).salvar(medicao);
        assertEquals(12.5, medicao.getValor(), 0.0001);
    }

    @Test
    void naoDeveGravarLeituraInvalida() throws Exception {
        // Arrange: sensor devolve chuva negativa
        when(sensor.lerValor()).thenReturn(-4.0);
        when(sensor.getId()).thenReturn("P-01");
        when(sensor.getTipo()).thenReturn(TipoSensor.PLUVIOMETRO);

        // Act
        assertThrows(DadosInvalidosException.class, () -> servico.registrarLeitura(sensor));

        // Assert
        verify(medicaoDAO, never()).salvar(any());
    }

    @Test
    void deveRepassarFalhaDoSensorSemGravarNada() throws Exception {
        // Arrange
        when(sensor.lerValor()).thenThrow(new FonteIndisponivelException("sensor P-01 não respondeu"));

        // Act
        FonteIndisponivelException erro = assertThrows(FonteIndisponivelException.class,
                () -> servico.registrarLeitura(sensor));

        // Assert
        assertTrue(erro.getMessage().contains("P-01"));
        verify(medicaoDAO, never()).salvar(any());
    }
}
