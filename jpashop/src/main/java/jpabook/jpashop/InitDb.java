package jpabook.jpashop;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Delivery;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.item.Book;
import lombok.RequiredArgsConstructor;

/**
 * 샘플 데이터: 총 주문 2개
 * userA
 * * JPA1 BOOK
 * * JPA2 BOOK
 * <p>
 * userB
 * * SPRING1 BOOK
 * * SPRING2 BOOK
 */
@Component
@RequiredArgsConstructor
public class InitDb {

	private final InitService initService;

	@PostConstruct
	public void init() {
		initService.dbInit1();
		initService.dbInit2();
	}

	@Component
	@Transactional
	@RequiredArgsConstructor
	static class InitService {

		private final EntityManager em;

		public void dbInit1() {
			Member member = createMember("userA", "서울", "거리", "111");
			em.persist(member);

			Book book1 = createBook("JPA1 BOOK", 10000, 50);
			em.persist(book1);

			Book book2 = createBook("JPA2 BOOK", 20000, 100);
			em.persist(book2);

			OrderItem orderItem1 = OrderItem.createOrderItem(book1, 10000, 1);
			OrderItem orderItem2 = OrderItem.createOrderItem(book2, 20000, 2);

			Delivery delivery = createDelivery(member);
			Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
			em.persist(order);
		}

		public void dbInit2() {
			Member member = createMember("userB", "부산", "거리", "222");
			em.persist(member);

			Book book1 = createBook("SPRING1 BOOK", 20000, 200);
			em.persist(book1);

			Book book2 = createBook("SPRING2 BOOK", 40000, 300);
			em.persist(book2);

			OrderItem orderItem1 = OrderItem.createOrderItem(book1, 20000, 3);
			OrderItem orderItem2 = OrderItem.createOrderItem(book2, 40000, 4);

			Delivery delivery = createDelivery(member);
			Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
			em.persist(order);
		}

		private static Member createMember(String name, String city, String street, String zipcode) {
			Member member = new Member();
			member.setName(name);
			member.setAddress(new Address(city, street, zipcode));
			return member;
		}

		private static Book createBook(String name, int price, int stockQuantity) {
			Book book2 = new Book();
			book2.setName(name);
			book2.setPrice(price);
			book2.setStockQuantity(stockQuantity);
			return book2;
		}

		private static Delivery createDelivery(Member member) {
			Delivery delivery = new Delivery();
			delivery.setAddress(member.getAddress());
			return delivery;
		}

	}

}
