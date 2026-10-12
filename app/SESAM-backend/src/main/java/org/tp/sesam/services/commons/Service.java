package org.tp.sesam.services.commons;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Contrat CRUD : les cas métier spécifiques sont ajoutés aux interfaces filles.
 */
public interface Service<T, ID> {
    T create(@NotNull @Valid T entity);

    T update(@NotNull @Valid T entity);

    Optional<T> findById(ID id);

    Page<T> findAll(Pageable pageable);

    Page<T> findAll(org.springframework.data.jpa.domain.Specification<T> spec, Pageable pageable);

    void deleteById(ID id);

    void delete(T entity);

    void deleteAll();

    long count();

    boolean existsById(ID id);
}
