package jpabook.jpashop;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import jpabook.jpashop.domain.item.Book;

/**
 * 변경 감지(dirty checking)와 병합(merge)의 차이를 알아보기 위한 테스트
 */

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
@Rollback(value = false)
class ItemUpdateTest {

	@Autowired EntityManager em;

	@BeforeEach
	void init() {
		Book book = new Book();
		book.setName("originName");
		book.setIsbn("1111");
		em.persist(book);
	}

	@Test
	@DisplayName("병합(merge): 엔티티의 모든 값을 준영속 엔티티의 값으로 변경")
	void mergeTest() {
		// given
		Book updatedBook = new Book();
		updatedBook.setId(1L);
		updatedBook.setName("updateName");

		// when
		em.merge(updatedBook);

		// then
		Book result = em.find(Book.class, 1L);
		assertThat(result.getName()).isEqualTo("updateName");
		assertThat(result.getIsbn()).isNull();
	}

	@Test
	@DisplayName("변경 감지(dirty checking): 원하는 필드만 변경 가능")
	void dirtyCheckingTest() {
		// given
		Book findBook = em.find(Book.class, 1L);

		// when
		findBook.setName("updateName");
		findBook.setAuthor("kim");

		// then
		Book result = em.find(Book.class, 1L);
		assertThat(result.getName()).isEqualTo("updateName");
		assertThat(result.getIsbn()).isEqualTo("1111");
		assertThat(result.getAuthor()).isEqualTo("kim");
	}

}
