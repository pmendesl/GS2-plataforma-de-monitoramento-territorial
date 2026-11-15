package dominio;

import excecao.DadosInvalidosException;

import java.util.List;

/**
 * Compara o período recente (últimas N leituras) com o período anterior (N leituras antes delas)
 * e projeta a mesma variação para frente:
 * previsto = médiaRecente + (médiaRecente - médiaAnterior).
 * Reage mais rápido que a média móvel quando a chuva está aumentando.
 */
public class PrevisaoComparacao extends EstrategiaPrevisao {

    public PrevisaoComparacao(int janela) {
        super("Comparação de Períodos", janela);
    }

    @Override
    public int leiturasNecessarias() {
        return getJanela() * 2;
    }

    @Override
    public ResultadoPrevisao prever(List<Medicao> historico) throws DadosInvalidosException {
        validarHistorico(historico);

        int fim = historico.size();
        int meio = fim - getJanela();
        int inicio = meio - getJanela();
        double mediaAnterior = media(historico, inicio, meio);
        double mediaRecente = media(historico, meio, fim);
        double variacao = mediaRecente - mediaAnterior;

        Medicao ultima = historico.get(fim - 1);
        TipoSensor tipo = ultima.getTipo();
        double previsto = tipo.limitarNaFaixa(mediaRecente + variacao);

        String tendencia = variacao > 0 ? "alta" : variacao < 0 ? "queda" : "estável";
        String explicacao = String.format(
                "período recente %.2f vs anterior %.2f %s: tendência de %s (%+.2f), projetada para frente",
                mediaRecente, mediaAnterior, tipo.getUnidade(), tendencia, variacao);
        return new ResultadoPrevisao(ultima.getSensorId(), tipo, getNome(),
                previsto, leiturasNecessarias(), explicacao);
    }

    @Override
    public String descrever() {
        return super.descrever() + ": projeta a tendência, bom para perceber piora rápida";
    }
}
