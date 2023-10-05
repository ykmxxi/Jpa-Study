package study.querydsl;

import static com.querydsl.jpa.JPAExpressions.select;
import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
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

	/**
	 * 팀의 이름과 각 팀의 평균 연령을 구하기
	 */
	@Test
	void group() {
		List<Tuple> result = queryFactory.select(team.name, member.age.avg())
										 .from(member)
										 .join(member.team, team)
										 .groupBy(team.name)
										 .having(member.age.avg().gt(10))
										 .fetch();

		Tuple teamA = result.get(0);
		Tuple teamB = result.get(1);

		assertThat(teamA.get(team.name)).isEqualTo("teamA");
		assertThat(teamA.get(member.age.avg())).isEqualTo(15);

		assertThat(teamB.get(team.name)).isEqualTo("teamB");
		assertThat(teamB.get(member.age.avg())).isEqualTo(35);
	}

	/**
	 * 기본 조인(내부 조인)
	 * - teamA에 소속된 모든 회원 조회
	 * - join(), innerJoin()
	 */
	@Test
	void join() {
		List<Member> result = queryFactory.selectFrom(member)
										  .join(member.team, team)
//										  .innerJoin(member.team, team)
										  .where(team.name.eq("teamA"))
										  .fetch();
		assertThat(result).extracting("username")
						  .containsExactly("member1", "member2");
	}

	/**
	 * 세타 조인
	 * - 회원의 이름이 팀 이름과 같은 회원 조회
	 * - 모든 회원과, 모든 팀을 가져와 조인을 진행
	 */
	@Test
	void theta_join() {
		em.persist(new Member("teamA"));
		em.persist(new Member("teamB"));

		List<Member> result = queryFactory.selectFrom(member)
										  .from(member, team)
										  .where(member.username.eq(team.name))
										  .fetch();

		assertThat(result).extracting("username")
						  .containsExactly("teamA", "teamB");
	}

	/**
	 * 조인 대상 필터링
	 * ex: 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
	 * - JPQL: select m, t from Member m left join m.team t on t.name = 'teamA'
	 * - SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.TEAM_ID=t.id and t.name='teamA'
	 */
	@Test
	void join_on_filtering() {
		List<Tuple> result = queryFactory.select(member, team)
										 .from(member)
										 .leftJoin(member.team, team).on(team.name.eq("teamA"))
										 .fetch();

		for (Tuple tuple : result) {
			System.out.println("tuple = " + tuple);
		}
	}

	/**
	 * 연관관계가 없는 엔티티 외부 조인
	 * ex: 회원의 이름이 팀 이름과 같은 대상 외부 조인
	 * JPQL: select m, t from Member m left join Team t on m.username = t.name
	 * SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.username = t.name
	 */
	@Test
	void join_on_no_relation() {
		em.persist(new Member("teamA"));
		em.persist(new Member("teamB"));
		em.persist(new Member("teamC"));

		List<Tuple> result = queryFactory.select(member, team)
										 .from(member)
										 .leftJoin(team).on(member.username.eq(team.name))
										 .fetch();

		for (Tuple tuple : result) {
			System.out.println("tuple = " + tuple);
		}
	}

	@PersistenceUnit private EntityManagerFactory emf;

	@Test
	void fetchJoinNo() {
		em.flush();
		em.clear();

		Member findMember = queryFactory.selectFrom(member)
										.where(member.username.eq("member1"))
										.fetchOne();

		boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
		assertThat(loaded).as("페치 조인 미적용").isFalse();
	}

	@Test
	void fetchJoinUse() {
		em.flush();
		em.clear();

		Member findMember = queryFactory.selectFrom(member)
										.join(member.team, team).fetchJoin()
										.where(member.username.eq("member1"))
										.fetchOne();

		boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
		assertThat(loaded).as("페치 조인 적용").isTrue();
	}

	/**
	 * 나이가 가장 많은 회원 조회
	 * - eq()
	 */
	@Test
	void subQuery() {
		QMember memberSub = new QMember("memberSub");

		List<Member> result = queryFactory.selectFrom(member)
										  .where(member.age.eq(
											  select(memberSub.age.max())
												  .from(memberSub)
										  ))
										  .fetch();

		assertThat(result).extracting("age")
						  .containsExactly(40);
	}

	/**
	 * 나이가 평균 나이 이상인 회원 조회
	 * - goe()
	 */
	@Test
	void subQueryGoe() {
		QMember memberSub = new QMember("memberSub");

		List<Member> result = queryFactory.selectFrom(member)
										  .where(member.age.goe(
											  select(memberSub.age.avg())
												  .from(memberSub)
										  ))
										  .fetch();

		assertThat(result).extracting("age")
						  .containsExactly(30, 40);
	}

	/**
	 * 서브쿼리 여러 건 처리, in 사용
	 */
	@Test
	void subQueryIn() {
		QMember memberSub = new QMember("memberSub");

		List<Member> result = queryFactory.selectFrom(member)
										  .where(member.age.in(
											  select(memberSub.age)
												  .from(memberSub)
												  .where(memberSub.age.gt(10))
										  ))
										  .fetch();

		assertThat(result).extracting("age")
						  .containsExactly(20, 30, 40);
	}

	/**
	 * select 절에 서브쿼리 사용
	 */
	@Test
	void selectSubQuery() {
		QMember memberSub = new QMember("memberSub");

		List<Tuple> fetch = queryFactory.select(
											member.username,
											select(memberSub.age.avg())
												.from(memberSub))
										.from(member)
										.fetch();

		for (Tuple tuple : fetch) {
			System.out.println("username = " + tuple.get(member.username));
			System.out.println("age = " + tuple.get(select(memberSub.age.avg())
														.from(memberSub)));
		}
	}

	/**
	 * Case 문: 단순한 조건
	 */
	@Test
	void basicCase() {
		List<String> result = queryFactory
			.select(member.age
						.when(10).then("열살")
						.when(20).then("스무살")
						.otherwise("기타"))
			.from(member)
			.fetch();

		for (String s : result) {
			System.out.println("s = " + s);
		}
	}

	/**
	 * Case 문: 복잡한  조건
	 */
	@Test
	void complexCase() {
		List<String> result = queryFactory
			.select(new CaseBuilder()
						.when(member.age.between(0, 20)).then("0 ~ 20살")
						.when(member.age.between(21, 30)).then("21 ~ 30살")
						.otherwise("기타"))
			.from(member)
			.fetch();

		for (String s : result) {
			System.out.println("s = " + s);
		}
	}

	/**
	 * Case 문: orderBy 절에서 사용
	 */
	@Test
	void orderByCase() {
		NumberExpression<Integer> rankPath = new CaseBuilder()
			.when(member.age.between(0, 20)).then(2)
			.when(member.age.between(21, 30)).then(1)
			.otherwise(3);

		List<Tuple> result = queryFactory
			.select(member.username, member.age, rankPath)
			.from(member)
			.orderBy(rankPath.desc())
			.fetch();

		for (Tuple tuple : result) {
			String username = tuple.get(member.username);
			Integer age = tuple.get(member.age);
			Integer rank = tuple.get(rankPath);
			System.out.println("username = " + username + " age = " + age + " rank = " + rank);
		}
	}

	@Test
	void constant() {
		List<Tuple> result = queryFactory
			.select(member.username, Expressions.constant("A"))
			.from(member)
			.fetch();

		for (Tuple tuple : result) {
			System.out.println("tuple = " + tuple);
		}
	}

	@Test
	void concat() {
		List<String> result = queryFactory
			.select(member.username.concat("_").concat(member.age.stringValue()))
			.from(member)
			.where(member.username.eq("member1"))
			.fetch();

		for (String s : result) {
			System.out.println("s = " + s);
		}
	}

}
