package study.datajpa.repository;

import static org.assertj.core.api.Assertions.assertThat;

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

}


