package study.querydsl.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MemberTest {

	@Autowired private EntityManager em;

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

		// then
		assertThat(members.stream()
						  .map(Member::getUsername)
						  .collect(Collectors.toList())
		).containsExactly("member1", "member2", "member3", "member4");

		assertThat(members.stream()
						  .map(Member::getTeam)
						  .map(Team::getName)
						  .distinct()
						  .collect(Collectors.toList())
		).containsExactly("teamA", "teamB");
	}

}
