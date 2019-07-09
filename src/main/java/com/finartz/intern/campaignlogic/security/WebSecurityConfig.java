package com.finartz.intern.campaignlogic.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.finartz.intern.campaignlogic.security.SecurityConstants.*;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
  @Autowired
  private JWTAuthenticationEntryPoint authenticationEntryPoint;

  @Autowired
  JwtTokenProvider tokenProvider;

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }

  @Bean
  public JWTAuthenticationFilter authenticationFilterBean() {
    return new JWTAuthenticationFilter();
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .cors().and().csrf().disable()
        .authorizeRequests()
        .antMatchers(SIGN_IN_URL).permitAll()
        .antMatchers(SIGN_UP_URL).permitAll()
        .antMatchers(SELLER_SIGN_UP).permitAll()
        .antMatchers(ITEM_LIST).permitAll()
        .antMatchers(ITEM_DETAIL).permitAll()
        .antMatchers(SELLER_DETAIL).permitAll()
        .antMatchers(SELLER_ITEMS).permitAll()
        .anyRequest().authenticated()
        .and()
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint);

    http.addFilterBefore(authenticationFilterBean(), UsernamePasswordAuthenticationFilter.class);
  }
}
