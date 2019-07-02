package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.commons.Converters;
import com.finartz.intern.campaignlogic.security.JwtTokenProvider;
import com.finartz.intern.campaignlogic.model.entity.UserEntity;
import com.finartz.intern.campaignlogic.model.request.LoginRequest;
import com.finartz.intern.campaignlogic.model.request.RegisterRequest;
import com.finartz.intern.campaignlogic.model.response.LoginResponse;
import com.finartz.intern.campaignlogic.model.response.RegisterResponse;
import com.finartz.intern.campaignlogic.model.value.Role;
import com.finartz.intern.campaignlogic.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("accountService")
public class AccountServiceImpl implements  AccountService, UserDetailsService {
  private final JwtTokenProvider jwtTokenProvider;
  private final AccountRepository accountRepository;

  @Autowired
  public AccountServiceImpl(AccountRepository accountRepository, JwtTokenProvider jwtTokenProvider) {
    this.accountRepository = accountRepository;
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @Override
  public RegisterResponse addUser(RegisterRequest request) {
    UserEntity userEntity = accountRepository.save(Converters.registerRequestToUserEntity(request));

    //TODO create cart
    String cartId = "null-cart-id";

    return RegisterResponse.builder()
        .name(userEntity.getName())
        .lastName(userEntity.getLastName())
        .token(generateToken(userEntity.getId(), cartId, Role.USER))
        .build();
  }

  @Override
  public LoginResponse loginUser(LoginRequest request) {
    Optional<UserEntity> optionalUserEntity = accountRepository.findByEmailAndPassword(request.getEmail(), request.getPassword());

    if (!optionalUserEntity.isPresent()){
      throw new RuntimeException("User Not Found");
    }

    //TODO create cart
    String cartId = "null-cart-id";

    LoginResponse loginResponse = Converters.userEntityToLoginResponse(optionalUserEntity.get());
    loginResponse.setToken(generateToken(optionalUserEntity.get().getId(), cartId, Role.USER));

    return loginResponse;
  }



  private String generateToken(String userId, String cartId, Role role) {
    SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role.name());
    //authorities.add(new SimpleGrantedAuthority(role.name()));
    //roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(role.name())));
    return jwtTokenProvider.generateToken(userId, cartId, authority);
  }

  @Override
  public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
    //UserEntity userEntity = userRepository.findByEmail(email);
    Optional<UserEntity> userEntity = accountRepository.findById(userId);
    if (!userEntity.isPresent()) {
      try {
        throw new Exception("UserEntity Not Found");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return new org.springframework.security.core.userdetails.User(userEntity.get().getEmail(), userEntity.get().getPassword(), getAuthority(userEntity.get()));
  }

  private Set getAuthority(UserEntity userEntity) {
    Set authorities = new HashSet<>();
    //userEntity.getRole().getRoles().forEach(role -> authorities.add(new SimpleGrantedAuthority(role.name())));
    authorities.add(userEntity.getRole());
    return authorities;
  }
}
