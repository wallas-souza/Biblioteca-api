package com.wallassouza.bibliotecaapi.api.resource.controller;

import com.wallassouza.bibliotecaapi.api.resource.dto.bookdto.BookDTO;
import com.wallassouza.bibliotecaapi.api.resource.exception.ApiErros;
import com.wallassouza.bibliotecaapi.api.resource.exception.BusinessException;
import com.wallassouza.bibliotecaapi.api.resource.model.Book;
import com.wallassouza.bibliotecaapi.api.resource.service.BookService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/livros")
public class BookController {

    private BookService bookService;
    public ModelMapper modelMapper;

    public BookController(BookService service, ModelMapper mapper) {
        this.bookService = service;
        this.modelMapper = mapper;
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookDTO create(@RequestBody @Valid BookDTO dto) {
        Book entidade = modelMapper.map(dto, Book.class);
        entidade = bookService.save(entidade);
        return modelMapper.map(entidade, BookDTO.class);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErros handleValidationExceptions(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        return new ApiErros(bindingResult);
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErros handleBusinessException(BusinessException ex) {
        return new ApiErros(ex);
    }

    @GetMapping("{id}")
    public BookDTO get(@PathVariable Long id) {
        return bookService.getById(id)
                .map(book -> modelMapper.map(book, BookDTO.class))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Book bookId = bookService.getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        bookService.delete(bookId);
    }

    @PutMapping("{id}")
    public BookDTO update(@PathVariable Long id, BookDTO dto) {
        return bookService.getById(id).map(book -> {

            book.setAutor(dto.getAutor());
            book.setTitulo(dto.getTitulo());
            book = bookService.update(book);
            return modelMapper.map(book, BookDTO.class);

        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public Page<BookDTO> find(BookDTO bookDTO, Pageable pageable){
        Book filter = modelMapper.map(bookDTO, Book.class);
        Page<Book> resultado = bookService.find(filter, pageable);
        List<BookDTO> list = resultado.getContent()
                .stream()
                .map(entidade -> modelMapper.map(entidade, BookDTO.class))
                .collect(Collectors.toList());
        return new PageImpl<BookDTO>( list, pageable, resultado.getTotalElements());
    }
}
