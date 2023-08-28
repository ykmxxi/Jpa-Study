package hellojpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Member {

	@Id @GeneratedValue
	@Column(name = "MEMBER_ID")
	private Long id;

	@Column(name = "USERNAME")
	private String name;

	// @Column(name = "TEAM_ID")
	// private Long teamId;

	@ManyToOne
	@JoinColumn(name = "TEAM_ID")
	private Team team;

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Team getTeam() {
		return team;
	}

	/**
	 * 연관관계 편의 메서드
	 */
	public void changeTeam(Team team) {
		this.team = team;

		// 역방향(주인이 아닌 방향) 연관관계 설정
		// 객체지향 관점에서도 양쪽 다 값을 넣어줘야 좋음
		team.getMembers().add(this);
	}

}
