package com.budgee.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.budgee.model.Goal;
import com.budgee.model.User;

@Repository
public interface GoalRepository extends JpaRepository<Goal, UUID> {

    @Query(
            """
                SELECT g
                FROM Goal g
                WHERE g.user.id = :userId
                  AND g.id NOT IN (
                      SELECT gc.goal.id
                      FROM GoalCategory gc
                      WHERE gc.category.id = :categoryId
                  )
            """)
    List<Goal> findGoalsWithoutAnyCategory(
            @Param("userId") UUID userId, @Param("categoryId") UUID categoryId);

    List<Goal> findAllByUser(User user);
}
