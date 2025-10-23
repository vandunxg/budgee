package com.budgee.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.budgee.model.Group;

@Repository
public interface GroupRepository extends JpaRepository<Group, UUID> {}
