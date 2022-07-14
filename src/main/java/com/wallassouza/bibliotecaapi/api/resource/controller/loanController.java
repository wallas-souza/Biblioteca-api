package com.wallassouza.bibliotecaapi.api.resource.controller;

import com.wallassouza.bibliotecaapi.api.resource.dto.bookdto.BookDTO;
import com.wallassouza.bibliotecaapi.api.resource.dto.bookdto.LoanDTO;
import com.wallassouza.bibliotecaapi.api.resource.dto.bookdto.LoanFilterDTO;
import com.wallassouza.bibliotecaapi.api.resource.dto.bookdto.ReturnedLoanDTO;
import com.wallassouza.bibliotecaapi.api.resource.model.Book;
import com.wallassouza.bibliotecaapi.api.resource.model.Loan;
import com.wallassouza.bibliotecaapi.api.resource.service.BookService;
import com.wallassouza.bibliotecaapi.api.resource.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class loanController {

    private final LoanService loanService;
    private final BookService bookService;
    private final ModelMapper modelMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody LoanDTO dto) {
        Book book = bookService
                .getBookByIsbn(dto.getIsbn())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book not found for passed isbn"));
        Loan entidade = Loan.builder()
                .book(book)
                .customer(dto.getCustomer())
                .loanDate(LocalDate.now())
                .build();

        entidade = loanService.save(entidade);

        return entidade.getId();
    }

    @PatchMapping("{id}")
    public void returnedBook(@PathVariable Long id, @RequestBody ReturnedLoanDTO dto){
        Loan loan = loanService.getById(id)
                .orElseThrow( ()-> new ResponseStatusException(HttpStatus.NOT_FOUND));
        loan.setReturned(dto.getReturned());

        loanService.update(loan);
    }

    @GetMapping
    public Page<LoanDTO> find(LoanFilterDTO dto, Pageable pageable){
        Page<Loan> result =  loanService.find(dto,pageable);
        List<LoanDTO> loans = result
                .getContent()
                .stream()
                .map(entidade -> {

                    Book book = entidade.getBook();
                    BookDTO bookDTO = modelMapper.map(book, BookDTO.class);
                    LoanDTO loanDTO = modelMapper.map(entidade, LoanDTO.class);
                    loanDTO.setBook(bookDTO);
                    return loanDTO;
                }).collect(Collectors.toList());
        return new PageImpl<LoanDTO>(loans,pageable,result.getTotalElements());
    }

}
