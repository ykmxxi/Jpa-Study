package jpabook.jpashop.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderSearch;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * XToOne(ManyToOne, OneToOne) 성능 최적화
 * Order
 * Order -> Member: 다대일 관계(ManyToOne)
 * Order -> Delivery: 일대일 관계(OneToOne)
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

	private final OrderRepository orderRepository;

	/**
	 * V1. 엔티티 직접 노출
	 * - Hibernate5Module 모듈 등록, LAZY = null 처리
	 * - 양방향 관계 문제 발생 -> @JsonIgnore
	 */
	@GetMapping("/api/v1/simple-orders")
	public List<Order> ordersV1() {
		List<Order> all = orderRepository.findAllByString(new OrderSearch());
		// 하이버네이트 모듈을 사용하지 않으려면 강제로 LAZY 로딩을 하면 됨
		for (Order order : all) {
			order.getMember().getName(); // LAZY 강제 초기화
			order.getDelivery().getAddress(); // LAZY 강제 초기화
		}
		return all;
	}

	/**
	 * V2. 엔티티를 조회해서 DTO로 변환(fetch join 사용X)
	 * - 단점: 지연로딩으로 쿼리 N번 호출(성능 문제)
	 * - 주문이 2개 이면
	 * 	- SQL 1번: 주문 조회(2개)
	 * 	- 1번째 주문: 회원 조회 + 배송 조회 -> SQL 2번
	 * 	- 2번째 주문: 회원 조회 + 배송 조회 -> SQL 2번
	 * 	- 총 5번의 쿼리가 나감
	 */
	@GetMapping("/api/v2/simple-orders")
	public List<SimpleOrderDto> ordersV2() {
		List<Order> orders = orderRepository.findAllByString(new OrderSearch());
		return orders.stream()
					 .map(SimpleOrderDto::new)
					 .collect(Collectors.toList());
	}

	@Data
	static class SimpleOrderDto {

		private Long orderId;
		private String name;
		private LocalDateTime orderDate;
		private OrderStatus orderStatus;
		private Address address;

		public SimpleOrderDto(Order order) {
			orderId = order.getId();
			name = order.getMember().getName(); // LAZY 초기화
			orderDate = order.getOrderDate();
			orderStatus = order.getStatus();
			address = order.getDelivery().getAddress(); // LAZY 초기화
		}

	}

}
