package study.querydsl;

import static study.querydsl.entity.QMember.member;

import java.util.List;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import study.querydsl.dto.MemberDto;
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

}
