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
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
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
        when(repository.existsByIsbn(Mockito.anyString())).thenReturn(false);
        when(repository.save(book)).thenReturn(Book.builder()
                .id(1l)
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
    public void naoDeveSavarLivroComISBNDublicada() {
        //cenario
        Book book = createValidBook();
        when(repository.existsByIsbn(Mockito.anyString())).thenReturn(true);

        //execuçao
        Throwable exception = Assertions.catchThrowable(() -> bookService.save(book));

        //verificacoes
        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("ISBN já cadastrado");

        Mockito.verify(repository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Deve Obter um livro por Id")
    public void getbyIdTest() {
        Long id = 1l;
        Book book = createValidBook();
        book.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(book));

        Optional<Book> foundBook = bookService.getById(id);

        assertThat(foundBook.isPresent()).isTrue();
        assertThat(foundBook.get().getId()).isEqualTo(id);
        assertThat(foundBook.get().getAutor()).isEqualTo(book.getAutor());
        assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
        assertThat(foundBook.get().getTitulo()).isEqualTo(book.getTitulo());

    }

    @Test
    @DisplayName("Deve retornar vazio quando Obter um id que não existe")
    public void bookNotFoundTest() {
        Long id = 1l;
        when(repository.findById(id)).thenReturn(Optional.empty());

        Optional<Book> book = bookService.getById(id);

        assertThat(book.isPresent()).isFalse();

    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBookTest() {
        Book book = Book.builder().id(1l).build();

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> bookService.delete(book));

        Mockito.verify(repository, Mockito.times(1)).delete(book);
    }

    @Test
    @DisplayName("Deve ocorrer erro ao tentar deletar um book invalido.")
    public void deleteInvalidBookTest() {
        Book book = new Book();

        org.junit.jupiter.api.Assertions
                .assertThrows(IllegalArgumentException.class, () -> bookService.delete(book));

        Mockito.verify( repository, Mockito.never() ).delete(book);

    }

    @Test
    @DisplayName("Deve atualizar um livro")
    public void updateBookTest(){
        //cenario
        long id = 1l;

        //livro a atualizar
        Book updatingBook = Book.builder().id(id).build();

        //simulacao
        Book updatedBook = createValidBook();
        updatedBook.setId(id);
        when(repository.save(updatingBook)).thenReturn(updatedBook);

        //execucao
        Book book = bookService.update(updatingBook);

        //verificacao
        assertThat(book.getId()).isEqualTo(updatedBook.getId());
        assertThat(book.getTitulo()).isEqualTo(updatedBook.getTitulo());
        assertThat(book.getIsbn()).isEqualTo(updatedBook.getIsbn());
        assertThat(book.getAutor()).isEqualTo(updatedBook.getAutor());
    }

    @Test
    @DisplayName("Deve filtrar livros pelas propriedades")
    public void findBookTest(){
        //cenario
        Book book = createValidBook();
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Book> list = Arrays.asList(book);
        Page<Book> page = new PageImpl<Book>(list, pageRequest, 1);
        when(repository.findAll(Mockito.any(Example.class),Mockito.any(PageRequest.class)))
                .thenReturn(page);
        //execucao
        Page<Book> resultado = bookService.find(book, pageRequest);

        //verificacao
        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getContent()).isEqualTo(list);
        assertThat(resultado.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(resultado.getPageable().getPageSize()).isEqualTo(10);

    }

    @Test
    @DisplayName("Deve obter um livro pelo isbn")
    public void getBookByIsbnTest(){
        String isbn = "1230";
        Book book = Book.builder().id(1l).isbn(isbn).build();
        when(repository.findByIsbn(isbn))
                .thenReturn(Optional.of(book));

        Optional<Book> bookByIsbn = bookService.getBookByIsbn(isbn);

        assertThat(bookByIsbn.isPresent()).isTrue();
        assertThat(bookByIsbn.get().getId()).isEqualTo(1l);
        assertThat(bookByIsbn.get().getIsbn()).isEqualTo("1230");

        verify(repository, times(1)).findByIsbn(isbn);
    }

}
