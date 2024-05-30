package br.com.ccs.uuidvslong.repositories;

import br.com.ccs.uuidvslong.entities.ProdutoUuid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProdutoUuidRepository extends JpaRepository<ProdutoUuid, UUID>, CustomRepository {
}
