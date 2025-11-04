package com.budgee.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.budgee.model.Group;
import com.budgee.model.GroupMember;
import com.budgee.model.User;

public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {

    GroupMember findByGroupAndUser(Group group, User user);

    GroupMember findByGroupAndId(Group group, UUID id);

    Boolean existsByGroupAndUser(Group group, User authenticatedUser);

    List<GroupMember> findAllByUser(User user);

    @Query("SELECT gm FROM GroupMember gm JOIN FETCH gm.user WHERE gm.group.id = :groupId")
    List<GroupMember> findAllByGroupId(@Param("groupId") UUID groupId);

    @Query(
            """
        SELECT gm
        FROM GroupMember gm
        JOIN FETCH gm.user u
        WHERE gm.group.id = :groupId
    """)
    List<GroupMember> findAllWithUserByGroupId(@Param("groupId") UUID groupId);
}
