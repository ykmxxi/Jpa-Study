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

			Member member3 = new Member();
			member3.setUsername("관리자");
			member2.setType(MemberType.ADMIN);

			em.persist(member3);

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

			// 기본 CASE 식
			String query3 =
				"select " +
					"case when m.age <= 19 then '학생요금' " +
					"when m.age >= 60 then '경로요금' " +
					"else '일반요금' " +
					"end " +
					"from Member m";

			List<String> resultList = em.createQuery(query3, String.class)
										.getResultList();
			for (String fee : resultList) {
				System.out.println("fee = " + fee);
			}

			// 사용자 이름이 '관리자'면 null을 반환하고 나머지는 본인의 이름을 반환
			String query4 = "select nullif(m.username, '관리자') from Member m";
			List<String> resultList1 = em.createQuery(query4, String.class)
										 .getResultList();
			for (String username : resultList1) {
				System.out.println("username = " + username);
			}

			// 성공: FROM 절에서 명시적 조인을 통해 별칭을 얻음
			// 항상 명시적 조인을 사용하자
			String query5 = "select m.username from Team t join t.members m";
			List<String> resultList2 = em.createQuery(query5, String.class)
										 .getResultList();
			for (String name : resultList2) {
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
