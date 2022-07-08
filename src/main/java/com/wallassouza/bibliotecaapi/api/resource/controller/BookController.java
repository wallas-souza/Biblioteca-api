package com.wallassouza.bibliotecaapi.api.resource.controller;

import com.wallassouza.bibliotecaapi.api.resource.dto.bookdto.BookDTO;
import com.wallassouza.bibliotecaapi.api.resource.exception.ApiErros;
import com.wallassouza.bibliotecaapi.api.resource.exception.BusinessException;
import com.wallassouza.bibliotecaapi.api.resource.model.Book;
import com.wallassouza.bibliotecaapi.api.resource.service.BookService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/livros")
public class BookController {

    private BookService bookService;
    public ModelMapper modelMapper;

    public BookController (BookService service, ModelMapper mapper){
        this.bookService = service;
        this.modelMapper = mapper;
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookDTO create(@RequestBody @Valid BookDTO dto){
        Book entidade = modelMapper.map(dto, Book.class);
        entidade =  bookService.save(entidade);
        return modelMapper.map(entidade, BookDTO.class);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErros handleValidationExceptions(MethodArgumentNotValidException ex){
        BindingResult bindingResult = ex.getBindingResult();
        return  new ApiErros(bindingResult);
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErros handleBusinessException(BusinessException ex){
        return new ApiErros(ex);
    }
}
