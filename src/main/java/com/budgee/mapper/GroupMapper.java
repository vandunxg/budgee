package com.budgee.mapper;

import java.math.BigDecimal;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.budgee.model.Group;
import com.budgee.payload.request.group.GroupRequest;
import com.budgee.payload.response.group.GroupResponse;

@Mapper(componentModel = "spring")
public interface GroupMapper {

    Group toGroup(GroupRequest request);

    @Mapping(target = "groupId", source = "group.id")
    @Mapping(target = "groupName", source = "group.name")
    @Mapping(target = "totalSponsorship", source = "totalSponsorship")
    GroupResponse toGroupResponse(Group group, BigDecimal totalSponsorship);

    @Mapping(target = "groupId", source = "group.id")
    @Mapping(target = "groupName", source = "group.name")
    @Mapping(target = "totalIncomeAndSponsorship", source = "totalIncomeAndSponsorship")
    @Mapping(target = "totalExpense", source = "totalExpense")
    @Mapping(target = "members", expression = "java( null )")
    GroupResponse toGroupResponse(
            Group group, BigDecimal totalIncomeAndSponsorship, BigDecimal totalExpense);
}
