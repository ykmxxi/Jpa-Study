package study.datajpa.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import study.datajpa.repository.MemberRepository;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberTest {

	@PersistenceContext EntityManager em;
	@Autowired MemberRepository memberRepository;

	@Test
	void testEntity() {
		// given
		Team teamA = new Team("teamA");
		Team teamB = new Team("teamB");
		em.persist(teamA);
		em.persist(teamB);

		Member member1 = new Member("member1", 10, teamA);
		Member member2 = new Member("member2", 20, teamA);
		Member member3 = new Member("member3", 30, teamB);
		Member member4 = new Member("member4", 40, teamB);
		em.persist(member1);
		em.persist(member2);
		em.persist(member3);
		em.persist(member4);

		em.flush();
		em.clear();

		// when
		List<Member> members = em.createQuery("select m from Member m", Member.class)
								 .getResultList();
		System.out.println("members = " + members);

		// then
		for (Member member : members) {
			System.out.println("member = " + member);
			System.out.println("member.team = " + member.getTeam());
		}

		assertThat(members.stream()
						  .map(Member::getUsername)
						  .collect(Collectors.toList())
		).containsExactly("member1", "member2", "member3", "member4");
	}

	@Test
	void jpaEventBaseEntity() throws Exception {
		// given
		Member member = new Member("member1");
		memberRepository.save(member); // @PrePersist 발생

		Thread.sleep(1000);
		member.setUsername("member2");

		em.flush(); // @PreUpdate
		em.clear();

		// when
		Member findMember = memberRepository.findById(member.getId()).get();

		// then
		assertThat(findMember.getUsername()).isEqualTo("member2");
		assertThat(findMember.getUpdatedDate()).isAfter(findMember.getCreatedDate());
		System.out.println("findMember.getCreatedDate() = " + findMember.getCreatedDate());
		System.out.println("findMember.getUpdatedDate() = " + findMember.getUpdatedDate());
	}

}
