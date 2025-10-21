package com.budgee.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.budgee.model.Goal;
import com.budgee.payload.request.GoalRequest;
import com.budgee.payload.response.GoalResponse;

@Mapper
public interface GoalMapper {

    GoalMapper INSTANCE = Mappers.getMapper(GoalMapper.class);

    Goal toGoal(GoalRequest request);

    @Mapping(target = "goalId", source = "goal.id")
    GoalResponse toGoalResponse(Goal goal);
}
