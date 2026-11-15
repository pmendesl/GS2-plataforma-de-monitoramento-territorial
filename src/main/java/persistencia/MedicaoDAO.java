package persistencia;

import dominio.Medicao;
import dominio.TipoSensor;
import excecao.DadosInvalidosException;
import excecao.PersistenciaException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Acesso à tabela medicao. Sempre PreparedStatement com parâmetros "?"
 * e try-with-resources para fechar conexão, comando e resultado.
 */
public class MedicaoDAO {

    private static final String INSERIR =
            "INSERT INTO medicao (sensor_id, tipo_sensor, valor, data_hora) VALUES (?, ?, ?, ?)";
    private static final String BUSCAR_POR_SENSOR =
            "SELECT id, sensor_id, tipo_sensor, valor, data_hora FROM medicao WHERE sensor_id = ? ORDER BY id";
    private static final String BUSCAR_ULTIMAS_POR_SENSOR =
            "SELECT id, sensor_id, tipo_sensor, valor, data_hora FROM medicao WHERE sensor_id = ? ORDER BY id DESC LIMIT ?";
    private static final String CONTAR_POR_SENSOR =
            "SELECT COUNT(*) FROM medicao WHERE sensor_id = ?";

    private final ConexaoBanco banco;

    public MedicaoDAO(ConexaoBanco banco) {
        this.banco = banco;
    }

    public void salvar(Medicao medicao) {
        try (Connection conexao = banco.abrir();
             PreparedStatement comando = conexao.prepareStatement(INSERIR, Statement.RETURN_GENERATED_KEYS)) {
            comando.setString(1, medicao.getSensorId());
            comando.setString(2, medicao.getTipo().name());
            comando.setDouble(3, medicao.getValor());
            comando.setTimestamp(4, Timestamp.valueOf(medicao.getDataHora()));
            comando.executeUpdate();
            try (ResultSet chaves = comando.getGeneratedKeys()) {
                if (chaves.next()) {
                    medicao.definirId(chaves.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("não foi possível gravar a medição do sensor " + medicao.getSensorId(), e);
        }
    }

    /** Histórico completo do sensor, do mais antigo para o mais recente. */
    public List<Medicao> buscarPorSensor(String sensorId) {
        try (Connection conexao = banco.abrir();
             PreparedStatement comando = conexao.prepareStatement(BUSCAR_POR_SENSOR)) {
            comando.setString(1, sensorId);
            return lerLista(comando);
        } catch (SQLException e) {
            throw new PersistenciaException("não foi possível consultar o histórico do sensor " + sensorId, e);
        }
    }

    /** As N leituras mais recentes do sensor, devolvidas em ordem cronológica. */
    public List<Medicao> buscarUltimasPorSensor(String sensorId, int quantidade) {
        try (Connection conexao = banco.abrir();
             PreparedStatement comando = conexao.prepareStatement(BUSCAR_ULTIMAS_POR_SENSOR)) {
            comando.setString(1, sensorId);
            comando.setInt(2, quantidade);
            List<Medicao> medicoes = lerLista(comando);
            Collections.reverse(medicoes);
            return medicoes;
        } catch (SQLException e) {
            throw new PersistenciaException("não foi possível consultar as últimas leituras do sensor " + sensorId, e);
        }
    }

    public int contarPorSensor(String sensorId) {
        try (Connection conexao = banco.abrir();
             PreparedStatement comando = conexao.prepareStatement(CONTAR_POR_SENSOR)) {
            comando.setString(1, sensorId);
            try (ResultSet resultado = comando.executeQuery()) {
                resultado.next();
                return resultado.getInt(1);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("não foi possível contar as leituras do sensor " + sensorId, e);
        }
    }

    private List<Medicao> lerLista(PreparedStatement comando) throws SQLException {
        List<Medicao> medicoes = new ArrayList<>();
        try (ResultSet resultado = comando.executeQuery()) {
            while (resultado.next()) {
                medicoes.add(mapear(resultado));
            }
        }
        return medicoes;
    }

    private Medicao mapear(ResultSet linha) throws SQLException {
        long id = linha.getLong("id");
        try {
            return new Medicao(id, linha.getString("sensor_id"),
                    TipoSensor.valueOf(linha.getString("tipo_sensor")),
                    linha.getDouble("valor"),
                    linha.getTimestamp("data_hora").toLocalDateTime());
        } catch (DadosInvalidosException e) {
            throw new PersistenciaException("registro de medição " + id + " está inválido no banco: " + e.getMessage(), e);
        }
    }
}
