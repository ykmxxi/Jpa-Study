package hellojpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Member {

	@Id
	private Long id;

	// 제약조건 추가: 회원 이름은 필수이고 10자를 초과하면 안됨
	@Column(nullable = false, length = 10)
	private String name;

	// JPA 스펙 상 기본 생성자가 필요
	public Member() {
	}

	public Member(Long id, String name) {
		this.id = id;
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
