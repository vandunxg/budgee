package com.budgee.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.budgee.model.Goal;
import com.budgee.payload.request.GoalRequest;
import com.budgee.payload.response.GoalResponse;

@Mapper(componentModel = "spring")
public interface GoalMapper {

    Goal toGoal(GoalRequest request);

    @Mapping(target = "goalId", source = "goal.id")
    GoalResponse toGoalResponse(Goal goal);
}
