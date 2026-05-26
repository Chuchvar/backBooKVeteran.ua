package com.example.sunatoriVeteran.security;

import com.example.sunatoriVeteran.model.User;
import com.example.sunatoriVeteran.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Cacheable("users")
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> userOpt = userRepository.findFirstByEmail(email);
        if (userOpt.isEmpty()) {
            throw new UsernameNotFoundException("Користувача не знайдено з email: " + email);
        }
        return new CustomUserDetails(userOpt.get());
    }
}
