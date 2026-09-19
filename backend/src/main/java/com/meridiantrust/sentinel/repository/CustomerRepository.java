package com.meridiantrust.sentinel.repository;

import com.meridiantrust.sentinel.domain.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, String> {
    Page<Customer> findByRiskRating(String riskRating, Pageable pageable);
}
