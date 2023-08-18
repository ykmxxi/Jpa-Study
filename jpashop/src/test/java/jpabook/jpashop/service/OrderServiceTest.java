package jpabook.jpashop.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
class OrderServiceTest {

	@Autowired OrderService orderService;
	@Autowired OrderRepository orderRepository;
	@Autowired EntityManager em;

	@Test
	@DisplayName("상품 주문 성공 테스트")
	void itemOrderSuccess() {
		// given
		Member member = createMember();
		Item book = createBook("JPA", 10000, 10);

		// when
		int orderCount = 2;
		Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

		// then
		Order getOrder = orderRepository.findOne(orderId);
		assertEquals(OrderStatus.ORDER, getOrder.getStatus(), "상품 주문시 상태는 ORDER");
		assertEquals(1, getOrder.getOrderItems().size(), "주문한 상품 종류 수가 정확해야 한다");
		assertEquals(10000 * orderCount, getOrder.getTotalPrice(), "주문 가격은 가격 * 수량이다");
		assertEquals(8, book.getStockQuantity(), "주문 수량만큼 재고가 줄어야 한다");
	}

	@Test
	@DisplayName("상품 주문 재고 수량 초과 테스트: NotEnoughStockException 발생")
	void itemStockOver() {
		// given
		Member member = createMember();
		Item book = createBook("JPA", 10000, 10);

		int orderCount = 11;

		// when & then
		assertThatThrownBy(() -> orderService.order(member.getId(), book.getId(), orderCount))
			.isInstanceOf(NotEnoughStockException.class);
	}

	@Test
	@DisplayName("상품 주문 취소 성공 테스트")
	void itemOrderCancel() {
		// given
		Member member = createMember();
		Item book = createBook("JPA", 10000, 10);

		int orderCount = 2;
		Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

		// when
		orderService.cancelOrder(orderId);

		// then
		Order getOrder = orderRepository.findOne(orderId);
		assertEquals(OrderStatus.CANCEL, getOrder.getStatus(), "주문 취소시 상태는 CANCEL");
		assertEquals(10, book.getStockQuantity(), "주문이 취소된 상품은 그만큼 재고가 증가해야 한다");
	}

	private Member createMember() {
		Member member = new Member();
		member.setName("회원1");
		member.setAddress(new Address("서울", "강가", "123-123"));
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
