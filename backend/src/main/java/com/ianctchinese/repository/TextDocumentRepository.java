package com.ianctchinese.repository;

import com.ianctchinese.model.TextDocument;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TextDocumentRepository extends JpaRepository<TextDocument, Long> {

  List<TextDocument> findByCategory(String category);

  @Query("SELECT t FROM TextDocument t WHERE " +
      "LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
      "LOWER(t.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
      "LOWER(t.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
  List<TextDocument> searchByKeyword(@Param("keyword") String keyword);
}
