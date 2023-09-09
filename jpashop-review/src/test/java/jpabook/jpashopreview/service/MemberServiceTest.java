package jpabook.jpashopreview.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import jpabook.jpashopreview.domain.Member;
import jpabook.jpashopreview.repository.MemberRepository;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
class MemberServiceTest {

	@Autowired MemberRepository memberRepository;
	@Autowired MemberService memberService;

	@Test
	void 회원가입() {
		// given
		Member member = new Member();
		member.setName("kim");

		// when
		Long savedId = memberService.join(member);

		// then
		assertEquals(member, memberService.findOne(savedId));
	}

	@Test()
	void 중복_회원_예제() {
		// given
		Member member1 = new Member();
		member1.setName("kim");

		Member member2 = new Member();
		member2.setName("kim");

		// when & then
		assertThatThrownBy(() -> {
			memberService.join(member1);
			memberService.join(member2);
		}).isInstanceOf(IllegalStateException.class)
		  .hasMessage("이미 존재하는 회원입니다.");
	}

}
