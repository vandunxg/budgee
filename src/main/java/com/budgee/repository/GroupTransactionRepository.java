package com.budgee.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.budgee.model.*;

@Repository
public interface GroupTransactionRepository extends JpaRepository<GroupTransaction, UUID> {
    List<GroupTransaction> findAllByGroup(Group group);
}
