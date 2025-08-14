package org.apolenkov.application.service.user;

import java.util.Collection;
import java.util.List;
import org.apolenkov.application.domain.port.UserRepository;
import org.apolenkov.application.model.User;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Profile({"jpa", "prod"})
public class JpaUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public JpaUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository
                .findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        Collection<? extends GrantedAuthority> authorities = user.getRoles().isEmpty()
                ? List.of(new SimpleGrantedAuthority("ROLE_USER"))
                : user.getRoles().stream().map(SimpleGrantedAuthority::new).toList();
        String password = user.getPasswordHash() == null ? "{noop}" : user.getPasswordHash();
        return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(password)
                .authorities(authorities)
                .build();
    }
}
