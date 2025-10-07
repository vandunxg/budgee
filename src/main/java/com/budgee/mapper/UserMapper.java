package com.budgee.mapper;


import com.budgee.model.User;
import com.budgee.payload.request.RegisterRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper( UserMapper.class );

    @Mapping(target = "user.password", ignore = true)
    User toUser (RegisterRequest request);
}

