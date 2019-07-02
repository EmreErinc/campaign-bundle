package com.finartz.intern.campaignlogic.commons;

import com.finartz.intern.campaignlogic.model.entity.UserEntity;
import com.finartz.intern.campaignlogic.model.request.RegisterRequest;
import com.finartz.intern.campaignlogic.model.response.LoginResponse;

public class Converters {
  public static UserEntity registerRequestToUserEntity(RegisterRequest request){
    return UserEntity.builder()
        .name(request.getName())
        .lastName(request.getLastName())
        .email(request.getEmail())
        .password(request.getPassword())
        .build();
  }

  public static LoginResponse userEntityToLoginResponse(UserEntity userEntity){
    return LoginResponse.builder()
        .name(userEntity.getName())
        .lastName(userEntity.getLastName())
        .id(userEntity.getId())
        .build();
  }
}
