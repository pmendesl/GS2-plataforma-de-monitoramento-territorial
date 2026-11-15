package persistencia;

import excecao.PersistenciaException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Abre conexões JDBC com o H2 e cria as tabelas se ainda não existirem.
 * Por padrão o banco fica no arquivo ./dados/territorio.mv.db,
 * então o histórico sobrevive ao reiniciar o programa.
 */
public class ConexaoBanco {

    private static final String USUARIO = "sa";
    private static final String SENHA = "";

    private static final String CRIAR_MEDICAO = """
            CREATE TABLE IF NOT EXISTS medicao (
                id           BIGINT AUTO_INCREMENT PRIMARY KEY,
                sensor_id    VARCHAR(20)  NOT NULL,
                tipo_sensor  VARCHAR(30)  NOT NULL,
                valor        DOUBLE       NOT NULL,
                data_hora    TIMESTAMP    NOT NULL
            )""";

    private static final String CRIAR_ALERTA = """
            CREATE TABLE IF NOT EXISTS alerta (
                id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                sensor_id       VARCHAR(20)  NOT NULL,
                tipo_sensor     VARCHAR(30)  NOT NULL,
                nivel           VARCHAR(10)  NOT NULL,
                valor_previsto  DOUBLE       NOT NULL,
                limite          DOUBLE       NOT NULL,
                estrategia      VARCHAR(60)  NOT NULL,
                mensagem        VARCHAR(600) NOT NULL,
                data_hora       TIMESTAMP    NOT NULL
            )""";

    private static final String CRIAR_INDICE = "CREATE INDEX IF NOT EXISTS idx_medicao_sensor ON medicao(sensor_id)";

    private final String url;

    public ConexaoBanco(String url) {
        this.url = url;
    }

    public Connection abrir() throws SQLException {
        return DriverManager.getConnection(url, USUARIO, SENHA);
    }

    public void inicializarEsquema() {
        try (Connection conexao = abrir(); Statement comando = conexao.createStatement()) {
            comando.execute(CRIAR_MEDICAO);
            comando.execute(CRIAR_ALERTA);
            comando.execute(CRIAR_INDICE);
        } catch (SQLException e) {
            throw new PersistenciaException("não foi possível criar as tabelas no banco " + url, e);
        }
    }
}
