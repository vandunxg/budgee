package com.budgee.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.budgee.model.Category;
import com.budgee.model.GoalCategory;
import com.budgee.model.User;

@Repository
public interface GoalCategoryRepository extends JpaRepository<GoalCategory, UUID> {

    void deleteAllByCategory(Category category);

    List<GoalCategory> findAllByUser(User user);

    @Modifying
    @Query(
            """
               delete from GoalCategory gc
                           where gc.category.id = :categoryId
            """)
    void deleteAllByCategoryId(@Param("categoryId") UUID categoryId);
}
