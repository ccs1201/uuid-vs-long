package br.com.ccs.uuidvslong.services;

import br.com.ccs.uuidvslong.config.ConfigBean;
import br.com.ccs.uuidvslong.entities.ProdutoLong;
import br.com.ccs.uuidvslong.entities.ProdutoUuid;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@SpringBootTest
public class UuidVsLongConcorrenteTest {

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
        var qtdItensPorLista = 1_000;

        AtomicReference<List<ProdutoUuid>> produtosUuid = new AtomicReference<>(new LinkedList<>());
        AtomicReference<List<ProdutoLong>> produtosLong = new AtomicReference<>(new LinkedList<>());
        var threads = config.getQtd_produtos() / qtdItensPorLista;
        uuids = new LinkedList<>();
        longs = new LinkedList<>();

        var futures = new CompletableFuture[threads];
        AtomicInteger futureIndex = new AtomicInteger();
        var executor = Executors.newFixedThreadPool(config.getThreads(), Thread.ofVirtual().factory());

        System.out.println("Criando "
                .concat(DecimalFormat.getIntegerInstance()
                        .format(config.getQtd_produtos()))
                .concat(" Produtos..."));

        try {

            for (int i = 1; i < (config.getQtd_produtos() + 1); i++) {
                produtosUuid.get().add(new ProdutoUuid(UUID.randomUUID(), NOME + 1));
                produtosLong.get().add(new ProdutoLong(i, NOME + i));

                if (i % config.getBatch_size() == 0) {
                    uuids.add(produtosUuid.get().getLast().getId());
                    longs.add(produtosLong.get().getLast().getId());
                }

                if (i % qtdItensPorLista == 0) {
                    var listaUUID = new ArrayList<>(produtosUuid.get());
                    var listaLong = new ArrayList<>(produtosLong.get());
                    produtosUuid.set(new ArrayList<>());
                    produtosLong.set(new ArrayList<>());

                    futures[futureIndex.getAndIncrement()] = CompletableFuture.runAsync(() -> {

                        try {
                            produtoLongService.saveAllInBatch(new ArrayList<>(listaLong));
                            produtoUuidService.saveAllInBatch(new ArrayList<>(listaUUID));

                        } catch (Exception e) {
                            throw new CompletionException(e);
                        }

                    }, executor);
                }
            }
            CompletableFuture.allOf(futures).join();
            System.out.println("Qtd Threads criadas -> " + NumberFormat.getInstance().format(futures.length));
        } catch (CompletionException e) {
            // tenta para todas as threads em execução
            executor.shutdown();
            e.getCause().printStackTrace();
        } finally {
            futures = null;
        }

        produtoLongService.saveAllInBatch(produtosLong.get());
        produtoUuidService.saveAllInBatch(produtosUuid.get());

        System.out.println("\nSalvando Produtos Long...");
        produtoLongService.count();

        System.out.println("\nSalvando Produtos UUID...");
        produtoUuidService.count();

        System.out.println("\nVamos dar uma pequena pausa para que BD possar fazer qq otimização nos seus índices. \n");
        Thread.sleep(5_000);

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
