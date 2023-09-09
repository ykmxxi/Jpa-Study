package jpabook.jpashopreview.domain.item;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;

import jpabook.jpashopreview.domain.Category;
import jpabook.jpashopreview.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@Getter @Setter
public abstract class Item {

	@Id @GeneratedValue
	@Column(name = "item_id")
	private Long id;

	private String name;
	private int price;
	private int stockQuantity;

	@ManyToMany(mappedBy = "items")
	private List<Category> categories = new ArrayList<>();

	//== 비즈니스 로직 ==//
	// DDD에서 엔티티 자체가 해결할 수 있는 것은 엔티티 안에 비즈니스 로직을 넣는 것이 좋음
	// 데이터를 가지고 있는 쪽에서 비즈니스 로직을 갖고 있는 것이 좋음

	/**
	 * stock 증가
	 */
	public void addStock(int quantity) {
		this.stockQuantity += quantity;
	}

	/**
	 * stock 감소
	 */
	public void removeStock(int quantity) {
		int remainStockQuantity = this.stockQuantity - quantity;
		if (remainStockQuantity < 0) {
			throw new NotEnoughStockException("need more stock");
		}
		this.stockQuantity = remainStockQuantity;
	}

}
