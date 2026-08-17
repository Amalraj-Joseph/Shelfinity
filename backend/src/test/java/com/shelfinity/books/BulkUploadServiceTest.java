/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.books;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shelfinity.books.BulkUploadService.BulkUploadResult;

@ExtendWith(MockitoExtension.class)
class BulkUploadServiceTest {

    @Mock private BookRepository bookRepository;

    @InjectMocks
    private BulkUploadService bulkUploadService;

    private static InputStream csv(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void uploadFromCSV_skipsHeaderRow() {
        when(bookRepository.findByIsbn(org.mockito.ArgumentMatchers.anyString())).thenReturn(Optional.empty());

        BulkUploadResult result = bulkUploadService.uploadFromCSV(
                csv("title,author,isbn,description,totalCopies\nClean Code,Robert Martin,978-1,A book,3\n"));

        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getErrorCount()).isZero();
        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("Clean Code");
        assertThat(captor.getValue().getTotalCopies()).isEqualTo(3);
    }

    @Test
    void uploadFromCSV_handlesQuotedFieldsContainingCommas() {
        when(bookRepository.findByIsbn(org.mockito.ArgumentMatchers.anyString())).thenReturn(Optional.empty());

        BulkUploadResult result = bulkUploadService.uploadFromCSV(
                csv("title,author,isbn,description,totalCopies\n"
                        + "\"Clean Code, 1st Edition\",Robert Martin,978-1,\"A book, about code\",2\n"));

        assertThat(result.getSuccessCount()).isEqualTo(1);
        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("Clean Code, 1st Edition");
        assertThat(captor.getValue().getDescription()).isEqualTo("A book, about code");
    }

    @Test
    void uploadFromCSV_missingTitle_recordsErrorAndSkipsRow() {
        BulkUploadResult result = bulkUploadService.uploadFromCSV(
                csv("title,author,isbn,description,totalCopies\n,Robert Martin,978-1,desc,1\n"));

        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getErrorCount()).isEqualTo(1);
        assertThat(result.getErrorMessages().get(0)).contains("Title cannot be empty");
        verify(bookRepository, never()).save(any());
    }

    @Test
    void uploadFromCSV_missingAuthor_recordsError() {
        BulkUploadResult result = bulkUploadService.uploadFromCSV(
                csv("title,author\nSome Title,\n"));

        assertThat(result.getErrorCount()).isEqualTo(1);
        assertThat(result.getErrorMessages().get(0)).contains("Author cannot be empty");
    }

    @Test
    void uploadFromCSV_invalidTotalCopies_defaultsToOne() {
        when(bookRepository.findByIsbn(org.mockito.ArgumentMatchers.anyString())).thenReturn(Optional.empty());

        bulkUploadService.uploadFromCSV(csv("title,author,isbn,description,totalCopies\nTitle,Author,978-1,desc,not-a-number\n"));

        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(captor.capture());
        assertThat(captor.getValue().getTotalCopies()).isEqualTo(1);
    }

    @Test
    void uploadFromCSV_duplicateIsbn_recordsErrorAndSkipsSave() {
        when(bookRepository.findByIsbn("978-1"))
                .thenReturn(Optional.of(new Book("Existing", "Author")));

        BulkUploadResult result = bulkUploadService.uploadFromCSV(
                csv("title,author,isbn\nTitle,Author,978-1\n"));

        assertThat(result.getErrorCount()).isEqualTo(1);
        assertThat(result.getErrorMessages().get(0)).contains("already exists");
        verify(bookRepository, never()).save(any());
    }

    @Test
    void uploadFromCSV_skipsBlankLines() {
        when(bookRepository.findByIsbn(org.mockito.ArgumentMatchers.anyString())).thenReturn(Optional.empty());

        BulkUploadResult result = bulkUploadService.uploadFromCSV(
                csv("title,author,isbn\nTitle,Author,978-1\n\n\n"));

        assertThat(result.getTotalProcessed()).isEqualTo(1);
    }

    @Test
    void generateCSVTemplate_containsExpectedHeader() {
        String template = bulkUploadService.generateCSVTemplate();

        assertThat(template).startsWith("title,author,isbn,description,totalCopies");
    }
}
