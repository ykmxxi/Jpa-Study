package jpabook.jpashop.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepositoryOld;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
class MemberServiceTest {

	@Autowired MemberService memberService;
	@Autowired MemberRepositoryOld memberRepositoryOld;

	@Test
	@DisplayName("회원 가입 성공 테스트")
	void join() {
		// given
		Member member = new Member();
		member.setName("kim");

		// when
		Long savedId = memberService.join(member);

		// then
		assertEquals(member, memberRepositoryOld.findOne(savedId));
	}

	@Test
	@DisplayName("중복된 이름이 존재하면 회원 가입 예외처리 테스트")
	void duplicateMember() {
		// given
		Member member1 = new Member();
		member1.setName("kim");

		Member member2 = new Member();
		member2.setName("kim");

		// when & then
		assertThatThrownBy(() -> {
			memberService.join(member1);
			memberService.join(member2);
		}).isInstanceOf(IllegalStateException.class);
	}

}
