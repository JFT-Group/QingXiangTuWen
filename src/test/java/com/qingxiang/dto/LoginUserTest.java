package com.qingxiang.dto;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginUserTest {

    private LoginUser user(Set<String> permissions) {
        return new LoginUser(1L, "reviewer", "审核员", 3L, "REVIEWER", 0, permissions);
    }

    @Test
    void permissionLookupIsExact() {
        LoginUser user = user(Set.of("article:review", "user:manage"));

        assertTrue(user.hasPermission("article:review"));
        assertFalse(user.hasPermission("article:create"));
        assertFalse(user.hasPermission(null));
    }

    @Test
    void nullPermissionsBecomeEmptySet() {
        LoginUser user = user(null);

        assertTrue(user.getPermissions().isEmpty());
        assertFalse(user.hasPermission("user:manage"));
    }

    @Test
    void permissionsAreCopiedAndImmutable() {
        LoginUser user = user(Set.copyOf(List.of("log:view")));

        assertThrows(UnsupportedOperationException.class,
                () -> user.getPermissions().add("hacked"));
        assertFalse(user.hasPermission("hacked"));
    }
}
