package study.datajpa.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnitUtil;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberRepositoryTest {

	@Autowired private MemberRepository memberRepository;
	@Autowired private TeamRepository teamRepository;
	@PersistenceContext private EntityManager em;

	@Test
	void testMember() {
		// given
		Member member = new Member("memberA");
		Member savedMember = memberRepository.save(member);

		// when
		Member findMember = memberRepository.findById(savedMember.getId()).get();

		// then
		assertThat(findMember.getId()).isEqualTo(member.getId());
		assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
		assertThat(findMember).isEqualTo(member);
	}

	@Test
	void basicCRUD() {
		Member member1 = new Member("member1");
		Member member2 = new Member("member2");
		memberRepository.save(member1);
		memberRepository.save(member2);

		// 단건 조회 검증
		Member findMember1 = memberRepository.findById(member1.getId()).get();
		Member findMember2 = memberRepository.findById(member2.getId()).get();
		assertThat(findMember1).isEqualTo(member1);
		assertThat(findMember2).isEqualTo(member2);

		// 리스트 조회 검증
		List<Member> members = memberRepository.findAll();
		assertThat(members.size()).isEqualTo(2);

		// 카운트 검증
		long count = memberRepository.count();
		assertThat(count).isEqualTo(2L);

		// 삭제 검증
		memberRepository.delete(member1);
		memberRepository.delete(member2);

		long deleteCount = memberRepository.count();
		assertThat(deleteCount).isEqualTo(0L);
	}

	@Test
	void findByUsernameAndAgeGreaterThan() {
		// given
		Member member1 = new Member("kim", 10);
		Member member2 = new Member("kim", 20);
		memberRepository.save(member1);
		memberRepository.save(member2);

		// when
		List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("kim", 15);

		// then
		assertThat(result.get(0).getUsername()).isEqualTo("kim");
		assertThat(result.get(0).getAge()).isEqualTo(20);
		assertThat(result.size()).isEqualTo(1);
	}

	@Test
	void findByUsername() {
		// given
		Member member = new Member("kim");
		memberRepository.save(member);

		// when
		List<Member> result = memberRepository.findByUsername("kim");

		// then
		assertThat(result.get(0).getUsername()).isEqualTo("kim");
		assertThat(result.get(0)).isEqualTo(member);
	}

	@Test
	void findUser() {
		// given
		Member member1 = new Member("kim", 10);
		Member member2 = new Member("lee", 20);
		memberRepository.save(member1);
		memberRepository.save(member2);

		// when
		List<Member> result = memberRepository.findUser("kim", 10);

		// then
		assertThat(result.get(0)).isEqualTo(member1);
	}

	@Test
	void findUsernameList() {
		// given
		Member member1 = new Member("kim", 10);
		Member member2 = new Member("lee", 20);
		memberRepository.save(member1);
		memberRepository.save(member2);

		// when
		List<String> result = memberRepository.findUsernameList();

		// then
		assertThat(result).containsExactly("kim", "lee");
	}

	@Test
	void findMemberDto() {
		// given
		Team teamA = new Team("teamA");
		teamRepository.save(teamA);

		Member member1 = new Member("kim", 10, teamA);
		Member member2 = new Member("lee", 20, teamA);
		memberRepository.save(member1);
		memberRepository.save(member2);

		// when
		List<MemberDto> result = memberRepository.findMemberDto();

		// then
		assertThat(result.stream()
						 .map(MemberDto::getTeamName)
						 .distinct()
						 .collect(Collectors.toList())
		).containsExactly("teamA");
	}

	@Test
	void findByNames() {
		// given
		Member member1 = new Member("kim", 10);
		Member member2 = new Member("lee", 20);
		memberRepository.save(member1);
		memberRepository.save(member2);

		// when
		List<Member> result = memberRepository.findByNames(List.of("kim", "lee"));

		// then
		assertThat(result).containsExactly(member1, member2);
	}

	@Test
	void returnTypes() {
		// given
		Member member1 = new Member("kim", 10);
		Member member2 = new Member("lee", 20);
		memberRepository.save(member1);
		memberRepository.save(member2);

		// when
		Member result1 = memberRepository.findMembers("kim");
		List<Member> result2 = memberRepository.findByUsername("kim");
		Optional<Member> result3 = memberRepository.findOptionalByUsername("lee");

		// then
		System.out.println("result1 = " + result1);
		System.out.println("result2 = " + result2);
		System.out.println("result3 = " + result3);
	}

	@Test
	void paging() {
		// given
		memberRepository.save(new Member("member1", 10));
		memberRepository.save(new Member("member2", 10));
		memberRepository.save(new Member("member3", 10));
		memberRepository.save(new Member("member4", 10));
		memberRepository.save(new Member("member5", 10));

		int age = 10;
		PageRequest pageRequest = PageRequest.of(0, 3, Sort.Direction.DESC, "username");

		// when
		Page<Member> page = memberRepository.findByAge(age, pageRequest);
		Slice<Member> slice = memberRepository.findSliceByAge(age, pageRequest);
		// 실무에서는 엔티티를 DTO로 변환시킨 후 반환해야 한다
		Page<MemberDto> dtoPage = page.map(m -> new MemberDto(m.getId(), m.getUsername(), "teamA"));
		for (MemberDto memberDto : dtoPage) {
			System.out.println("memberDto = " + memberDto);
		}

		// then
		List<Member> content = page.getContent();
		List<Member> sliceContent = slice.getContent();

		assertThat(content.size()).isEqualTo(3); // 조회된 데이터 수
		assertThat(page.getTotalElements()).isEqualTo(5); // 전체 데이터 수
		assertThat(page.getNumber()).isEqualTo(0); // 페이지 번호
		assertThat(page.getTotalPages()).isEqualTo(2); // 전체 페이지 번호
		assertThat(page.isFirst()).isTrue(); // 첫번째 항목인가?
		assertThat(page.hasNext()).isTrue(); // 다음 페이지가 있는가?

		assertThat(sliceContent.size()).isEqualTo(3); // 조회된 데이터 수
//		assertThat(slice.getTotalElements()).isEqualTo(5); // Slice는 전체 count 쿼리가 안나감
		assertThat(slice.getNumber()).isEqualTo(0); // 페이지 번호
//		assertThat(slice.getTotalPages()).isEqualTo(2); // Slice는 전체 count 쿼리가 안나감
		assertThat(slice.isFirst()).isTrue(); // 첫번째 항목인가?
		assertThat(slice.hasNext()).isTrue(); // 다음 페이지가 있는가?
	}

	@Test
	void bulkUpdate() {
		// given
		memberRepository.save(new Member("member1", 10));
		memberRepository.save(new Member("member2", 19));
		memberRepository.save(new Member("member3", 20));
		memberRepository.save(new Member("member4", 21));
		memberRepository.save(new Member("member5", 40));

		// when
		int resultCount = memberRepository.bulkAgePlus(20);

		List<Member> members = memberRepository.findByUsername("member5");
		Member result = members.get(0);

		// then
		assertThat(resultCount).isEqualTo(3);
		assertThat(result.getAge()).isEqualTo(41);
	}

	@Test
	void findMemberLazy() {
		// given
		// member1 -> teamA, member2 -> teamB
		Team teamA = new Team("teamA");
		Team teamB = new Team("teamB");
		teamRepository.save(teamA);
		teamRepository.save(teamB);

		Member member1 = new Member("member1", 10, teamA);
		Member member2 = new Member("member2", 20, teamB);
		memberRepository.save(member1);
		memberRepository.save(member2);

		em.flush();
		em.clear();

		// when
		// N + 1 문제: member 조회(1) + team 조회(N)
		List<Member> members = memberRepository.findAll();

		// then
		for (Member member : members) {
			System.out.println("member = " + member);
			System.out.println("team.getClass() = " + member.getTeam().getClass()); // proxy
			System.out.println("member.team = " + member.getTeam().getName());
		}

		boolean isLazy1 = Hibernate.isInitialized(members.get(0).getTeam());

		PersistenceUnitUtil util = em.getEntityManagerFactory().getPersistenceUnitUtil();
		boolean isLazy2 = util.isLoaded(members.get(0).getTeam());

		assertThat(isLazy1).isTrue();
		assertThat(isLazy2).isTrue();
	}

	@Test
	void entityGraph() {
		// given
		Team teamA = new Team("teamA");
		Team teamB = new Team("teamB");
		teamRepository.save(teamA);
		teamRepository.save(teamB);

		Member member1 = new Member("member1", 10, teamA);
		Member member2 = new Member("member2", 20, teamB);
		memberRepository.save(member1);
		memberRepository.save(member2);

		em.flush();
		em.clear();

		// when
		List<Member> members = memberRepository.findAll();

		// then
		for (Member member : members) {
			System.out.println("member = " + member);
			System.out.println("member.team = " + member.getTeam().getName());
		}

		boolean isLazy1 = Hibernate.isInitialized(members.get(0).getTeam());
		assertThat(isLazy1).isTrue();
	}

	@Test
	void queryHint() {
		// given
		Member member1 = new Member("member1", 10);
		memberRepository.save(member1);
		em.flush();
		em.clear();

		// when
		Member findMember = memberRepository.findReadOnlyByUsername("member1");
		findMember.setUsername("member2");

		em.flush();
	}

	@Test
	void lock() {
		// given
		Member member1 = new Member("member1", 10);
		memberRepository.save(member1);
		em.flush();
		em.clear();

		// when
		List<Member> members = memberRepository.findLockByUsername("member1");

		// then
	}

	@Test
	void callCustom() {
		Member member1 = new Member("member1", 10);
		memberRepository.save(member1);

		List<Member> memberCustom = memberRepository.findMemberCustom();
		for (Member member : memberCustom) {
			System.out.println("member = " + member);
		}

	}

	@Test
	void specBasic() {
		// given
		Team teamA = new Team("teamA");
		em.persist(teamA);

		Member m1 = new Member("m1", 0, teamA);
		Member m2 = new Member("m2", 0, teamA);
		em.persist(m1);
		em.persist(m2);

		em.flush();
		em.clear();

		// when
		Specification<Member> spec = MemberSpec.username("m1").and(MemberSpec.teamName("teamA"));
		List<Member> result = memberRepository.findAll(spec);

		// then
		assertThat(result.size()).isEqualTo(1);
	}

	@Test
	void queryByExample() {
		// given
		Team teamA = new Team("teamA");
		em.persist(teamA);

		Member m1 = new Member("m1", 0, teamA);
		Member m2 = new Member("m2", 0, teamA);
		em.persist(m1);
		em.persist(m2);

		em.flush();
		em.clear();

		// when
		// Probe
		Member member = new Member("m1");
		Team team = new Team("teamA"); // 내부조인으로 teamA 가능
		member.setTeam(team);

		// ExampleMatcher 생성, age 프로퍼티는 무시
		ExampleMatcher matcher = ExampleMatcher.matching()
											   .withIgnorePaths("age");
		Example<Member> example = Example.of(member, matcher);

		List<Member> result = memberRepository.findAll(example);

		// then
		assertThat(result.size()).isEqualTo(1);
		assertThat(result.get(0).getUsername()).isEqualTo("m1");
		assertThat(result.get(0).getTeam().getName()).isEqualTo("teamA");
	}

	@Test
	void projections() {
		// given
		Team teamA = new Team("teamA");
		em.persist(teamA);

		Member m1 = new Member("m1", 0, teamA);
		Member m2 = new Member("m2", 0, teamA);
		em.persist(m1);
		em.persist(m2);

		em.flush();
		em.clear();

		// when
		List<UsernameOnly> result = memberRepository.findProjectionsByUsername("m1", UsernameOnly.class);
		List<UsernameOnlyDto> result2 = memberRepository.findProjectionsByUsername("m1", UsernameOnlyDto.class);

		// then
		assertThat(result.size()).isEqualTo(1);
		assertThat(result.get(0).getUsername()).isEqualTo("m1");

		assertThat(result2.size()).isEqualTo(1);
		assertThat(result2.get(0).getUsername()).isEqualTo("m1");
	}

}
