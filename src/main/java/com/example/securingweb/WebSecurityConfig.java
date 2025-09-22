package com.example.securingweb;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSecurityConfig {

    // QUICK TEST ONLY — compares raw text from login to raw text in DB
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    // Load users/roles from MySQL table: info(user, password, role)
    @Bean
    public UserDetailsService userDetailsService(DataSource dataSource) {
        JdbcUserDetailsManager mgr = new JdbcUserDetailsManager(dataSource);

        // Get single user row: username, password, enabled
        // Note: backticks because `user` is a reserved word in MySQL
        mgr.setUsersByUsernameQuery(
                "select distinct `user` as username, `password`, true as enabled " +
                        "from info where `user` = ?"
        );

        // Get authorities (roles) for that user
        // If role values are USER/ADMIN, we prefix to ROLE_USER/ROLE_ADMIN
        mgr.setAuthoritiesByUsernameQuery(
                "select `user` as username, " +
                        "       case when upper(role) like 'ROLE_%' then role else concat('ROLE_', role) end as authority " +
                        "from info where `user` = ?"
        );

        return mgr;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // enable later if you add state-changing forms/APIs
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/home", "/css/**", "/js/**", "/login").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")   // only ADMIN allowed
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/hello", true)  // <-- ÉP luôn chuyển sang /hello sau khi login
                        .permitAll()
                )

                .logout(logout -> logout.permitAll())
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
