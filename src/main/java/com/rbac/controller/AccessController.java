package com.rbac.controller;

import com.rbac.domain.User;
import com.rbac.service.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
public class AccessController {

    private MyUserDetailsService userDetailsService;

    public AccessController(MyUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<User> getUserInfo(@PathVariable long id) {
        Optional<User> userInfo = userDetailsService.getUserInfo(id);
        if(userInfo.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        userInfo.get().setRoles(null);
        return ResponseEntity.ok(userInfo.get());
    }

    @GetMapping("/admin/user/{id}")
    public ResponseEntity<User> getFullUserInfo(@PathVariable long id) {
        Optional<User> userInfo = userDetailsService.getUserInfo(id);
        if(userInfo.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        return ResponseEntity.ok(userInfo.get());
    }

    @PostMapping("/user/roles")
    public ResponseEntity<Map<Long, Map<String, String>>> addUserRole(@RequestBody Map<Long, String> roles) {
        return ResponseEntity
                .accepted()
                .body(userDetailsService.addRoleToUser(roles));
    }

    @DeleteMapping("/user/roles")
    public ResponseEntity<Map<Long, Map<String, String>>> removeUserFromRole(@RequestBody Map<Long, String> roles) {
        return ResponseEntity
                .accepted()
                .body(userDetailsService.removeUserFromRole(roles));
    }

    @PostMapping("/check-access/{id}")
    public ResponseEntity<Boolean> checkUserAccess(@RequestBody Map<String, String> body,
                                                   @PathVariable long id) {
        return ResponseEntity.ok(userDetailsService.checkUserAccess(body, id));
    }
}
