package dominio;

import excecao.DadosInvalidosException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Ponto vulnerável do bairro (baixada, encosta...) com seus sensores instalados.
 */
public class PontoMonitoramento extends ComponenteTerritorial {

    private final List<Sensor> sensores = new ArrayList<>();

    public PontoMonitoramento(String id, String nome, String regiao) throws DadosInvalidosException {
        super(id, nome, regiao);
    }

    public void adicionarSensor(Sensor sensor) {
        sensores.add(Objects.requireNonNull(sensor, "sensor"));
    }

    public List<Sensor> getSensores() {
        return List.copyOf(sensores);
    }

    @Override
    public StatusComponente verificarStatus() {
        long funcionando = sensores.stream().filter(Sensor::estaFuncionando).count();
        StatusComponente novoStatus;
        if (funcionando == 0) {
            novoStatus = StatusComponente.INOPERANTE;
        } else if (funcionando < sensores.size()) {
            novoStatus = StatusComponente.PARCIAL;
        } else {
            novoStatus = StatusComponente.OPERANTE;
        }
        setStatus(novoStatus);
        return novoStatus;
    }
}
