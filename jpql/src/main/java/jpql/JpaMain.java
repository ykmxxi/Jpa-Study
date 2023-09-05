package jpql;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class JpaMain {

	public static void main(String[] args) {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();

		tx.begin();
		try {

			Team team1 = new Team();
			team1.setName("teamA");
			em.persist(team1);

			Team team2 = new Team();
			team2.setName("teamB");
			em.persist(team2);

			Member member1 = new Member();
			member1.setUsername("member1");
			member1.changeTeam(team1);
			em.persist(member1);

			Member member2 = new Member();
			member2.setUsername("member2");
			member2.changeTeam(team1);
			em.persist(member2);

			Member member3 = new Member();
			member3.setUsername("member3");
			member3.changeTeam(team2);
			em.persist(member3);

			em.flush();
			em.clear();

			String query = "select m from Member m join fetch m.team";
			List<Member> result = em.createQuery(query, Member.class)
									.getResultList();

			// member1, teamA (SQL)
			// member2, teamA (1차 캐시)
			// member3, teamB (SQL)
			for (Member member : result) {
				System.out.println("member = " + member.getUsername() + ", team = " + member.getTeam().getName());
			}

			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			e.printStackTrace();
		} finally {
			em.close();
		}

		emf.close();
	}

}
