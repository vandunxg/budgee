package com.budgee.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.budgee.model.GroupTransaction;
import com.budgee.payload.request.group.GroupTransactionRequest;
import com.budgee.payload.response.group.GroupTransactionResponse;

@Mapper(componentModel = "spring")
public interface GroupTransactionMapper {

    GroupTransaction toGroupTransaction(GroupTransactionRequest request);

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "groupId", source = "group.id")
    GroupTransactionResponse toGroupTransactionResponse(GroupTransaction transaction);
}
