package com.wallassouza.bibliotecaapi.api.resource.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

@Table
public class Loan {

//    @Id
//    @Column
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customer;

    private Book book;

    private LocalDate loanDate;

    private Boolean returned;
}
