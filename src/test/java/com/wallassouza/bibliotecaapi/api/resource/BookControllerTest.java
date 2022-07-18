package com.wallassouza.bibliotecaapi.api.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallassouza.bibliotecaapi.api.resource.controller.BookController;
import com.wallassouza.bibliotecaapi.api.resource.dto.bookdto.BookDTO;
import com.wallassouza.bibliotecaapi.api.resource.exception.BusinessException;
import com.wallassouza.bibliotecaapi.api.resource.model.Book;
import com.wallassouza.bibliotecaapi.api.resource.service.BookService;
import com.wallassouza.bibliotecaapi.api.resource.service.LoanService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc
public class BookControllerTest {

    static String BOOK_API = "/api/livros";

    @Autowired
    MockMvc mvc;

    @MockBean
    BookService service;

    @MockBean
    LoanService loanService;

    @Test
    @DisplayName("Deve criar um livro com sucesso")
    public void createBookTest() throws Exception {

        BookDTO dto = createNewBook();
        Book savedBook = Book.builder().id(10l).autor("Wallas").titulo("As aventuras").isbn("123456789").build();

        BDDMockito.given(service.save(Mockito.any(Book.class))).willReturn(savedBook);
        String json  = new ObjectMapper().writeValueAsString(dto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").value(10))
                .andExpect(jsonPath("titulo").value(dto.getTitulo()))
                .andExpect(jsonPath("autor").value(dto.getAutor()))
                .andExpect(jsonPath("isbn").value(dto.getIsbn()));

    }

    private BookDTO createNewBook() {
        return BookDTO.builder().autor("Wallas").titulo("As aventuras").isbn("123456789").build();
    }

    @Test
    @DisplayName("Deve lançar um erro de validaçao quando não houver dados suficiente para criar um livro.")
    public void createInvalidBookTest() throws Exception {

        String json  = new ObjectMapper().writeValueAsString(new BookDTO());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(3)));
    }

    @Test
    @DisplayName("Deve lançar um erro quando tentar cadastrar um livro com o mesmo isbn.")
    public void createBookWithDuplicateIsbn() throws Exception {
        BookDTO dto = createNewBook();
        String json  = new ObjectMapper().writeValueAsString(dto);
        String mensagemError = "ISBN já cadastrado";
        BDDMockito.given(service.save(Mockito.any(Book.class)))
                .willThrow(new BusinessException(mensagemError));


        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors",hasSize(1)))
                .andExpect(jsonPath("errors[0]").value(mensagemError));
    }

    @Test
    @DisplayName("Deve obter informacoes de um livro")
    public void getBookDetailsTest() throws Exception {
        //cenario
        Long id = 1L;

        Book book = Book.builder()
                .id(id)
                .titulo(createNewBook().getTitulo())
                .autor(createNewBook().getAutor())
                .isbn(createNewBook().getIsbn())
                .build();

        BDDMockito.given(service.getById(id)).willReturn(Optional.of(book));

        //execucao
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("titulo").value(createNewBook().getTitulo()))
                .andExpect(jsonPath("autor").value(createNewBook().getAutor()))
                .andExpect(jsonPath("isbn").value(createNewBook().getIsbn()));
    }

    @Test
    @DisplayName("Deve retornar um erro quando livro informado nao existir.")
    public void bookNotFoundTest() throws Exception{
        //cenario
        BDDMockito.given( service.getById(anyLong()) ).willReturn(Optional.empty());

        //execucao
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("Deve deletar o livro informado.")
    public void deleteBookTest() throws Exception {

        BDDMockito
                .given(service.getById(anyLong()))
                .willReturn(Optional.of(Book.builder().id(1L).build()));

        MockHttpServletRequestBuilder delete =
                MockMvcRequestBuilders.delete(BOOK_API.concat("/" + 1));

        mvc.perform(delete).andExpect(status().isNoContent());

    }

    @Test
    @DisplayName("Deve retornar um erro ao tentar deletar um livro inexistente.")
    public void deleteBookNotFoundTest() throws Exception {

        BDDMockito
                .given(service.getById(anyLong()))
                .willReturn(Optional.empty());

        MockHttpServletRequestBuilder delete =
                MockMvcRequestBuilders.delete(BOOK_API.concat("/" + 1));

        mvc.perform(delete).andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("atualizar o livro informado.")
    public void updateBookTest() throws Exception {

        Long id = 1L;

        String json = new ObjectMapper().writeValueAsString(createNewBook());

        Book updatingBook = Book.builder().id(1l).titulo("outro titulo").build();
        BDDMockito.given(service.getById(id))
                .willReturn(Optional.of(updatingBook));
        Book updatedBook = Book.builder().id(id).autor("Wallas").titulo("As aventuras").isbn("123456789").build();
        BDDMockito.given(service.update(updatingBook)).willReturn(updatedBook);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + 1))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("titulo").value(createNewBook().getTitulo()))
                .andExpect(jsonPath("autor").value(createNewBook().getAutor()))
                .andExpect(jsonPath("isbn").value(createNewBook().getIsbn()));

    }

    @Test
    @DisplayName("Deve lançar um erro ao tentar atualizar o livro não existente informado informado.")
    public void updateBookNotFoundTest() throws Exception {
        String json = new ObjectMapper().writeValueAsString(createNewBook());

        BDDMockito.given(service.getById(Mockito.anyLong()))
                .willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + 1))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("Deve filtrar livros")
    public void findBooksTest() throws Exception {
        long id = 1l;

        Book book = Book.builder()
                .id(id).titulo(createNewBook().getTitulo())
                .titulo(createNewBook().getTitulo())
                .isbn(createNewBook().getIsbn())
                .autor(createNewBook().getAutor())
                .build();

        BDDMockito.given(service.find(Mockito.any(Book.class), Mockito.any(Pageable.class)) )
                .willReturn( new PageImpl<Book>( Arrays.asList(book), PageRequest.of(0,100), 1) );

        String queryString = String.format("?titulo=%s&autor=%s&page=0&size=100",
                book.getTitulo(),book.getAutor());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect( jsonPath("content", Matchers.hasSize(1)))
                .andExpect( jsonPath("totalElements").value(1) )
                .andExpect( jsonPath("pageable.pageSize").value(100) )
                .andExpect( jsonPath("pageable.pageNumber").value(0))
        ;
    }

}
