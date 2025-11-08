package com.chao.aicode.converter;

import com.chao.aicode.model.dto.user.UserAddRequest;
import com.chao.aicode.model.dto.user.UserUpdateRequest;
import com.chao.aicode.model.entity.User;
import com.chao.aicode.model.vo.LoginUserVO;
import com.chao.aicode.model.vo.UserVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserConverter {
    UserConverter INSTANCE = Mappers. getMapper(UserConverter.class);

    LoginUserVO LoginVoTo(User user);

    UserVO VoTo(User user);

    User ToAddRequest(UserAddRequest request);
    User ToUpdateRequest(UserUpdateRequest request);
}