package com.teste.testejwt.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teste.testejwt.domain.AppUser;
import com.teste.testejwt.domain.Role;
import com.teste.testejwt.exceptions.InvalidTokenException;
import com.teste.testejwt.repository.AppUserRepository;
import com.teste.testejwt.repository.RoleRepository;
import com.teste.testejwt.security.JWTConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service @Transactional
public class AppUserService implements UserDetailsService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTConfig jwtConfig;

    public AppUser saveAppUser(AppUser user) {
        log.info("Saving new user '{}' to database", user.getName());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return appUserRepository.save(user);
    }

    public Role saveRole(Role role) {
        log.info("Saving new role '{}' to database", role.getName());
        return roleRepository.save(role);
    }

    public void addRoleToAppUser(String username, String roleName) {
        log.info("Adding role '{}' to user '{}'", roleName, username);
        AppUser appUser = appUserRepository.findAppUserByUsername(username);
        Role role = roleRepository.findRoleByName(roleName);
        appUser.getRoles().add(role);

    }

    public AppUser getAppUser(String username) {
        log.info("Fetching user '{}'", username);
        return appUserRepository.findAppUserByUsername(username);
    }

    public List<AppUser> getAllUsers() {
        log.info("Fetching all users");
        return appUserRepository.findAll();
    }

    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authorizationHeader = request.getHeader("refresh_token");
        if(authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            try {
                String refreshToken = authorizationHeader.substring("Bearer ".length());
                DecodedJWT decodedJWT = jwtConfig.decodeJWT(refreshToken);
                String username = decodedJWT.getSubject();
                AppUser user = getAppUser(username);

                String accessToken = JWT.create()
                        .withSubject(user.getUsername())
                        .withExpiresAt(new Date(System.currentTimeMillis() + 10 * 60 * 1000))
                        .withIssuer(request.getRequestURL().toString())
                        .withClaim("roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                        .sign(jwtConfig.getAlgorithm());

                Map<String, String> tokens = new HashMap<>();
                tokens.put("access_token", accessToken);
                tokens.put("refresh_token", refreshToken);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), tokens);
            } catch (TokenExpiredException e) {
                log.error("Error refreshing token: {}", e.getMessage());
                throw new InvalidTokenException(e.getMessage());
            }
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Refresh token is missing");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = appUserRepository.findAppUserByUsername(username);
        if(appUser == null) {
            log.error("User not found in the database");
            throw new UsernameNotFoundException("User not found in the database");
        }
        log.info("User found in the database: '{}'", username);
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        appUser.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
        });
        return new User(appUser.getUsername(), appUser.getPassword(), authorities);
    }

}
