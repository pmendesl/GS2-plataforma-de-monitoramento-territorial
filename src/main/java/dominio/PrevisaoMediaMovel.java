package dominio;

import excecao.DadosInvalidosException;

import java.util.List;

/**
 * Previsão = média das últimas N leituras.
 * Suaviza picos isolados: um minuto de chuva forte não dispara alerta sozinho.
 */
public class PrevisaoMediaMovel extends EstrategiaPrevisao {

    public PrevisaoMediaMovel(int janela) {
        super("Média Móvel", janela);
    }

    @Override
    public int leiturasNecessarias() {
        return getJanela();
    }

    @Override
    public ResultadoPrevisao prever(List<Medicao> historico) throws DadosInvalidosException {
        validarHistorico(historico);

        int fim = historico.size();
        int inicio = fim - getJanela();
        double previsto = media(historico, inicio, fim);

        Medicao ultima = historico.get(fim - 1);
        String explicacao = String.format("média das últimas %d leituras", getJanela());
        return new ResultadoPrevisao(ultima.getSensorId(), ultima.getTipo(), getNome(),
                previsto, getJanela(), explicacao);
    }

    @Override
    public String descrever() {
        return super.descrever() + ": repete a média recente, bom para tendência estável";
    }
}
