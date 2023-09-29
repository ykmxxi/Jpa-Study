package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import study.datajpa.entity.Item;

@SpringBootTest
class ItemRepositoryTest {

	@Autowired private ItemRepository itemRepository;

	@Test
	void test() {
		Item item = new Item("A");
		itemRepository.save(item); // isNew()로 확인 후 persist() 호출
	}

}
