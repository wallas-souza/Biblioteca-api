package com.wallassouza.bibliotecaapi.service;

import com.wallassouza.bibliotecaapi.api.resource.exception.BusinessException;
import com.wallassouza.bibliotecaapi.api.resource.model.Book;
import com.wallassouza.bibliotecaapi.api.resource.repository.BookRepository;
import com.wallassouza.bibliotecaapi.api.resource.service.BookService;
import com.wallassouza.bibliotecaapi.api.resource.service.BookServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    BookService bookService;

    @MockBean
    BookRepository repository;

    @BeforeEach
    public void setUp() {
        this.bookService = new BookServiceImpl(repository);
    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBook() {
        //cenario
        Book book = createValidBook();
        Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(false);
        Mockito.when(repository.save(book)).thenReturn(Book.builder()
                .id(11)
                .isbn("123")
                .autor("Fulano")
                .titulo("As aventuras").build());

        //execucao
        Book savedBook = bookService.save(book);

        //verificacao
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getIsbn()).isEqualTo("123");
        assertThat(savedBook.getAutor()).isEqualTo("Fulano");
        assertThat(savedBook.getTitulo()).isEqualTo("As aventuras");
    }

    private Book createValidBook() {
        return Book.builder().isbn("123").titulo("Fulano").autor("As aventuras").build();
    }

    @Test
    @DisplayName("Deve lançar error quando salvar livro com ISBN duplicada.")
    public void naoDeveSavarLivroComISBNDublicada(){
        //cenario
        Book book = createValidBook();
        Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(true);

        //execuçao
        Throwable exception = Assertions.catchThrowable(() -> bookService.save(book));

        //verificacoes
        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("ISBN já cadastrado");

        Mockito.verify(repository,Mockito.never()).save(book);
    }

}
