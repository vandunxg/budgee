package com.budgee.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.budgee.model.*;

@Repository
public interface GroupTransactionRepository extends JpaRepository<GroupTransaction, UUID> {
    List<GroupTransaction> findAllByGroup(Group group);

    List<GroupTransaction> findAllByGroupAndMember(Group group, GroupMember member);

    void deleteAllByGroupId(UUID groupId);

    @Query(
            """
        SELECT gt
        FROM GroupTransaction gt
        JOIN FETCH gt.member m
        JOIN FETCH m.user u
        WHERE gt.id = :id
    """)
    Optional<GroupTransaction> findByIdWithMemberAndUser(@Param("id") UUID id);
}
