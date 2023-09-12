package jpabook.jpashopreview.controller;

import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import jpabook.jpashopreview.domain.Address;
import jpabook.jpashopreview.domain.Member;
import jpabook.jpashopreview.service.MemberService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;

	@GetMapping("/members/new")
	public String createForm(Model model) {
		model.addAttribute("memberForm", new MemberForm());
		return "members/createMemberForm";
	}

	@PostMapping("/members/new")
	public String create(@Valid MemberForm memberForm, BindingResult result) {
		if (result.hasErrors()) {
			return "members/createMemberForm";
		}
		Address address = new Address(memberForm.getCity(), memberForm.getStreet(), memberForm.getZipcode());

		Member member = new Member();
		member.setName(memberForm.getName());
		member.setAddress(address);

		memberService.join(member);
		return "redirect:/";
	}

}
