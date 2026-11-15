package dominio;

import excecao.DadosInvalidosException;
import servico.ConfiguracaoSistema;

import java.util.Objects;

/**
 * Dados gerais do território monitorado. Atributos privados, alterados só
 * por setters que validam. Mudar limiares exige o código de acesso da coordenação.
 * Nada de dados pessoais: o território é descrito por região e números agregados.
 */
public class DadosTerritorio {

    private String regiao;
    private int populacaoEstimada;
    private int domiciliosEmAreaDeRisco;
    private final String codigoAcesso;
    private final ConfiguracaoSistema configuracao;

    public DadosTerritorio(String regiao, int populacaoEstimada, int domiciliosEmAreaDeRisco,
                           String codigoAcesso, ConfiguracaoSistema configuracao) throws DadosInvalidosException {
        if (codigoAcesso == null || codigoAcesso.length() < 4) {
            throw new DadosInvalidosException("o código de acesso precisa ter pelo menos 4 caracteres");
        }
        this.codigoAcesso = codigoAcesso;
        this.configuracao = Objects.requireNonNull(configuracao, "configuracao");
        setRegiao(regiao);
        setPopulacaoEstimada(populacaoEstimada);
        setDomiciliosEmAreaDeRisco(domiciliosEmAreaDeRisco);
    }

    /** Altera os limiares de um tipo de sensor, somente com o código de acesso correto. */
    public void alterarLimiar(String codigoInformado, TipoSensor tipo,
                              double atencao, double alerta, double critico) throws DadosInvalidosException {
        if (!codigoAcesso.equals(codigoInformado)) {
            throw new DadosInvalidosException("código de acesso incorreto: os limiares não foram alterados");
        }
        if (tipo == null) {
            throw new DadosInvalidosException("tipo de sensor não informado");
        }
        configuracao.definirLimiar(tipo, new Limiar(atencao, alerta, critico));
    }

    public Limiar consultarLimiar(TipoSensor tipo) {
        return configuracao.getLimiar(tipo);
    }

    public void setRegiao(String regiao) throws DadosInvalidosException {
        if (regiao == null || regiao.isBlank()) {
            throw new DadosInvalidosException("o nome da região não pode ser vazio");
        }
        this.regiao = regiao;
    }

    public void setPopulacaoEstimada(int populacaoEstimada) throws DadosInvalidosException {
        if (populacaoEstimada < 0) {
            throw new DadosInvalidosException("população negativa não é válida: " + populacaoEstimada);
        }
        this.populacaoEstimada = populacaoEstimada;
    }

    public void setDomiciliosEmAreaDeRisco(int domicilios) throws DadosInvalidosException {
        if (domicilios < 0) {
            throw new DadosInvalidosException("número de domicílios em área de risco não pode ser negativo: " + domicilios);
        }
        this.domiciliosEmAreaDeRisco = domicilios;
    }

    public String getRegiao() {
        return regiao;
    }

    public int getPopulacaoEstimada() {
        return populacaoEstimada;
    }

    public int getDomiciliosEmAreaDeRisco() {
        return domiciliosEmAreaDeRisco;
    }
}
