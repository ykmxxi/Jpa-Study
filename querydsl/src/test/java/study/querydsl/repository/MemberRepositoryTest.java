package study.querydsl.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

	@Autowired private EntityManager em;
	@Autowired private MemberRepository memberRepository;

	@Test
	void basicTest() {
		Member member = new Member("member1", 10);
		memberRepository.save(member);

		// 단 건 조회
		Member findMember = memberRepository.findById(member.getId()).get();
		assertThat(findMember).isEqualTo(member);

		// 전체 조회
		List<Member> result1 = memberRepository.findAll();
		assertThat(result1).containsExactly(member);

		// 조건 검색
		List<Member> result2 = memberRepository.findByUsername("member1");
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

		List<MemberTeamDto> result1 = memberRepository.search(condition1);
		List<MemberTeamDto> result2 = memberRepository.search(condition2);

		assertThat(result1).extracting("username")
						   .containsExactly("member4");
		assertThat(result2).extracting("username")
						   .containsExactly("member3", "member4");
	}

	@Test
	void searchPageSimple() {
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

		MemberSearchCondition condition = new MemberSearchCondition();
		PageRequest pageRequest = PageRequest.of(0, 3);

		Page<MemberTeamDto> result = memberRepository.searchPageSimple(condition, pageRequest);

		assertThat(result.getSize()).isEqualTo(3);
		assertThat(result.getContent()).extracting("username")
									   .containsExactly("member1", "member2", "member3");
	}

	@Test
	void querydslPredicateExecutor() {
		QMember member = QMember.member;
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

		Iterable<Member> result = memberRepository.findAll(
			member.age.between(10, 40).and(member.username.eq("member1")));
		result.forEach(m -> assertThat(m).extracting("username")
										 .isEqualTo("member1"));
	}

}
