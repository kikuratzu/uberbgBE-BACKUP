package com.uber.bg.uber.bg.Repositories.Redis;

import com.uber.bg.uber.bg.Entities.VerificationCode;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface VerificationCodeRepository extends CrudRepository<VerificationCode, String> {
    List<VerificationCode> findByEmail(String email);
}
