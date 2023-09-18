package jpabook.jpashop.repository.order.query;

import java.time.LocalDateTime;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.OrderStatus;
import lombok.Data;

/**
 * Order와 OrderItem을 조인해 한번에 가져옴
 */
@Data
public class OrderFlatDto {

	private Long orderId; // 주문 id
	private String name; // 회원 이름
	private LocalDateTime orderDate; // 주문 시간
	private OrderStatus orderStatus; // 주문 상태
	private Address address; // 배송 주소

	private String itemName; // 주문 상품 이름
	private int orderPrice; // 주문 상품 가격
	private int count; // 주문 상품 갯수

	public OrderFlatDto(Long orderId, String name, LocalDateTime orderDate, OrderStatus orderStatus, Address address,
						String itemName, int orderPrice, int count) {
		this.orderId = orderId;
		this.name = name;
		this.orderDate = orderDate;
		this.orderStatus = orderStatus;
		this.address = address;
		this.itemName = itemName;
		this.orderPrice = orderPrice;
		this.count = count;
	}

}
