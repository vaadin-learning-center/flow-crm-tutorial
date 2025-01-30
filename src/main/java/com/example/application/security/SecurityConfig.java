package com.example.application.security;

import java.util.Collections;

import com.example.application.views.LoginView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.vaadin.flow.spring.security.VaadinAwareSecurityContextHolderStrategy;
import com.vaadin.flow.spring.security.VaadinWebSecurity;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends VaadinWebSecurity {

  private static class CrmInMemoryUserDetailsManager extends InMemoryUserDetailsManager {
    public CrmInMemoryUserDetailsManager() {
      createUser(new User("user",
              "{noop}userpass",
              Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))));
      createUser(new User("admin",
              "{noop}adminpass",
              Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN"))));
    }
  }

  @Autowired
  private ApplicationContext applicationContext;

  @Bean
  public SwitchUserFilter switchUserFilter() {
    SwitchUserFilter filter = new SwitchUserFilter();
    filter.setSecurityContextHolderStrategy(
            applicationContext.getBean(VaadinAwareSecurityContextHolderStrategy.class));
    filter.setUserDetailsService(userDetailsService());
    filter.setSwitchUserMatcher(antMatcher(HttpMethod.GET, "/impersonate"));
    filter.setSwitchFailureUrl("/switchUser");
    filter.setExitUserMatcher(antMatcher(HttpMethod.GET, "/impersonate/exit"));
    filter.setTargetUrl("/");
    return filter;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // Authorize access to /images/ without authentication
    http.authorizeHttpRequests(auth -> auth.requestMatchers(new AntPathRequestMatcher("/images/**")).permitAll());
    http.authorizeHttpRequests(auth -> auth.requestMatchers(new AntPathRequestMatcher("/switchUser")).hasAnyRole("ADMIN", "PREVIOUS_ADMINISTRATOR"));
    http.authorizeHttpRequests(auth -> auth.requestMatchers(new AntPathRequestMatcher("/impersonate/exit")).hasRole("PREVIOUS_ADMINISTRATOR"));

    // Set default security policy that permits Vaadin internal requests and
    // denies all other
    super.configure(http);
    setLoginView(http, LoginView.class, "/logout");
  }

  @Bean
  public InMemoryUserDetailsManager userDetailsService() {
    return new CrmInMemoryUserDetailsManager();
  }
}
