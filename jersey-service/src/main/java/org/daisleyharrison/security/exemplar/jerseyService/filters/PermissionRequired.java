package org.daisleyharrison.security.samples.jerseyService.filters;

import java.lang.reflect.Method;
import java.util.Map;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import org.daisleyharrison.security.samples.jerseyService.filters.NonceRequired;

import java.util.HashMap;

/**
 * Compile class for @PermitAll, @DenyAll, @NonceRequred and @RolesRequired
 */
public class PermissionRequired {
    private enum Permission {
        DenyAll, PermitAll, RoleRequired
    }

    private static Map<Method,PermissionRequired> requiredByMethod = new HashMap<>();
    
    private Permission permission;
    private String[] roles;
    private PathOnAccessDenied pathOnAccessDenied;
    private boolean nonceRequired;

    public static PermissionRequired getFor(Method method){
        PermissionRequired required = requiredByMethod.get(method);
        if(required == null){
            required = new PermissionRequired(method);
            requiredByMethod.put(method,required);
        }
        return required;
    }
    private PermissionRequired(Method method) {
        if (method.isAnnotationPresent(DenyAll.class)) {
            permission = Permission.DenyAll;
        } else if (method.isAnnotationPresent(RolesAllowed.class)) {
            RolesAllowed rolesAllowed = method.getAnnotation(RolesAllowed.class);
            roles = rolesAllowed.value();
            permission = Permission.RoleRequired;
        } else if (method.isAnnotationPresent(PermitAll.class)) {
            permission = Permission.PermitAll;
        } else {
            Class<?> type = method.getDeclaringClass();
            if (type.isAnnotationPresent(DenyAll.class)) {
                permission = Permission.DenyAll;
            } else if (type.isAnnotationPresent(RolesAllowed.class)) {
                RolesAllowed rolesAllowed = type.getAnnotation(RolesAllowed.class);
                roles = rolesAllowed.value();
                permission = Permission.RoleRequired;
            } else if (type.isAnnotationPresent(PermitAll.class)) {
                permission = Permission.PermitAll;
            } else {
                permission = Permission.DenyAll;
            }
        }

        if (method.isAnnotationPresent(PathOnAccessDenied.class)) {
            this.pathOnAccessDenied = method.getAnnotation(PathOnAccessDenied.class);
        } else if (method.getDeclaringClass().isAnnotationPresent(PathOnAccessDenied.class)) {
            this.pathOnAccessDenied = method.getDeclaringClass().getAnnotation(PathOnAccessDenied.class);
        }

        this.nonceRequired = (method.isAnnotationPresent(NonceRequired.class) 
            || method.getDeclaringClass().isAnnotationPresent(NonceRequired.class));
    }

    public String[] getRoles() {
        return this.roles;
    }

    public Permission getPermission() {
        return this.permission;
    }

    public boolean isPermitAll() {
        return this.permission == Permission.PermitAll;
    }

    public boolean isDenyAll() {
        return this.permission == Permission.DenyAll;
    }

    public boolean isRoleRequired() {
        return this.permission == Permission.RoleRequired;
    }

    public boolean isNonceRequired() {
        return this.nonceRequired;
    }

    @Override
    public String toString() {
        switch (permission) {
        case PermitAll:
            return "Permit All";
        case RoleRequired:
            return "Requires role " + String.join(", ", getRoles());
        case DenyAll:
        default:
            return "Deny All";
        }
    }

    /**
     * @return PathOnAccessDenied return the @PathOnAccessDenied annotation
     */
    public PathOnAccessDenied getPathOnAccessDenied() {
        return pathOnAccessDenied;
    }
}

