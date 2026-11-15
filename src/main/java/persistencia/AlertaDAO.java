package persistencia;

import dominio.Alerta;
import dominio.NivelAlerta;
import dominio.TipoSensor;
import excecao.PersistenciaException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso à tabela alerta (PreparedStatement + try-with-resources).
 */
public class AlertaDAO {

    private static final String INSERIR = """
            INSERT INTO alerta (sensor_id, tipo_sensor, nivel, valor_previsto, limite, estrategia, mensagem, data_hora)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)""";
    private static final String LISTAR_TODOS = """
            SELECT id, sensor_id, tipo_sensor, nivel, valor_previsto, limite, estrategia, mensagem, data_hora
            FROM alerta ORDER BY id DESC""";

    private final ConexaoBanco banco;

    public AlertaDAO(ConexaoBanco banco) {
        this.banco = banco;
    }

    public void salvar(Alerta alerta) {
        try (Connection conexao = banco.abrir();
             PreparedStatement comando = conexao.prepareStatement(INSERIR, Statement.RETURN_GENERATED_KEYS)) {
            comando.setString(1, alerta.getSensorId());
            comando.setString(2, alerta.getTipoSensor().name());
            comando.setString(3, alerta.getNivel().name());
            comando.setDouble(4, alerta.getValorPrevisto());
            comando.setDouble(5, alerta.getLimite());
            comando.setString(6, alerta.getEstrategia());
            comando.setString(7, alerta.getMensagem());
            comando.setTimestamp(8, Timestamp.valueOf(alerta.getDataHora()));
            comando.executeUpdate();
            try (ResultSet chaves = comando.getGeneratedKeys()) {
                if (chaves.next()) {
                    alerta.definirId(chaves.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("não foi possível gravar o alerta do sensor " + alerta.getSensorId(), e);
        }
    }

    /** Todos os alertas já emitidos, do mais recente para o mais antigo. */
    public List<Alerta> listarTodos() {
        List<Alerta> alertas = new ArrayList<>();
        try (Connection conexao = banco.abrir();
             PreparedStatement comando = conexao.prepareStatement(LISTAR_TODOS);
             ResultSet linha = comando.executeQuery()) {
            while (linha.next()) {
                alertas.add(new Alerta(
                        linha.getLong("id"),
                        linha.getString("sensor_id"),
                        TipoSensor.valueOf(linha.getString("tipo_sensor")),
                        NivelAlerta.valueOf(linha.getString("nivel")),
                        linha.getDouble("valor_previsto"),
                        linha.getDouble("limite"),
                        linha.getString("estrategia"),
                        linha.getString("mensagem"),
                        linha.getTimestamp("data_hora").toLocalDateTime()));
            }
            return alertas;
        } catch (SQLException e) {
            throw new PersistenciaException("não foi possível listar os alertas emitidos", e);
        }
    }
}
