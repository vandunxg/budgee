package com.budgee.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.budgee.model.Group;
import com.budgee.model.GroupSharing;
import com.budgee.model.User;

@Repository
public interface GroupSharingRepository extends JpaRepository<GroupSharing, UUID> {
    boolean existsByGroupAndSharedUser(Group group, User sharedUser);

    GroupSharing findByGroupAndSharedUser(Group group, User sharedUser);

    List<GroupSharing> findAllByGroup(Group group);

    void deleteAllByGroupId(UUID groupId);
}
