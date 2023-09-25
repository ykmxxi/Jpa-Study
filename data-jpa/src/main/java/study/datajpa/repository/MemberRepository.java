package study.datajpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import study.datajpa.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

	/**
	 * 이름과 나이를 기준으로 전체 회원을 조회
	 */
	List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

}
