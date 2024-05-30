package br.com.ccs.uuidvslong.repositories;

import jakarta.persistence.EntityManager;

public interface CustomRepository {

    EntityManager getEntityManager();
}
