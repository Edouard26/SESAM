package org.tp.sesam.services.commons;

import org.tp.sesam.models.commons.AbstractPersistableWithIdSetter;
import org.tp.sesam.repositories.commons.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

/**
 * CRUD technique. Chaque service concret ajoute ses règles métier.
 */
@Validated
@Transactional(readOnly = true)
public abstract class AbstractService<T extends AbstractPersistableWithIdSetter<Long>,
        R extends BaseRepository<T>> implements Service<T, Long> {
    protected final R repository;

    protected AbstractService(R repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    @Validated({jakarta.validation.groups.Default.class, org.tp.sesam.models.validators.groups.CreateGroup.class})
    public T create(T entity) {
        if (entity.getId() != null || entity.getVersion() != null)
            throw new ServiceException("Une création ne doit pas fournir d'ID ni de version");
        return repository.save(entity);
    }

    @Override
    @Transactional
    @Validated({jakarta.validation.groups.Default.class, org.tp.sesam.models.validators.groups.UpdateGroup.class})
    public T update(T entity) {
        if (entity.getId() == null || entity.getVersion() == null)
            throw new ServiceException("ID et version obligatoires pour une mise à jour");
        if (!repository.existsById(entity.getId()))
            throw new ResourceNotFoundException("Ressource introuvable : " + entity.getId());
        return repository.save(entity);
    }

    @Override
    public Optional<T> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Page<T> findAll(org.springframework.data.jpa.domain.Specification<T> spec, Pageable pageable) {
        return repository.findAll(spec, pageable);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        T entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ressource introuvable : " + id));
        repository.delete(entity);
    }

    @Override
    @Transactional
    public void delete(T entity) {
        repository.delete(entity);
    }

    @Override
    @Transactional
    public void deleteAll() {
        repository.deleteAll();
    }

    @Override
    public long count() {
        return repository.count();
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }
}
