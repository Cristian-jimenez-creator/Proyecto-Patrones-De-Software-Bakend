package com.smartpool.backend.controller;

import com.smartpool.backend.model.Account;
import com.smartpool.backend.repository.AccountRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class RegistrationController {
    private final AccountRepository accounts;
    private final PasswordEncoder passwordEncoder;

    public RegistrationController(AccountRepository accounts, PasswordEncoder passwordEncoder) {
        this.accounts = accounts;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationResponse register(@Valid @RequestBody RegistrationRequest request) {
        String email = request.email().trim().toLowerCase();
        if (accounts.existsByEmailIgnoreCase(email)) {
            throw new IllegalStateException("Ya existe una cuenta con ese correo.");
        }
        Account account = accounts.save(new Account(request.name().trim(), email,
                passwordEncoder.encode(request.password())));
        return new RegistrationResponse(account.id, account.name, account.email);
    }

    public record RegistrationRequest(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(min = 12, max = 72) String password) {}

    public record RegistrationResponse(Long id, String name, String email) {}
}
