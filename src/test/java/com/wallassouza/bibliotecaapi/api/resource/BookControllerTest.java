package com.wallassouza.bibliotecaapi.api.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallassouza.bibliotecaapi.api.resource.dto.bookdto.BookDTO;
import com.wallassouza.bibliotecaapi.api.resource.exception.BusinessException;
import com.wallassouza.bibliotecaapi.api.resource.model.Book;
import com.wallassouza.bibliotecaapi.api.resource.service.BookService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest
@AutoConfigureMockMvc
public class BookControllerTest {

    static String BOOK_API = "/api/livros";

    @Autowired
    MockMvc mvc;

    @MockBean
    BookService service;

    @Test
    @DisplayName("Deve criar um livro com sucesso")
    public void createBookTest() throws Exception {

        BookDTO dto = createNewBook();
        Book savedBook = Book.builder().id(10).autor("Wallas").titulo("As aventuras").isbn("123456789").build();

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

}
