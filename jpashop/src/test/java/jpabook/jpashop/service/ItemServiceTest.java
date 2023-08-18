package jpabook.jpashop.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
class ItemServiceTest {

	@Autowired ItemService itemService;
	@Autowired ItemRepository itemRepository;

	@Test
	@DisplayName("상품 저장 성공 테스트")
	void save() {
		// given
		Item item = new Book();
		item.setName("JPA");
		item.setStockQuantity(10);

		// when
		itemService.saveItem(item);

		// then
		assertEquals(item, itemRepository.findOne(1L));
	}

}
