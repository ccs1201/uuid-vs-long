package br.com.ccs.uuidvslong.services;

import br.com.ccs.uuidvslong.config.ConfigBean;
import br.com.ccs.uuidvslong.entities.ProdutoLong;
import br.com.ccs.uuidvslong.repositories.ProdutoLongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class ProdutoLongService {

    private final ProdutoLongRepository repository;
    private final ConfigBean config;

    @Transactional
    public void saveAllInBatch(Collection<ProdutoLong> produtos) {
        var contador = 0;
        var start = System.currentTimeMillis();
        for (ProdutoLong produto : produtos) {
            contador++;
            repository.getEntityManager().persist(produto);
            if (contador % config.getBatch_size() == 0) {
                repository.flush();
                repository.getEntityManager().clear();
            }
        }
        var end = System.currentTimeMillis();
        System.out.println("Tempo total de inserção ProdutoLong -> "
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
        System.out.println("Total produtosLong no banco -> "
                .concat(DecimalFormat.getNumberInstance().format(repository.count()) + " registros"));
    }

    @Transactional(readOnly = true)
    public boolean exists(Long id) {
        return repository.existsById(id);
    }
}
