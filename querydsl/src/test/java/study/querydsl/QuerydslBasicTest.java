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
import com.querydsl.core.Tuple;
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

	/**
	 * 페이징: 조회 건수 제한
	 */
	@Test
	void paging1() {
		List<Member> result = queryFactory.selectFrom(member)
										  .orderBy(member.username.desc())
										  .offset(1) // 앞에 몇 개를 skip, 0부터 시작 -> 1개 생략
										  .limit(2) // 2개만 가져오기
										  .fetch();

		assertThat(result).hasSize(2);
	}

	/**
	 * 페이징: 전체 조회 수(total count)가 필요하면?
	 */
	@Test
	void paging2() {
		QueryResults<Member> queryResults = queryFactory.selectFrom(member)
														.orderBy(member.username.desc())
														.offset(1)
														.limit(2)
														.fetchResults();

		assertThat(queryResults.getTotal()).isEqualTo(4);
		assertThat(queryResults.getLimit()).isEqualTo(2);
		assertThat(queryResults.getOffset()).isEqualTo(1);
		assertThat(queryResults.getResults()).hasSize(2);
	}

	/**
	 * JPQL
	 * select
	 * COUNT(m), // 회원수
	 * SUM(m.age), // 나이 합
	 * AVG(m.age), // 평균 나이
	 * MAX(m.age), // 최대 나이
	 * MIN(m.age) // 최소 나이
	 * from Member m
	 */
	@Test
	void aggregation() {
		List<Tuple> result = queryFactory.select(
											 member.count(),
											 member.age.sum(),
											 member.age.avg(),
											 member.age.max(),
											 member.age.min()
										 )
										 .from(member)
										 .fetch();

		Tuple tuple = result.get(0);

		assertThat(tuple.get(member.count())).isEqualTo(4);
		assertThat(tuple.get(member.age.sum())).isEqualTo(100);
		assertThat(tuple.get(member.age.avg())).isEqualTo(25);
		assertThat(tuple.get(member.age.max())).isEqualTo(40);
		assertThat(tuple.get(member.age.min())).isEqualTo(10);
	}

}
