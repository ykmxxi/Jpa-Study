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

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

@SpringBootTest
@Transactional
public class QuerydslIntermediateTest {

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

	/**
	 * 프로젝션 대상이 하나
	 */
	@Test
	void simpleProjection() {
		List<String> result = queryFactory
			.select(member.username)
			.from(member)
			.fetch();

		for (String name : result) {
			System.out.println("name = " + name);
		}
	}

	/**
	 * 프로젝션 대상이 둘 이상: Tuple or DTO
	 */
	@Test
	void tupleProjection() {
		List<Tuple> result = queryFactory
			.select(member.username, member.age)
			.from(member)
			.fetch();

		for (Tuple tuple : result) {
			String username = tuple.get(member.username);
			Integer age = tuple.get(member.age);

			System.out.println("username = " + username + ", age = " + age);
		}
	}

	/**
	 * 순수 JPA로 DTO 조회
	 */
	@Test
	void findDtoByJPQL() {
		String jpql = "select new study.querydsl.dto.MemberDto(m.username, m.age) from Member m";
		List<MemberDto> result = em.createQuery(jpql, MemberDto.class)
								   .getResultList();
		for (MemberDto memberDto : result) {
			System.out.println("memberDto = " + memberDto);
		}
	}

	/**
	 * Querydsl로 DTO 조회: 프로퍼티 접근(setter)
	 */
	@Test
	void findDtoBySetter() {
		List<MemberDto> result = queryFactory
			.select(Projections.bean(MemberDto.class, member.username, member.age))
			.from(member)
			.fetch();

		for (MemberDto memberDto : result) {
			System.out.println("memberDto = " + memberDto);
		}
	}

	/**
	 * Querydsl로 DTO 조회: 필드 직접 접근
	 */
	@Test
	void findDtoByField() {
		List<MemberDto> result = queryFactory
			.select(Projections.fields(MemberDto.class, member.username, member.age))
			.from(member)
			.fetch();

		for (MemberDto memberDto : result) {
			System.out.println("memberDto = " + memberDto);
		}
	}

	/**
	 * Querydsl로 DTO 조회: 생성자 사용
	 */
	@Test
	void findDtoByConstructor() {
		List<MemberDto> result = queryFactory
			.select(Projections.constructor(MemberDto.class, member.username, member.age))
			.from(member)
			.fetch();

		for (MemberDto memberDto : result) {
			System.out.println("memberDto = " + memberDto);
		}
	}

	/**
	 * 별칭(alias)이 다를 때
	 */
	@Test
	void findUserDtoByField() {
		QMember memberSub = new QMember("memberSub");
		List<UserDto> result = queryFactory
			.select(
				Projections.fields(
					UserDto.class,
					member.username.as("name"),
					ExpressionUtils.as(JPAExpressions
										   .select(memberSub.age.max())
										   .from(memberSub), "age")
				))
			.from(member)
			.fetch();

		for (UserDto userDto : result) {
			System.out.println("userDto = " + userDto);
		}
	}

	/**
	 * - @QueryProjection
	 * - 컴파일러로 타입을 체크할 수 있어 가장 안전한 방법
	 * - DTO에 Querydsl 어노테이션을 유지해야 하는 점과 DTO까지 Q-Type을 생성해야 하는 단점 존재
	 */
	@Test
	void findDtoByQueryProjection() {
		List<MemberDto> result = queryFactory
			.select(new QMemberDto(member.username, member.age))
			.from(member)
			.fetch();

		for (MemberDto memberDto : result) {
			System.out.println("memberDto = " + memberDto);
		}
	}

	/**
	 * 동적 쿼리 해결: BooleanBuilder 사용
	 */
	@Test
	void dynamicQuery_BooleanBuilder() {
		String usernameParam = "member1";
		Integer ageParam = 10;

		List<Member> result = searchMember1(usernameParam, ageParam);
		assertThat(result.size()).isEqualTo(1);
	}

	private List<Member> searchMember1(String usernameCondition, Integer ageCondition) {
		BooleanBuilder builder = new BooleanBuilder();
		if (usernameCondition != null) {
			builder.and(member.username.eq(usernameCondition));
		}
		if (ageCondition != null) {
			builder.and(member.age.eq(ageCondition));
		}

		return queryFactory
			.selectFrom(member)
			.where(builder)
			.fetch();
	}

	/**
	 * 동적 쿼리 해결: Where 다중 파라미터 사용
	 * - 코드가 깔끔해짐
	 * - where()은 null을 무시
	 * - 조건을 조립(Composition)할 수 있음
	 */
	@Test
	void dynamicQuery_WhereParameter() {
		String usernameParam = "member1";
		Integer ageParam = 10;

		List<Member> result = searchMember2(usernameParam, ageParam);
		assertThat(result.size()).isEqualTo(1);
	}

	private List<Member> searchMember2(String usernameCondition, Integer ageCondition) {
		return queryFactory
			.selectFrom(member)
			.where(allEq(usernameCondition, ageCondition))
			.fetch();
	}

	private BooleanExpression allEq(String usernameCondition, Integer ageCondition) {
		return usernameEq(usernameCondition).and(ageEq(ageCondition));
	}

	private BooleanExpression usernameEq(String usernameCondition) {
		if (usernameCondition == null) {
			return null;
		}
		return member.username.eq(usernameCondition);
	}

	private BooleanExpression ageEq(Integer ageCondition) {
		if (ageCondition == null) {
			return null;
		}
		return member.age.eq(ageCondition);
	}

	/**
	 * 벌크 연산: 수정
	 * - 벌크 연산 후 DB는 수정 쿼리가 반영되지만 영속성 컨텍스트 반영 X
	 * - 영속성 컨텍스트 초기화가 필요
	 */
	@Test
	void bulkUpdate() {
		long count = queryFactory
			.update(member)
			.set(member.username, "비회원")
			.where(member.age.lt(28))
			.execute();

		List<Member> before = queryFactory
			.selectFrom(member)
			.where(member.age.lt(28))
			.fetch();
		assertThat(before.get(0).getUsername()).isEqualTo("member1");

		em.flush();
		em.clear();

		List<Member> after = queryFactory
			.selectFrom(member)
			.where(member.age.lt(28))
			.fetch();
		assertThat(after.get(0).getUsername()).isEqualTo("비회원");
	}

	@Test
	void bulkAdd() {
		long count = queryFactory
			.update(member)
			.set(member.age, member.age.add(1))
			.execute();

		em.flush();
		em.clear();

		assertThat(count).isEqualTo(4L);
	}

	@Test
	void bulkDelete() {
		long count = queryFactory
			.delete(member)
			.where(member.age.gt(18))
			.execute();

		em.flush();
		em.clear();

		assertThat(count).isEqualTo(3L);
	}

	@Test
	void sqlFunction() {
		List<String> result = queryFactory
			.select(Expressions.stringTemplate(
				"function('replace', {0}, {1}, {2})",
				member.username, "member", "M"))
			.from(member)
			.fetch();

		for (String s : result) {
			System.out.println("s = " + s);
		}
	}

	@Test
	void sqlFunction2() {
		List<String> result = queryFactory
			.select(member.username)
			.from(member)
			.where(member.username.eq(
				Expressions.stringTemplate(
					"function('lower', {0})", member.username)
			))
			.fetch();

		for (String s : result) {
			System.out.println("s = " + s);
		}
	}

}
