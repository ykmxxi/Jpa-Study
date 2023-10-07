package study.querydsl.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

	@Autowired private EntityManager em;
	@Autowired private MemberJpaRepository memberJpaRepository;

	@Test
	void basicTest() {
		Member member = new Member("member1", 10);
		memberJpaRepository.save(member);

		// 단 건 조회
		Member findMember = memberJpaRepository.findById(member.getId()).get();
		assertThat(findMember).isEqualTo(member);

		// 전체 조회
		List<Member> result1 = memberJpaRepository.findAll();
		assertThat(result1).containsExactly(member);

		// 조건 검색
		List<Member> result2 = memberJpaRepository.findByUsername("member1");
		assertThat(result2).containsExactly(member);
	}

	@Test
	void basicTest_Querydsl() {
		Member member = new Member("member1", 10);
		memberJpaRepository.save(member);

		// 전체 조회: querydsl
		List<Member> result1 = memberJpaRepository.findAll_Querydsl();
		assertThat(result1).containsExactly(member);

		// 조건 검색: querydsl
		List<Member> result2 = memberJpaRepository.findByUsername_Querydsl("member1");
		assertThat(result2).containsExactly(member);
	}

	@Test
	void searchTest() {
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

		MemberSearchCondition condition1 = new MemberSearchCondition();
		condition1.setAgeGoe(35);
		condition1.setAgeLoe(40);
		condition1.setTeamName("teamB");

		MemberSearchCondition condition2 = new MemberSearchCondition();
		condition2.setTeamName("teamB");

		List<MemberTeamDto> result1 = memberJpaRepository.searchByBuilder(condition1);
		List<MemberTeamDto> result2 = memberJpaRepository.searchByBuilder(condition2);

		assertThat(result1).extracting("username")
						   .containsExactly("member4");
		assertThat(result2).extracting("username")
						   .containsExactly("member3", "member4");
	}

}
