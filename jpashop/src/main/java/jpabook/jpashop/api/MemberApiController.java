package jpabook.jpashop.api;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

	private final MemberService memberService;

	/**
	 * V1: 엔티티를 Request Body에 직접 매핑
	 */
	@PostMapping("/api/v1/members")
	public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
		Long id = memberService.join(member);

		return new CreateMemberResponse(id);
	}

	/**
	 * V2: DTO를 Request Body에 매핑
	 */
	@PostMapping("/api/v2/members")
	public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
		Member member = new Member();
		member.setName(request.getName());

		Long id = memberService.join(member);
		return new CreateMemberResponse(id);
	}

	@Data
	static class CreateMemberResponse {

		private Long id;

		public CreateMemberResponse(Long id) {
			this.id = id;
		}

	}

	@Data
	static class CreateMemberRequest {

		private String name;

	}

}
