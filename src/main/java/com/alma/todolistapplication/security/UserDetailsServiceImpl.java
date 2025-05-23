package com.alma.todolistapplication.security;

import com.alma.todolistapplication.model.User;
import com.alma.todolistapplication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("userDetailsService") // Giving it a specific bean name
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true) // Good practice for read-only operations
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Our User entity already implements UserDetails, so we can return it directly
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with username: " + username));

        // You could also load by email if you prefer:
        // User user = userRepository.findByUsernameOrEmail(username) // Assuming you have this method
        // .orElseThrow(() ->
        // new UsernameNotFoundException("User not found with username or email: " + username));

        return user;
    }
}