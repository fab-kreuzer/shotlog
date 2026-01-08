package dev.fkreuzer.shotlog.service;

import dev.fkreuzer.shotlog.domain.UserAccount;
import dev.fkreuzer.shotlog.repository.UserAccountRepository;
import dev.fkreuzer.shotlog.security.SecurityUser;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    public DatabaseUserDetailService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        UserAccount userAccount = userAccountRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        return new SecurityUser(userAccount);
    }
}
