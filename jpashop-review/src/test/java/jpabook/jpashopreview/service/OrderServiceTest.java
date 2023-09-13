package jpabook.jpashopreview.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import jpabook.jpashopreview.domain.Address;
import jpabook.jpashopreview.domain.Member;
import jpabook.jpashopreview.domain.Order;
import jpabook.jpashopreview.domain.OrderStatus;
import jpabook.jpashopreview.domain.item.Book;
import jpabook.jpashopreview.domain.item.Item;
import jpabook.jpashopreview.exception.NotEnoughStockException;
import jpabook.jpashopreview.repository.OrderRepository;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
class OrderServiceTest {

	@Autowired EntityManager em;
	@Autowired private OrderService orderService;
	@Autowired private OrderRepository orderRepository;

	@Test
	void 상품주문() {
		// given
		Member member = createMember();

		Item item = createBook("JPA", 10000, 10);

		// when
		int orderCount = 2;
		Long orderId = orderService.order(member.getId(), item.getId(), orderCount);

		// then
		Order findOrder = orderRepository.findOne(orderId);
		assertEquals(OrderStatus.ORDER, findOrder.getStatus(), "상품 주문시 상태는 ORDER");
		assertEquals(1, findOrder.getOrderItems().size(), "주문한 상품 종류 수가 정확해야 한다.");
		assertEquals(10000 * orderCount, findOrder.getTotalPrice(), "주문 가격은 가격 * 수량 이다.");
		assertEquals(8, item.getStockQuantity(), "주문 수량만큼 재고가 줄어야 한다.");

	}

	@Test
	void 상품주문_재고수량초과() {
		// given
		Member member = createMember();
		Item item = createBook("JPA", 10000, 10);

		// when
		int orderCount = 11;

		// then
		assertThatThrownBy(() -> orderService.order(member.getId(), item.getId(), orderCount))
			.isInstanceOf(NotEnoughStockException.class)
			.hasMessage("need more stock");
	}

	@Test
	void 주문취소() {
		// given
		Member member = createMember();
		Item item = createBook("JPA", 10000, 10);

		int orderCount = 2;
		Long orderId = orderService.order(member.getId(), item.getId(), orderCount);

		// when
		orderService.cancelOrder(orderId);

		// then
		Order findOrder = orderRepository.findOne(orderId);
		assertThat(findOrder.getStatus()).isEqualTo(OrderStatus.CANCEL);
		assertThat(item.getStockQuantity()).isEqualTo(10);
	}

	private Member createMember() {
		Member member = new Member();
		member.setName("회원1");
		member.setAddress(new Address("서울", "거리", "123-123"));
		em.persist(member);
		return member;
	}

	private Item createBook(String name, int price, int stockQuantity) {
		Item book = new Book();
		book.setName(name);
		book.setPrice(price);
		book.setStockQuantity(stockQuantity);
		em.persist(book);
		return book;
	}

}
