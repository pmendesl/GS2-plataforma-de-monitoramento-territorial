package servico;

import dominio.EstrategiaPrevisao;
import dominio.PrevisaoComparacao;
import dominio.PrevisaoMediaMovel;
import dominio.TipoEstrategia;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntFunction;

/**
 * Factory: o único lugar que sabe "dar new" nas estratégias.
 * A escolha é feita por um mapa TipoEstrategia -> construtor, sem if/switch/instanceof.
 * Para adicionar uma estratégia nova basta registrar mais uma linha aqui.
 */
public class FabricaPrevisao {

    private final Map<TipoEstrategia, IntFunction<EstrategiaPrevisao>> construtores = new EnumMap<>(TipoEstrategia.class);
    private final ConfiguracaoSistema configuracao;

    public FabricaPrevisao(ConfiguracaoSistema configuracao) {
        this.configuracao = Objects.requireNonNull(configuracao, "configuracao");
        construtores.put(TipoEstrategia.MEDIA_MOVEL, PrevisaoMediaMovel::new);
        construtores.put(TipoEstrategia.COMPARACAO, PrevisaoComparacao::new);
    }

    /** Cria a estratégia pedida já com a janela definida na configuração do sistema. */
    public EstrategiaPrevisao criar(TipoEstrategia tipo) {
        IntFunction<EstrategiaPrevisao> construtor = construtores.get(Objects.requireNonNull(tipo, "tipo"));
        return construtor.apply(configuracao.getJanelaPrevisao());
    }
}
