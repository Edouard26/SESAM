package org.tp.sesam.services.impl;
import org.tp.sesam.models.entities.User;
import org.tp.sesam.repositories.UserRepository;
import org.tp.sesam.services.UserService;
import org.tp.sesam.services.commons.AbstractService;
import org.springframework.stereotype.Service;
@Service
public class UserServiceImpl extends AbstractService<User, UserRepository> implements UserService {
    public UserServiceImpl(UserRepository repository) { super(repository); }
    // Les cas métier supplémentaires hérités retournent 501 jusqu'à leur implémentation.
}
