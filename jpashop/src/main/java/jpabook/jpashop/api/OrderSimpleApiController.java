package jpabook.jpashop.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderSearch;
import jpabook.jpashop.repository.OrderRepository;
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

}
