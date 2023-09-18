package jpabook.jpashop.repository.order.query;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

	private final EntityManager em;

	/**
	 * 컬렉션은 별도로 조회
	 * Query: 루트 1번, 컬렉션 N 번
	 * 단건 조회에서 많이 사용하는 방식
	 */
	public List<OrderQueryDto> findOrderQueryDtos() {
		List<OrderQueryDto> result = findOrders();

		result.forEach(o -> {
			List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());
			o.setOrderItems(orderItems);
		});
		return result;
	}

	/**
	 * 최적화
	 * Query: 루트 1번, 컬렉션 1번
	 * 데이터를 한꺼번에 처리할 때 많이 사용하는 방식
	 */
	public List<OrderQueryDto> findAllByDto_optimization() {
		List<OrderQueryDto> result = findOrders();

		Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(toOrderIds(result));

		result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));
		return result;
	}

	/**
	 * 1:N 관계(컬렉션)를 제외한 나머지를 한번에 조회
	 */
	private List<OrderQueryDto> findOrders() {
		return em.createQuery(
					 "select new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
						 " from Order o" +
						 " join o.member m" +
						 " join o.delivery d", OrderQueryDto.class)
				 .getResultList();

	}

	/**
	 * 1:N 관계인 orderItems 조회
	 */
	private List<OrderItemQueryDto> findOrderItems(Long orderId) {
		return em.createQuery(
					 "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
						 " from OrderItem oi" +
						 " join oi.item i" +
						 " where oi.order.id = :orderId", OrderItemQueryDto.class)
				 .setParameter("orderId", orderId)
				 .getResultList();

	}

	private List<Long> toOrderIds(List<OrderQueryDto> result) {
		return result.stream()
					 .map(OrderQueryDto::getOrderId)
					 .collect(Collectors.toList());
	}

	private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {
		// order_item과 item을 inner join 하고, in 쿼리가 나감
		List<OrderItemQueryDto> orderItems =
			em.createQuery(
				  "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
					  " from OrderItem oi" +
					  " join oi.item i" +
					  " where oi.order.id in :orderIds", OrderItemQueryDto.class)
			  .setParameter("orderIds", orderIds)
			  .getResultList();

		return orderItems.stream()
						 .collect(Collectors.groupingBy(OrderItemQueryDto::getOrderId));
	}

}
