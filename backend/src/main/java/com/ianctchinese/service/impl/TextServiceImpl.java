package com.ianctchinese.service.impl;

import com.ianctchinese.dto.TextUploadRequest;
import com.ianctchinese.model.TextDocument;
import com.ianctchinese.repository.TextDocumentRepository;
import com.ianctchinese.service.TextService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TextServiceImpl implements TextService {

  private final TextDocumentRepository textDocumentRepository;

  @Override
  @Transactional
  public TextDocument createText(TextUploadRequest request) {
    String resolvedCategory = Optional.ofNullable(request.getCategory())
        .filter(value -> !value.isBlank())
        .orElse("unknown");
    TextDocument textDocument = TextDocument.builder()
        .title(request.getTitle())
        .content(request.getContent())
        .category(resolvedCategory)
        .author(request.getAuthor())
        .era(request.getEra())
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
    return textDocumentRepository.save(textDocument);
  }

  @Override
  public List<TextDocument> listTexts(String category) {
    if (category == null || category.isBlank()) {
      return textDocumentRepository.findAll();
    }
    return textDocumentRepository.findByCategory(category);
  }

  @Override
  public TextDocument getText(Long id) {
    return textDocumentRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Text not found: " + id));
  }

  @Override
  @Transactional
  public TextDocument updateCategory(Long id, String category) {
    TextDocument document = getText(id);
    document.setCategory(category);
    document.setUpdatedAt(LocalDateTime.now());
    return textDocumentRepository.save(document);
  }

  @Override
  public List<TextDocument> searchTexts(String keyword) {
    if (keyword == null || keyword.isBlank()) {
      return List.of();
    }
    return textDocumentRepository.searchByKeyword(keyword.trim());
  }
}
