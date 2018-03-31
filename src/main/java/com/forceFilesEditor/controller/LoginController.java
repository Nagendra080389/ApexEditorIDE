package com.forceFilesEditor.controller;

import com.forceFilesEditor.model.Person;
import com.forceFilesEditor.service.Hash;
import com.forceFilesEditor.service.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This controller Is responsible for login.
 */
@RestController
@RequestMapping("/auth")
public class LoginController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);
    private LoginService loginService;

    public LoginController(final LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping
    public ResponseEntity<Hash> login(@RequestBody Person person){


        if (this.loginService.checkUsername(person)) {
            // Login Successful
            Hash hash = this.loginService.addInPersonMap(person);
            LOGGER.info("Login Successful with username: "+ person.getUsername());
            return ResponseEntity.ok().body(hash);

        } else {
            LOGGER.warn("Login UnSuccessful with username: "+ person.getUsername());
            return ResponseEntity.badRequest().build();
        }
    }

}
