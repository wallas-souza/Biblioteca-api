package com.wallassouza.bibliotecaapi.api.resource.service;

import com.wallassouza.bibliotecaapi.api.resource.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BookService {
    Book save(Book any);

    Optional<Book> getById(Long id);

    void delete(Book book);

    Book update(Book book);

    Page<Book> find (Book filter, Pageable pageRequest);
}
