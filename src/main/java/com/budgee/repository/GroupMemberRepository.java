package com.budgee.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.budgee.model.Group;
import com.budgee.model.GroupMember;
import com.budgee.model.User;

public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {

    GroupMember findByGroupAndUser(Group group, User user);

    List<GroupMember> findAllByGroup(Group group);
}
