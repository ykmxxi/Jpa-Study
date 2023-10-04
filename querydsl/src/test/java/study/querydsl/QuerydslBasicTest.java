package study.querydsl;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;

import java.util.List;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.QueryResults;
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

	@Test
	void startQuerydsl2() {
		// 별칭 직접 지정
		QMember qMember1 = new QMember("m");

		// 기본 인스턴스 사용
		QMember qMember2 = member;

		// 기본 인스턴스 static import 사용
		Member findMember = queryFactory.select(member)
										.from(member)
										.where(member.username.eq("member1"))
										.fetchOne();

		assertThat(findMember.getUsername()).isEqualTo("member1");
	}

	@Test
	void search() {
		Member findMember = queryFactory
			.selectFrom(member)
			.where(member.username.eq("member1")
								  .and(member.age.between(10, 30)))
			.fetchOne();

		assertThat(findMember.getUsername()).isEqualTo("member1");
	}

	@Test
	void searchAndParam() {
		Member findMember = queryFactory
			.selectFrom(member)
			.where(
				member.username.eq("member1"),
				member.age.eq(10))
			.fetchOne();

		assertThat(findMember.getUsername()).isEqualTo("member1");
	}

	@Test
	void resultFetch() {
		// 리스트
		List<Member> fetch = queryFactory.selectFrom(member)
										 .fetch();

		// 단 건
		Member fetchOne = queryFactory.selectFrom(member)
									  .fetchOne();

		// 처음 한 건 조회: limit(1) + fetchOne
		Member fetchFirst = queryFactory.selectFrom(member)
										.fetchFirst();

		// fetchResults(): 현재 deprecated
		// 페이징에서 사용
		QueryResults<Member> fetchResults = queryFactory.selectFrom(member)
														.fetchResults();

		// fetchCount():
		// count 쿼리로 변경: 현재 deprecated
		long count = queryFactory.selectFrom(member)
								 .fetchCount();
	}

	/**
	 * 회원 정렬 순서
	 * 1. 회원 나이 내림차순(desc)
	 * 2. 회원 이름 올림차순(asc)
	 * - 단, 2에서 회원 이름이 없으면 마지막에 출력(nullsLast)
	 */
	@Test
	void sort() {
		em.persist(new Member(null, 100));
		em.persist(new Member("member5", 100));
		em.persist(new Member("member6", 100));

		List<Member> result = queryFactory.selectFrom(member)
										  .where(member.age.eq(100))
										  .orderBy(member.age.desc(), member.username.asc().nullsLast())
										  .fetch();

		Member member5 = result.get(0);
		Member member6 = result.get(1);
		Member memberNull = result.get(2);

		assertThat(member5.getUsername()).isEqualTo("member5");
		assertThat(member6.getUsername()).isEqualTo("member6");
		assertThat(memberNull.getUsername()).isNull();
	}

}
