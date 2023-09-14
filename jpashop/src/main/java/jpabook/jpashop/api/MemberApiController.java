package jpabook.jpashop.api;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
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
	 * 회원 조회 V1: 응답 값으로 엔티티를 직접 외부에 노출
	 */
	@GetMapping("/api/v1/members")
	public List<Member> membersV1() {
		return memberService.findMembers();
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

	/**
	 * 회원 수정
	 */
	@PostMapping("/api/v2/members/{id}")
	public UpdateMemberResponse updateMemberV2(
		@PathVariable("id") Long id,
		@RequestBody @Valid UpdateMemberRequest request) {

		memberService.update(id, request.getName());
		Member findMember = memberService.findOne(id);
		return new UpdateMemberResponse(findMember.getId(), findMember.getName());
	}

	/**
	 * 회원 조회 V2: 응답 값으로 엔티티가 아닌 별도의 DTO 사용
	 */
	@GetMapping("/api/v2/members")
	public Result membersV2() {
		List<Member> findMembers = memberService.findMembers();
		List<MemberDto> collect = findMembers.stream()
											 .map(m -> new MemberDto(m.getName()))
											 .collect(Collectors.toList());
		return new Result(collect);
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

	@Data
	static class UpdateMemberRequest {

		private String name;

	}

	@Data
	@AllArgsConstructor
	static class UpdateMemberResponse {

		private Long id;
		private String name;

	}

	@Data
	@AllArgsConstructor
	static class Result<T> {

		private T data;

	}

	@Data
	@AllArgsConstructor
	static class MemberDto {

		private String name;

	}

}
