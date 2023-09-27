package study.datajpa.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

	/**
	 * 이름과 나이를 기준으로 전체 회원을 조회
	 */
	List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

	//	@Query(name = "Member.findByUsername") // 생략 가능
	List<Member> findByUsername(@Param("username") String username);

	@Query("select m from Member m where m.username = :username and m.age = :age")
	List<Member> findUser(@Param("username") String username, @Param("age") int age);

	@Query("select m.username from Member m")
	List<String> findUsernameList();

	@Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
	List<MemberDto> findMemberDto();

	@Query("select m from Member m where m.username = :name")
	Member findMembers(@Param("name") String username);

	@Query("select m from Member m where m.username in :names")
	List<Member> findByNames(@Param("names") List<String> names);

	Optional<Member> findOptionalByUsername(String username);

	/**
	 * count 쿼리 분리
	 * - 실무에서 매우 중요한 내용
	 * - 분리를 통해 count 쿼리에 불필요한 join을 없앨 수 있음
	 */
	@Query(value = "select m from Member m left join m.team t",
		countQuery = "select count(m) from Member m")
	Page<Member> findByAge(int age, Pageable pageable);

	Slice<Member> findSliceByAge(int age, Pageable pageable);

	/**
	 * 벌크성 수정 쿼리
	 * - @Modifying(clearAutomatically = true): 영속성 컨텍스트 초기화
	 * - 영속성 컨텍스트를 초기화해 DB와 데이터를 맞춘다
	 */
	@Modifying(clearAutomatically = true)
	@Query("update Member m set m.age = m.age + 1 where m.age >= :age")
	int bulkAgePlus(@Param("age") int age);

}
