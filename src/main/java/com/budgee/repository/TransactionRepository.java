package com.budgee.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.budgee.model.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {}
