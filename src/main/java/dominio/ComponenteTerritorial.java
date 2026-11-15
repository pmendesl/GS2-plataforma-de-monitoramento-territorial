package dominio;

import excecao.DadosInvalidosException;

/**
 * Qualquer elemento físico monitorado no território (ponto de monitoramento,
 * estação, equipamento público...). Cada tipo concreto decide como verifica o próprio status.
 */
public abstract class ComponenteTerritorial {

    private final String id;
    private String nome;
    private String regiao;
    private StatusComponente status;

    protected ComponenteTerritorial(String id, String nome, String regiao) throws DadosInvalidosException {
        if (id == null || id.isBlank()) {
            throw new DadosInvalidosException("componente territorial sem identificador");
        }
        this.id = id;
        setNome(nome);
        setRegiao(regiao);
        this.status = StatusComponente.DESCONHECIDO;
    }

    /** Verifica o estado atual do componente, atualiza e devolve o status. */
    public abstract StatusComponente verificarStatus();

    protected void setStatus(StatusComponente status) {
        this.status = status;
    }

    public void setNome(String nome) throws DadosInvalidosException {
        if (nome == null || nome.isBlank()) {
            throw new DadosInvalidosException("o nome do componente " + id + " não pode ser vazio");
        }
        this.nome = nome;
    }

    public void setRegiao(String regiao) throws DadosInvalidosException {
        if (regiao == null || regiao.isBlank()) {
            throw new DadosInvalidosException("a região do componente " + id + " não pode ser vazia");
        }
        this.regiao = regiao;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getRegiao() {
        return regiao;
    }

    public StatusComponente getStatus() {
        return status;
    }
}
