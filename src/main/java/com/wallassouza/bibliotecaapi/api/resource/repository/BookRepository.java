package com.wallassouza.bibliotecaapi.api.resource.repository;

import com.wallassouza.bibliotecaapi.api.resource.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRepository extends JpaRepository <Book, Long> {
    boolean existsByIsbn(String isbn);

    Optional<Book> findByIsbn(String isbn);
}
