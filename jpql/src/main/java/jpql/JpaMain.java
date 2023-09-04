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

			Team team = new Team();
			team.setName("teamA");
			em.persist(team);

			Member member1 = new Member();
			member1.setUsername("member1");
			member1.setAge(10);
			member1.setType(MemberType.USER);

			member1.changeTeam(team);
			em.persist(member1);

			Member member2 = new Member();
			member2.setUsername("member2");
			member2.setAge(20);
			member2.setType(MemberType.ADMIN);

			member2.changeTeam(team);
			em.persist(member2);

			em.flush();
			em.clear();

			// 나이가 평균보다 많은 회원
			String query = "select m from Member m where m.age > (select avg(m2.age) from Member m2)";
			List<Member> result = em.createQuery(query, Member.class)
									.getResultList();

			for (Member m : result) {
				System.out.println("member.name = " + m.getUsername() + ", member.age = " + m.getAge());
			}

			// 어떤 팀이든 팀에 소속된 회원
			String query1 = "select m from Member m where m.team = ANY (select t from Team t)";
			List<Member> result1 = em.createQuery(query1, Member.class)
									 .getResultList();
			for (Member m : result1) {
				System.out.println("member.name = " + m.getUsername() + ", member.team = " + m.getTeam().getName());
			}

			// ADMIN인 유저 조회
			String query2 = "select m.username from Member m " +
				"where m.type = jpql.MemberType.ADMIN";
			List<String> result2 = em.createQuery(query2, String.class)
									 .getResultList();
			for (String name : result2) {
				System.out.println("name = " + name);
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
