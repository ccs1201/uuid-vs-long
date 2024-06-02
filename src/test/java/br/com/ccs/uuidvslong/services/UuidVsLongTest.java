package br.com.ccs.uuidvslong.services;

import br.com.ccs.uuidvslong.config.ConfigBean;
import br.com.ccs.uuidvslong.entities.ProdutoLong;
import br.com.ccs.uuidvslong.entities.ProdutoUuid;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@SpringBootTest
public class UuidVsLongTest {

    private static final String NOME = "Produto Teste ";

    @Autowired
    private ProdutoUuidService produtoUuidService;
    @Autowired
    private ProdutoLongService produtoLongService;
    @Autowired
    private ConfigBean config;
    private List<UUID> uuids;
    private List<Integer> longs;

    private static long start;

    @Test
    public void runTests() throws InterruptedException {

        List<ProdutoUuid> produtosUuid = new ArrayList<>(config.getQtd_produtos());
        List<ProdutoLong> produtosLong = new ArrayList<>(config.getQtd_produtos());
        uuids = new ArrayList<>(config.getQtd_produtos());
        longs = new ArrayList<>(config.getQtd_produtos());

        System.out.println("Criando ".concat(DecimalFormat.getIntegerInstance().format(config.getQtd_produtos())).concat(" Produtos..."));
        for (int i = 0; i < config.getQtd_produtos(); i++) {
            produtosUuid.add(new ProdutoUuid(UUID.randomUUID(), NOME + 1));
            produtosLong.add(new ProdutoLong( i, NOME + i));

            if (i % 100 == 0) {
                uuids.add(produtosUuid.get(i).getId());
                longs.add(produtosLong.get(i).getId());
            }
        }

        System.out.println("Aquecendo o banco");
        var listLongs = produtosLong.subList(0, config.getQtd_produtos() / 100);
        produtoLongService.saveAllInBatch(listLongs);
        var listUuids = produtosUuid.subList(0, config.getQtd_produtos() / 100);
        produtoUuidService.saveAllInBatch(listUuids);

        produtoLongService.count();
        produtoUuidService.count();
        System.out.println("Banco aquecido... limpando dados...");
        produtoLongService.deleteAll();
        produtoUuidService.deleteAll();

        System.out.println("\nSalvando Produtos Long...");
        produtoLongService.saveAllInBatch(produtosLong);
        produtoLongService.count();

        System.out.println("\nSalvando Produtos UUID...");
        produtoUuidService.saveAllInBatch(produtosUuid);
        produtoUuidService.count();

        /*
        Setar nulo para que o GC possa liberar da memória os objetos
        que já foram persistido e não nos interessam mais.
         */
        produtosUuid = null;
        produtosLong = null;
        listUuids = null;
        listLongs = null;

        System.out.println("\nVamos dar uma pequena pausa para que BD possar fazer qq otimização nos seus índices. \n");
        Thread.sleep(5000);

        testeFindLongsOrdenado();
        testeFindUUIDsOrdenado();
        testeFindLongsEmbaralhado();
        testeFindUUIDsEmbaralhado();
    }

    public void testeFindUUIDsOrdenado() {
        start = System.currentTimeMillis();
        uuids.forEach(id -> Assertions.assertTrue(produtoUuidService.exists(id)));
        logTimeSpent("Teste FindUUIDs Ordenado size: ", uuids.size());
    }

    public void testeFindLongsOrdenado() {
        start = System.currentTimeMillis();
        longs.forEach(id -> Assertions.assertTrue(produtoLongService.exists(id)));
        logTimeSpent("Teste FindLongs Ordenado size: ", longs.size());
    }

    public void testeFindUUIDsEmbaralhado() {
        Collections.shuffle(uuids);
        start = System.currentTimeMillis();
        uuids.forEach(id -> Assertions.assertTrue(produtoUuidService.exists(id)));
        logTimeSpent("Teste FindUUIDs Embaralhado size: ", uuids.size());
    }

    public void testeFindLongsEmbaralhado() {
        Collections.shuffle(uuids);
        start = System.currentTimeMillis();
        longs.forEach(id -> Assertions.assertTrue(produtoLongService.exists(id)));
        logTimeSpent("Teste FindLongs Embaralhado size: ", longs.size());
    }

    private void logTimeSpent(String message, int size) {
        var end = System.currentTimeMillis();
        System.out.println(message
                .concat(DecimalFormat.getIntegerInstance().format(size))
                .concat(" Tempo total de execução -> ")
                .concat(DecimalFormat.getInstance().format(end - start))
                .concat(" ms"));
    }
}
