package study.querydsl;

import static org.assertj.core.api.Assertions.assertThat;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQueryFactory;

import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

	@Autowired private EntityManager em;
	private JPAQueryFactory queryFactory; // 필드로 가져올 수도 있음

	@BeforeEach
	void init() {
		queryFactory = new JPAQueryFactory(em);

		Team teamA = new Team("teamA");
		Team teamB = new Team("teamB");
		em.persist(teamA);
		em.persist(teamB);

		Member member1 = new Member("member1", 10, teamA);
		Member member2 = new Member("member2", 20, teamA);
		Member member3 = new Member("member3", 30, teamB);
		Member member4 = new Member("member4", 40, teamB);
		em.persist(member1);
		em.persist(member2);
		em.persist(member3);
		em.persist(member4);
	}

	@Test
	void startJPQL() {
		// given
		String query = "select m from Member m where m.username = :username";
		Member findMember = em.createQuery(query, Member.class)
							  .setParameter("username", "member1")
							  .getSingleResult();

		assertThat(findMember.getUsername()).isEqualTo("member1");
	}

	@Test
	void startQuerydsl() {
		// 생성자의 파라미터 문자열은는 어떤 Q-Type 인지 구분하는 용도
		QMember m = new QMember("m");

		Member findMember = queryFactory.select(m)
										.from(m)
										.where(m.username.eq("member1"))
										.fetchOne();

		assertThat(findMember.getUsername()).isEqualTo("member1");
	}

}
