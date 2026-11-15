package persistencia;

import dominio.Medicao;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Bônus: exporta o histórico consultado para CSV (separador ";", abre direto no Excel/LibreOffice).
 */
public class ExportadorCsv {

    public Path exportar(List<Medicao> medicoes, Path destino) throws IOException {
        Path pasta = destino.toAbsolutePath().getParent();
        if (pasta != null) {
            Files.createDirectories(pasta);
        }
        try (BufferedWriter escritor = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            escritor.write("id;sensor;tipo;valor;unidade;data_hora");
            escritor.newLine();
            for (Medicao m : medicoes) {
                escritor.write(String.format(Locale.ROOT, "%d;%s;%s;%.2f;%s;%s",
                        m.getId(), m.getSensorId(), m.getTipo().name(), m.getValor(),
                        m.getTipo().getUnidade(), m.getDataHora()));
                escritor.newLine();
            }
        }
        return destino.toAbsolutePath();
    }
}
