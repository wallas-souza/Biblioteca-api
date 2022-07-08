package com.wallassouza.bibliotecaapi.api.resource.service;

import com.wallassouza.bibliotecaapi.api.resource.exception.BusinessException;
import com.wallassouza.bibliotecaapi.api.resource.model.Book;
import com.wallassouza.bibliotecaapi.api.resource.repository.BookRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class BookServiceImpl implements BookService {

    private BookRepository repository;

    @Override
    public Book save(Book book) {
        if(repository.existsByIsbn(book.getIsbn())){
            throw new BusinessException("ISBN já cadastrado");
        }
        return repository.save(book);
    }
}
