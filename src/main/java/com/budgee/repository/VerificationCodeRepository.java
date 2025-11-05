package com.budgee.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.budgee.enums.VerificationType;
import com.budgee.model.User;
import com.budgee.model.VerificationCode;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, UUID> {

    Optional<VerificationCode> findTopByUserAndTypeAndCodeOrderByCreatedAtDesc(
            User user, VerificationType type, String code);

    void deleteByUserAndType(User user, VerificationType type);

    @Query(
            "SELECT v FROM VerificationCode v WHERE v.user = :user AND v.type = :type ORDER BY v.createdAt DESC")
    List<VerificationCode> findLatestByUserAndType(User user, VerificationType type);

    VerificationCode findLastestVerificationCodeByUserAndType(User user, VerificationType type);
}
