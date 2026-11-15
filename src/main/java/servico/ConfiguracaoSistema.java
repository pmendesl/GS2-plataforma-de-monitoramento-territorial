package servico;

import dominio.Limiar;
import dominio.TipoSensor;
import excecao.DadosInvalidosException;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Singleton: única fonte dos limiares e parâmetros do sistema.
 * Todo o sistema (serviço, fábrica, menu) lê a mesma instância, então uma
 * alteração de limiar vale imediatamente para todos.
 */
public final class ConfiguracaoSistema {

    private static final ConfiguracaoSistema INSTANCIA = new ConfiguracaoSistema();

    private final Map<TipoSensor, Limiar> limiares = new EnumMap<>(TipoSensor.class);
    private int janelaPrevisao;
    private String urlBanco;

    private ConfiguracaoSistema() {
        restaurarPadroes();
    }

    public static ConfiguracaoSistema getInstancia() {
        return INSTANCIA;
    }

    /** Volta aos valores de fábrica (usado na inicialização e nos testes). */
    public synchronized void restaurarPadroes() {
        limiares.put(TipoSensor.PLUVIOMETRO, limiarPadrao(20.0, 35.0, 50.0));   // mm/h
        limiares.put(TipoSensor.NIVEL_CORREGO, limiarPadrao(2.5, 3.5, 4.5));    // m
        limiares.put(TipoSensor.UMIDADE_SOLO, limiarPadrao(70.0, 85.0, 95.0));  // %
        janelaPrevisao = 5;
        urlBanco = System.getProperty("banco.url", "jdbc:h2:./dados/territorio");
    }

    private static Limiar limiarPadrao(double atencao, double alerta, double critico) {
        try {
            return new Limiar(atencao, alerta, critico);
        } catch (DadosInvalidosException e) {
            throw new IllegalStateException("limiar padrão mal configurado no código: " + e.getMessage(), e);
        }
    }

    public synchronized Limiar getLimiar(TipoSensor tipo) {
        return limiares.get(tipo);
    }

    public synchronized void definirLimiar(TipoSensor tipo, Limiar limiar) {
        limiares.put(Objects.requireNonNull(tipo, "tipo"), Objects.requireNonNull(limiar, "limiar"));
    }

    public synchronized int getJanelaPrevisao() {
        return janelaPrevisao;
    }

    public synchronized void setJanelaPrevisao(int janela) throws DadosInvalidosException {
        if (janela < 2 || janela > 50) {
            throw new DadosInvalidosException("a janela de previsão deve ficar entre 2 e 50 leituras (recebido " + janela + ")");
        }
        this.janelaPrevisao = janela;
    }

    public synchronized String getUrlBanco() {
        return urlBanco;
    }
}
