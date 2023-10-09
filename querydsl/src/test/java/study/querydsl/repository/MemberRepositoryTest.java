package study.querydsl.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import study.querydsl.entity.Member;

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

}
