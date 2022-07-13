package com.wallassouza.bibliotecaapi.api.resource.repository;

import com.wallassouza.bibliotecaapi.api.resource.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan,Long> {
}
