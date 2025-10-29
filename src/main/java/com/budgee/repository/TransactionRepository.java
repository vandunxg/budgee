package com.budgee.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.budgee.model.Category;
import com.budgee.model.Transaction;
import com.budgee.model.User;
import com.budgee.model.Wallet;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> getTransactionsByCategory(Category category);

    void deleteAllByCategoryAndUser(Category category, User user);

    void deleteAllByWalletAndUser(Wallet wallet, User user);

    List<Transaction> getAllByCategory(Category category);

    @Modifying
    @Query(
            """
            delete from Transaction tr
            where
            tr.category.id = :categoryId
            and
            tr.user.id = :userId
            """)
    void deleteAllByCategoryIdAndUserId(
            @Param("categoryId") UUID categoryId, @Param("userId") UUID userId);
}
