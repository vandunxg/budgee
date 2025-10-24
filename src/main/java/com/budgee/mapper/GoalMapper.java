package com.budgee.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.budgee.model.Goal;
import com.budgee.model.User;
import com.budgee.payload.request.GoalRequest;
import com.budgee.payload.response.GoalResponse;

@Mapper(componentModel = "spring")
public interface GoalMapper {

    @Mapping(target = "user", source = "user")
    @Mapping(target = "currentAmount", expression = "java(java.math.BigDecimal.ZERO)")
    Goal toGoal(GoalRequest request, User user);

    @Mapping(target = "goalId", source = "goal.id")
    @Mapping(
            target = "categoriesId",
            expression =
                    "java(goal.getGoalCategories().stream().map(gc -> gc.getCategory().getId()).toList())")
    @Mapping(
            target = "walletsId",
            expression =
                    "java(goal.getGoalWallets().stream().map(gw -> gw.getWallet().getId()).toList())")
    GoalResponse toGoalResponse(Goal goal);
}
