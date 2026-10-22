package org.tp.sesam.security.services;

import org.tp.sesam.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implémentation de UserDetailsService pour Spring Security.
 * <p>
 * Spring Security appelle loadUserByUsername() lors de l'authentification
 * (via AuthenticationManager) pour récupérer l'utilisateur depuis la BDD.
 * <p>
 * L'entité User implémente UserDetails, donc on peut la retourner directement.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Spring Security lève UsernameNotFoundException si l'utilisateur n'existe pas
        // → convertie en BadCredentialsException par Spring (pas de fuite d'information)
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Utilisateur non trouvé : "
                ));
    }
}
