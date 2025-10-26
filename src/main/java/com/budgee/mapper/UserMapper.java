package com.budgee.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.budgee.enums.Currency;
import com.budgee.enums.Role;
import com.budgee.enums.SubscriptionTier;
import com.budgee.enums.UserStatus;
import com.budgee.model.User;
import com.budgee.payload.request.RegisterRequest;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "user.password", ignore = true)
    User toUser(
            RegisterRequest request,
            UserStatus status,
            Currency currency,
            Role role,
            SubscriptionTier subscriptionTier);
}
