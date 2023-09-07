package jpabook.jpashopreview;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class MemberRepositoryTest {

	@Autowired
	private MemberRepository memberRepository;

	@Test
	@DisplayName("회원 저장, 조회 테스트")
	@Transactional
	void testMember() {
		// given
		Member member = new Member();
		member.setUsername("memberA");

		// when
		Long savedId = memberRepository.save(member);
		Member findMember = memberRepository.find(savedId);

		// then
		assertThat(findMember.getId()).isEqualTo(member.getId());
		assertThat(findMember.getUsername()).isEqualTo(member.getUsername());

		// JPA 동일성 보장: 같은 영속성 컨텍스트 내 동일성 보장
		assertThat(findMember).isEqualTo(member);
	}

}
