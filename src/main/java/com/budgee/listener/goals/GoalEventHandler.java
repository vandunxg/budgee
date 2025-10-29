package com.budgee.listener.goals;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.budgee.event.application.CategoryDeletedEvent;
import com.budgee.model.Goal;
import com.budgee.repository.GoalCategoryRepository;
import com.budgee.repository.GoalRepository;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "GOAL-EVENT-HANDLER")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GoalEventHandler {

    // -------------------------------------------------------------------
    // REPOSITORY
    // -------------------------------------------------------------------
    GoalCategoryRepository goalCategoryRepository;
    GoalRepository goalRepository;

    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onCategoryDeleted(CategoryDeletedEvent event) {
        UUID categoryId = event.categoryId();
        UUID ownerId = event.ownerId();

        log.info("[onCategoryDeleted] categoryId={} owner={}", categoryId, ownerId);

        goalCategoryRepository.deleteAllByCategoryId(categoryId);

        List<Goal> orphanGoals = goalRepository.findGoalsWithoutAnyCategory(ownerId, categoryId);

        if (!orphanGoals.isEmpty()) {
            log.info("[onCategoryDeleted] deleting {} orphan goals", orphanGoals.size());
            goalRepository.deleteAll(orphanGoals);
        }
    }
}
