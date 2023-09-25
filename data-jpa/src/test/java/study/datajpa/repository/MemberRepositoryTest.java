package study.datajpa.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

}
