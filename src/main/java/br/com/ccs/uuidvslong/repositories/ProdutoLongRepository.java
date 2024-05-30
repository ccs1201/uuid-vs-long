package br.com.ccs.uuidvslong.repositories;

import br.com.ccs.uuidvslong.entities.ProdutoLong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProdutoLongRepository extends JpaRepository<ProdutoLong, Long>, CustomRepository {
}
