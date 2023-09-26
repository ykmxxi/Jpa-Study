package study.datajpa.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import study.datajpa.entity.Member;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberJpaRepositoryTest {

	@Autowired private MemberJpaRepository memberJpaRepository;

	@Test
	void testMember() {
		// given
		Member member = new Member("memberA");
		Member savedMember = memberJpaRepository.save(member);

		// when
		Member findMember = memberJpaRepository.find(savedMember.getId());

		// then
		assertThat(findMember.getId()).isEqualTo(member.getId());
		assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
		assertThat(findMember).isEqualTo(member);
	}

	@Test
	void basicCRUD() {
		Member member1 = new Member("member1");
		Member member2 = new Member("member2");
		memberJpaRepository.save(member1);
		memberJpaRepository.save(member2);

		// 단건 조회 검증
		Member findMember1 = memberJpaRepository.findById(member1.getId()).get();
		Member findMember2 = memberJpaRepository.findById(member2.getId()).get();
		assertThat(findMember1).isEqualTo(member1);
		assertThat(findMember2).isEqualTo(member2);

		// 리스트 조회 검증
		List<Member> members = memberJpaRepository.findAll();
		assertThat(members.size()).isEqualTo(2);

		// 카운트 검증
		long count = memberJpaRepository.count();
		assertThat(count).isEqualTo(2L);

		// 삭제 검증
		memberJpaRepository.delete(member1);
		memberJpaRepository.delete(member2);

		long deleteCount = memberJpaRepository.count();
		assertThat(deleteCount).isEqualTo(0L);
	}

	@Test
	void findByUsernameAndAgeGreaterThan() {
		// given
		Member member1 = new Member("kim", 10);
		Member member2 = new Member("kim", 20);
		memberJpaRepository.save(member1);
		memberJpaRepository.save(member2);

		// when
		List<Member> result = memberJpaRepository.findByUsernameAndAgeGreaterThan("kim", 15);

		// then
		assertThat(result.get(0).getUsername()).isEqualTo("kim");
		assertThat(result.get(0).getAge()).isEqualTo(20);
		assertThat(result.size()).isEqualTo(1);
	}

	@Test
	void findByUsername() {
		// given
		Member member = new Member("kim");
		memberJpaRepository.save(member);

		// when
		List<Member> result = memberJpaRepository.findByUsername("kim");

		// then
		assertThat(result.get(0).getUsername()).isEqualTo("kim");
		assertThat(result.get(0)).isEqualTo(member);
	}

	@Test
	void paging() {
		// given
		memberJpaRepository.save(new Member("member1", 10));
		memberJpaRepository.save(new Member("member2", 10));
		memberJpaRepository.save(new Member("member3", 10));
		memberJpaRepository.save(new Member("member4", 10));
		memberJpaRepository.save(new Member("member5", 10));

		int age = 10;
		int offset = 0;
		int limit = 3;

		// when
		List<Member> members = memberJpaRepository.findByPage(age, offset, limit);
		long totalCount = memberJpaRepository.totalCount(age);

		// 페이지 계산 공식 적용...
		// totalPage = totalCount / size ...
		// 마지막 페이지 ...
		// 최초 페이지 ..

		// then
		assertThat(members.size()).isEqualTo(3);
		assertThat(totalCount).isEqualTo(5L);
	}

}
