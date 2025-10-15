package com.budgee.service;

import java.util.List;
import java.util.UUID;

import com.budgee.payload.request.GoalRequest;
import com.budgee.payload.response.GoalResponse;

public interface GoalService {

    GoalResponse createGoal(GoalRequest request);

    GoalResponse getGoal(UUID id);

    GoalResponse updateGoal(UUID id, GoalRequest request);

    void deleteGoal(UUID id);

    List<GoalResponse> getListGoals();
}
