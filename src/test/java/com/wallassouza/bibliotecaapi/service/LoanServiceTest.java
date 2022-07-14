package com.wallassouza.bibliotecaapi.service;

import com.wallassouza.bibliotecaapi.api.resource.dto.bookdto.LoanFilterDTO;
import com.wallassouza.bibliotecaapi.api.resource.exception.BusinessException;
import com.wallassouza.bibliotecaapi.api.resource.model.Book;
import com.wallassouza.bibliotecaapi.api.resource.model.Loan;
import com.wallassouza.bibliotecaapi.api.resource.repository.LoanRepository;
import com.wallassouza.bibliotecaapi.api.resource.service.LoanService;
import com.wallassouza.bibliotecaapi.api.resource.service.LoanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {

    LoanService loanService;
    @MockBean
    LoanRepository loanRepository;

    @BeforeEach
    public void setUp(){
        this.loanService = new LoanServiceImpl(loanRepository);
    }

    @Test
    @DisplayName("Deve salvar um emprestimo.")
    public void saveLoanTest(){
        Book book = Book.builder().id(1l).build();
        String customer = "Fulano";
        Loan loan = Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();

        Loan savedLoan = Loan.builder()
                .id(1l)
                .customer(customer)
                .book(book)
                .loanDate(LocalDate.now())
                .build();

        when(loanRepository.existsByBookAndNotReturned(book)).thenReturn(false);
        when(loanRepository.save(loan)).thenReturn(savedLoan);

        Loan saved = loanService.save(loan);

        assertThat(saved.getId()).isEqualTo(savedLoan.getId());
        assertThat(saved.getBook()).isEqualTo(savedLoan.getBook());
        assertThat(saved.getCustomer()).isEqualTo(savedLoan.getCustomer());
        assertThat(saved.getLoanDate()).isEqualTo(savedLoan.getLoanDate());
    }

    @Test
    @DisplayName("Deve lançar erro de negocio ao salvar um emprestimo com livro já emprestado.")
    public void LoanedBookSaveTest(){
        Book book = Book.builder().id(1l).build();
        String customer = "Fulano";

        Loan savingLoan = Loan.builder()
                .customer(customer)
                .book(book)
                .loanDate(LocalDate.now())
                .build();

        when(loanRepository.existsByBookAndNotReturned(book)).thenReturn(true);

        Throwable ex = catchThrowable(() -> loanService.save(savingLoan));

        assertThat(ex)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Book já emprestado");

        verify(loanRepository, never()).save(savingLoan);
    }

    @Test
    @DisplayName("Deve obter as informaçoes de um emprestimo pelo ID")
    public void getLoanDetaisTest(){
        //cenario
        Long id = 1l;
        Loan loan = createLoan();
        loan.setId(id);

        Mockito.when(loanRepository.findById(id)).thenReturn(Optional.of(loan));

        //execucao
        Optional<Loan> result = loanService.getById(id);

        //verificacao
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getId()).isEqualTo(id);
        assertThat(result.get().getCustomer()).isEqualTo(loan.getCustomer());
        assertThat(result.get().getBook()).isEqualTo(loan.getBook());
        assertThat(result.get().getLoanDate()).isEqualTo(loan.getLoanDate());

        verify(loanRepository).findById(id);
    }

    @Test
    @DisplayName("Deve atualizar um emprestimo.")
    public void updateLoanTest(){
        Loan loan = createLoan();
        String customer = "Fulano";
        loan.setReturned(true);

        when( loanRepository.save(loan)).thenReturn(loan);

        Loan updatedLoan = loanService.update(loan);

        assertThat(updatedLoan.getReturned()).isTrue();
        verify(loanRepository).save(loan);
    }

    public static Loan createLoan(){
        Book book = Book.builder().id(1l).build();
        String customer = "Fulano";

        return Loan.builder()
                .customer(customer)
                .book(book)
                .loanDate(LocalDate.now())
                .build();
    }

    @Test
    @DisplayName("Deve filtrar emprestimos pelas propriedades")
    public void findLoanTest(){
        //cenario
        LoanFilterDTO loanFilterDTO = LoanFilterDTO.builder().customer("Fulano").isbn("321").build();

        Loan loan = createLoan();
        loan.setId(1l);

        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Loan> list = Arrays.asList(loan);

        Page<Loan> page = new PageImpl<Loan>(list, pageRequest, 1);
        when(loanRepository
                .findByBookIsbnOrCustomer(
                        Mockito.anyString(),
                        Mockito.anyString(),
                        Mockito.any(PageRequest.class)))
                .thenReturn(page);
        //execucao
        Page<Loan> resultado = loanService.find(loanFilterDTO, pageRequest);

        //verificacao
        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getContent()).isEqualTo(list);
        assertThat(resultado.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(resultado.getPageable().getPageSize()).isEqualTo(10);

    }
}
