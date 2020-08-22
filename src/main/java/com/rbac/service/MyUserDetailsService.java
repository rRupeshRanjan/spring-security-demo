package com.rbac.service;

import com.rbac.domain.Role;
import com.rbac.domain.User;
import com.rbac.repository.AccessRepository;
import com.rbac.repository.RoleRepository;
import com.rbac.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("userDetailsService")
@Transactional
public class MyUserDetailsService implements UserDetailsService {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private AccessRepository accessRepository;

    private static final String SUCCESS = "success";
    private static final String FAILED = "failed";
    private static final String RESOURCE = "resource";
    private static final String ACTION = "action";

    public MyUserDetailsService(UserRepository userRepository, RoleRepository roleRepository,
                                AccessRepository accessRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.accessRepository = accessRepository;
    }

    @Override
    public UserDetails loadUserByUsername(final String email) throws UsernameNotFoundException {
        try {
            final User user = userRepository.findByEmail(email);
            if (user == null) {
                throw new UsernameNotFoundException("No user found with username: " + email);
            }

            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),
                    user.isEnabled(),
                    true,
                    true,
                    true,
                    getAuthorities(user.getRoles()));

        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public final Collection<? extends GrantedAuthority> getAuthorities(final Collection<Role> roles) {
        return roles.stream()
                .map(p -> new SimpleGrantedAuthority(p.getName()))
                .collect(Collectors.toList());
    }

    public Optional<User> getUserInfo(long id) {
        return userRepository.findById(id);
    }

    public Map<Long, Map<String, String>> addRoleToUser(Map<Long, String> roles) {
        Map<Long, Map<String, String>> response = new HashMap<>();

        roles.forEach((id, role) -> getUserInfo(id)
                .ifPresent(user -> {
                    Role roleByName = roleRepository.findByName(role);
                    if(roleByName!=null) {
                        if(!user.getRoles().contains(roleByName)) {
                            user.getRoles().add(roleByName);
                            userRepository.save(user);
                            response.put(id, Map.of(role, SUCCESS));
                        } else {
                            response.put(id, Map.of(role, "already holds role"));
                        }
                    } else {
                        response.put(id, Map.of(role, "role does not exist"));
                    }
                })
        );

        return response;
    }

    public Map<Long, Map<String, String>> removeUserFromRole(Map<Long, String> roles) {
        Map<Long, Map<String, String>> response = new HashMap<>();

        roles.forEach((id, role) -> getUserInfo(id)
                .ifPresent(user -> {
                    Role roleByName = roleRepository.findByName(role);
                    if(roleByName!=null) {
                        user.getRoles().remove(roleByName);
                        userRepository.save(user);
                        response.put(id, Map.of(role, SUCCESS));
                    } else {
                        response.put(id, Map.of(role, FAILED));
                    }
                })
        );

        return response;
    }

    public boolean checkUserAccess(Map<String, String> request, long id) {
        return getUserInfo(id)
                .filter(user -> accessRepository.hasUserAccessToResource(user.getRoles(),
                        request.get(RESOURCE), request.get(ACTION)))
                .isPresent();
    }
}