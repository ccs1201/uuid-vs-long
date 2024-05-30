package br.com.ccs.uuidvslong.services;

import br.com.ccs.uuidvslong.config.ConfigBean;
import br.com.ccs.uuidvslong.entities.ProdutoUuid;
import br.com.ccs.uuidvslong.repositories.ProdutoUuidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProdutoUuidService {

    private final ProdutoUuidRepository repository;
    private final ConfigBean config;

    @Transactional
    public void saveAllInBatch(Collection<ProdutoUuid> produtos) {
        var contador = 0;
        var start = System.currentTimeMillis();
        for (ProdutoUuid produto : produtos) {
            contador++;
            repository.getEntityManager().persist(produto);
            if (contador % config.getBatch_size() == 0) {
                repository.flush();
                repository.getEntityManager().clear();
            }
        }
        var end = System.currentTimeMillis();
        System.out.println("Tempo total de inserção ProdutoUuid -> "
                .concat(DecimalFormat.getNumberInstance().format(end - start) + "ms"));
        //vamos garantir q não fique nada no cache
        repository.flush();
        repository.getEntityManager().clear();
    }

    @Transactional
    void deleteAll() {
        repository.deleteAllInBatch();
        repository.flush();
    }

    @Transactional(readOnly = true)
    public void count() {
        System.out.println("Total produtosUuid no banco -> "
                .concat(DecimalFormat.getNumberInstance().format(repository.count()) + " registros"));
    }

    @Transactional(readOnly = true)
    public boolean exists(UUID id) {
        return repository.existsById(id);
    }
}
