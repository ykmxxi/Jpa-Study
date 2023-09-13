package jpabook.jpashopreview.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import jpabook.jpashopreview.domain.item.Book;
import jpabook.jpashopreview.domain.item.Item;
import jpabook.jpashopreview.exception.NotEnoughStockException;
import jpabook.jpashopreview.repository.ItemRepository;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
class ItemServiceTest {

	@Autowired private ItemRepository itemRepository;
	@Autowired private ItemService itemService;
	@Autowired private EntityManager em;

	@Test
	void 상품_저장() {
		// given
		Item item = new Book();
		item.setName("JPA");
		item.setStockQuantity(100);

		// when
		itemService.saveItem(item);
		Item findItem = em.find(Item.class, item.getId());

		// then
		assertEquals(item, findItem);
	}

	@Test
	void 상품_재고_부족() {
		// given
		Item item = new Book();
		item.setName("JPA");
		item.setStockQuantity(10);

		// when & then
		assertThatThrownBy(() -> item.removeStock(15))
			.isInstanceOf(NotEnoughStockException.class)
			.hasMessage("need more stock");
	}

}
