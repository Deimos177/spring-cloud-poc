package br.com.deimos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import br.com.deimos.dto.BookDTO;
import br.com.deimos.entity.Book;
import br.com.deimos.repository.BookRepository;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DataJpaTest(showSql = true)
@TestMethodOrder(OrderAnnotation.class)
public class BookServiceTest {

	@InjectMocks
	BookService service;
	@Mock
	BookRepository repository;
	@Mock
	private ArgumentCaptor<Book> bookArgumentCaptor;
	BookDTO request;
	Book requestBook = new Book();
	Book expectedSavedBook = new Book();

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("Create book test")
	@Order(1)
	public void when_save_user_it_should_return_user() {
		request = new BookDTO(null, "Test book", "Just some description",
				LocalDate.parse("25.07.2020", DateTimeFormatter.ofPattern("dd.MM.yyyy")));
		BeanUtils.copyProperties(request, expectedSavedBook);
		expectedSavedBook.setUUID("0477fbe0-be46-4ee2-9b1f-0b6cc82672c0");
		BeanUtils.copyProperties(request, requestBook);

		lenient().when(this.repository.save(requestBook)).thenReturn(expectedSavedBook);

		BookDTO created = (BookDTO) this.service.createBook(request).getBody();

		assertThat(created).isNotNull();
		verify(repository, times(1)).save(requestBook);
		assertThat(created.getName()).isNotEmpty();
		assertThat(created.getUUID()).isNotEmpty();
		assertThat(created.getReleaseDate().toString()).isNotEmpty();
		assertThat(created.getShortDescription()).isNotEmpty();

	}

	@Test
	@DisplayName("Return error due to large string test")
	@Order(2)
	public void should_return_error_due_to_too_large_string() {
		request = new BookDTO(null, "Test book",
				"quam nulla porttitor massa id neque aliquam vestibulum morbi blandit cursus risus at ultrices mi tempus imperdiet nullaa",
				LocalDate.parse("25-07-2020", DateTimeFormatter.ofPattern("dd-MM-yyyy")));
		BeanUtils.copyProperties(request, expectedSavedBook);
		expectedSavedBook.setUUID("0477fbe0-be46-4ee2-9b1f-0b6cc82672c1");

		lenient().when(this.repository.save(requestBook)).thenReturn(expectedSavedBook);

		ResponseEntity<Object> created = this.service.createBook(request);

		assertThat(created).isNotNull();
		assertThat(created.getStatusCode().is4xxClientError());
		assertThat(created.getBody()).isEqualTo("Error, description is too long");
	}

	@Test
	@DisplayName("Return error due to empty name")
	@Order(3)
	public void should_return_error_due_to_empty_name() {
		request = new BookDTO(null, "", "Just some description",
				LocalDate.parse("25-07-2020", DateTimeFormatter.ofPattern("dd-MM-yyyy")));
		BeanUtils.copyProperties(request, expectedSavedBook);
		expectedSavedBook.setUUID("0477fbe0-be46-4ee2-9b1f-0b6cc82672c1");

		lenient().when(this.repository.save(requestBook)).thenReturn(expectedSavedBook);

		ResponseEntity<Object> created = this.service.createBook(request);

		assertThat(created).isNotNull();
		assertThat(created.getStatusCode().is4xxClientError());
		assertThat(created.getBody()).isEqualTo("Empty name is not allowed");
	}

	@Test
	@DisplayName("Return error due to empty description")
	@Order(4)
	public void should_return_error_due_to_empty_description() {
		request = new BookDTO(null, "Test book", "",
				LocalDate.parse("25-07-2020", DateTimeFormatter.ofPattern("dd-MM-yyyy")));
		BeanUtils.copyProperties(request, expectedSavedBook);
		expectedSavedBook.setUUID("0477fbe0-be46-4ee2-9b1f-0b6cc82672c1");

		lenient().when(this.repository.save(requestBook)).thenReturn(expectedSavedBook);

		ResponseEntity<Object> created = this.service.createBook(request);

		assertThat(created).isNotNull();
		assertThat(created.getStatusCode().is4xxClientError());
		assertThat(created.getBody()).isEqualTo("Empty description is not allowed");
	}

	@Test
	@DisplayName("Return error due to empty release date")
	@Order(5)
	public void should_return_error_due_to_empty_release_date() {
		request = new BookDTO(null, "Test book", "Just some description",
				null);
		BeanUtils.copyProperties(request, expectedSavedBook);
		expectedSavedBook.setUUID("0477fbe0-be46-4ee2-9b1f-0b6cc82672c1");

		lenient().when(this.repository.save(requestBook)).thenReturn(expectedSavedBook);

		ResponseEntity<Object> created = this.service.createBook(request);

		assertThat(created).isNotNull();
		assertThat(created.getStatusCode().is4xxClientError());
		assertThat(created.getBody()).isEqualTo("Empty released date is not allowed");
	}
}