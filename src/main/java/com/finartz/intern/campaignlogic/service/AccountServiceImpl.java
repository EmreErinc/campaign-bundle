package com.finartz.intern.campaignlogic.service;

import com.finartz.intern.campaignlogic.commons.Converters;
import com.finartz.intern.campaignlogic.model.entity.AccountEntity;
import com.finartz.intern.campaignlogic.model.request.LoginRequest;
import com.finartz.intern.campaignlogic.model.request.RegisterRequest;
import com.finartz.intern.campaignlogic.model.response.LoginResponse;
import com.finartz.intern.campaignlogic.model.response.RegisterResponse;
import com.finartz.intern.campaignlogic.model.value.Role;
import com.finartz.intern.campaignlogic.repository.AccountRepository;
import com.finartz.intern.campaignlogic.security.JwtTokenProvider;
import com.finartz.intern.campaignlogic.security.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service(value = "accountService")
public class AccountServiceImpl implements AccountService, UserDetailsService {
  private final JwtTokenProvider jwtTokenProvider;
  private final AccountRepository accountRepository;
  private final CartService cartService;

  @Autowired
  public AccountServiceImpl(AccountRepository accountRepository,
                            JwtTokenProvider jwtTokenProvider,
                            CartService cartService) {
    this.accountRepository = accountRepository;
    this.jwtTokenProvider = jwtTokenProvider;
    this.cartService = cartService;
  }

  @Override
  public RegisterResponse addUser(RegisterRequest request) {
    if (accountRepository.existsByEmail(request.getEmail())) {
      throw new RuntimeException("User Account Already Exists");
    }

    AccountEntity accountEntity = accountRepository.save(Converters.registerRequestToUserEntity(request));
    String cartId = cartService.createCart(accountEntity.getId());
    ;

    return RegisterResponse.builder()
        .id(accountEntity.getId().toString())
        .name(accountEntity.getName())
        .lastName(accountEntity.getLastName())
        .token(generateToken(accountEntity.getId().toString(), cartId, Role.USER))
        .build();
  }

  @Override
  public LoginResponse loginUser(LoginRequest request) {
    Optional<AccountEntity> optionalUserEntity = accountRepository.findByEmailAndPassword(request.getEmail(), Utils.encrypt(request.getPassword()));

    if (!optionalUserEntity.isPresent()) {
      throw new ApplicationContextException("User Not Found");
    }

    String cartId = cartService.createCart(optionalUserEntity.get().getId());

    LoginResponse loginResponse = Converters.accountToLoginResponse(optionalUserEntity.get());
    loginResponse.setToken(generateToken(optionalUserEntity.get().getId().toString(), cartId, optionalUserEntity.get().getRole()));

    return loginResponse;
  }

  @Override
  public RegisterResponse addSellerAccount(RegisterRequest request) {
    if (accountRepository.existsByEmail(request.getEmail())) {
      throw new RuntimeException("Seller Account Already Exists");
    }

    AccountEntity accountEntity = accountRepository.save(Converters.registerSellerRequestToUserEntity(request));
    String cartId = cartService.createCart(accountEntity.getId());

    return RegisterResponse.builder()
        .id(accountEntity.getId().toString())
        .name(accountEntity.getName())
        .lastName(accountEntity.getLastName())
        .token(generateToken(accountEntity.getId().toString(), cartId, Role.SELLER))
        .build();
  }


  private String generateToken(String userId, String cartId, Role role) {
    SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role.name());
    //authorities.add(new SimpleGrantedAuthority(role.name()));
    //roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(role.name())));
    return jwtTokenProvider.generateToken(userId, cartId, authority);
  }

  @Override
  public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
    //AccountEntity userEntity = userRepository.findByEmail(email);
    Optional<AccountEntity> userEntity = accountRepository.findById(Integer.valueOf(userId));
    if (!userEntity.isPresent()) {
      try {
        throw new Exception("AccountEntity Not Found");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return new User(userEntity.get().getEmail(), userEntity.get().getPassword(), getAuthority(userEntity.get()));
  }

  private Set<GrantedAuthority> getAuthority(AccountEntity accountEntity) {
    Set<GrantedAuthority> authorities = new HashSet<>();
    //accountEntity.getRole().getRoles().forEach(role -> authorities.add(new SimpleGrantedAuthority(role.name())));
    authorities.add(new SimpleGrantedAuthority(accountEntity.getRole().name()));
    return authorities;
  }
}
