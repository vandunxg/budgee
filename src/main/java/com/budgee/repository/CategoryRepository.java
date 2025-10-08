package com.budgee.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.budgee.model.Category;

public interface CategoryRepository extends JpaRepository<Category, UUID> {}
