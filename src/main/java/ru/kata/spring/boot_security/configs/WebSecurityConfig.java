package ru.kata.spring.boot_security.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import ru.kata.spring.boot_security.services.UserService;

@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final SuccessUserHandler successUserHandler;
    private final UserService myUserDetailsService;

    @Autowired
    public WebSecurityConfig(SuccessUserHandler successUserHandler, UserService myUserDetailsService) {
        this.successUserHandler = successUserHandler;
        this.myUserDetailsService = myUserDetailsService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/login").permitAll()
                .antMatchers("/**").hasAnyRole("ADMIN", "USER")
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/processLogin")
                .successHandler(successUserHandler)
                .permitAll()
                .usernameParameter("email")
                .passwordParameter("password")
                .and()
                .logout()
                .logoutSuccessUrl("/login")
                .permitAll()
                .and()
                .csrf().disable();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setPasswordEncoder(BCryptCoder.passwordEncoder());
        authenticationProvider.setUserDetailsService(myUserDetailsService);
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager customAuthenticationManager() throws Exception {
        return authenticationManager();
    }

}