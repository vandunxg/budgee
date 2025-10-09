package com.budgee.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.budgee.model.Category;
import com.budgee.model.User;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Page<Category> findAllByUser(User user, Pageable pageable);
}
