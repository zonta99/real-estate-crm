package com.realestatecrm.service;

import com.realestatecrm.entity.Permission;
import com.realestatecrm.entity.User;
import com.realestatecrm.enums.Role;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PermissionService {

    public List<com.realestatecrm.entity.Permission> getUserPermissions(User user) {
        return new ArrayList<>(getRolePermissions(user.getRole()));
    }

    private List<Permission> getRolePermissions(Role role) {
        List<Permission> permissions = new ArrayList<>();

        switch (role) {
            case ADMIN:
                permissions.add(new Permission("users", List.of("create", "read", "update", "delete")));
                permissions.add(new Permission("properties", List.of("create", "read", "update", "delete")));
                permissions.add(new Permission("customers", List.of("create", "read", "update", "delete")));
                permissions.add(new Permission("property-attributes", List.of("create", "read", "update", "delete")));
                permissions.add(new Permission("reports", List.of("read", "generate")));
                permissions.add(new Permission("system", List.of("read", "update")));
                permissions.add(new Permission("hierarchy", List.of("create", "read", "update", "delete")));
                break;

            case BROKER:
                permissions.add(new Permission("users", List.of("create", "read", "update")));
                permissions.add(new Permission("properties", List.of("create", "read", "update", "delete")));
                permissions.add(new Permission("customers", List.of("create", "read", "update", "delete")));
                permissions.add(new Permission("property-attributes", List.of("read")));
                permissions.add(new Permission("reports", List.of("read", "generate")));
                permissions.add(new Permission("hierarchy", List.of("create", "read", "update")));
                break;

            case AGENT:
                permissions.add(new Permission("properties", List.of("create", "read", "update")));
                permissions.add(new Permission("customers", List.of("create", "read", "update")));
                permissions.add(new Permission("property-attributes", List.of("read")));
                permissions.add(new Permission("reports", List.of("read")));
                break;

            case ASSISTANT:
                permissions.add(new Permission("properties", List.of("read")));
                permissions.add(new Permission("customers", List.of("read", "update")));
                permissions.add(new Permission("property-attributes", List.of("read")));
                break;

            default:
                break;
        }

        return permissions;
    }
}