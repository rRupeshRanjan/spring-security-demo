package com.rbac.repository;

import com.rbac.domain.Role;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class AccessRepository {
    private static Map<String, Map<String, List<String>>> accessMap;

    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final String DELETE = "DELETE";

    public AccessRepository() {
        accessMap = new HashMap<>() {{
            put("USER", Map.of("/user/{id}", Collections.singletonList(GET)));
            put("ADMIN", Map.of(
                    "/admin/user/{id}", Collections.singletonList(GET),
                    "/user/{id}/roles", Arrays.asList(POST, DELETE)));
        }};
    }

    public boolean hasUserAccessToResource(Collection<Role> roles, String resource, String action) {
        for(Role role: roles) {
            String roleName = role.getName();
            boolean hasAccessToResource = accessMap.get(roleName).containsKey(resource);
            if (hasAccessToResource) {
                return accessMap.get(roleName).get(resource).contains(action);
            }
        }

        return false;
    }
}
