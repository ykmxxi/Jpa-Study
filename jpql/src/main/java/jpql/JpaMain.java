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

			Product product1 = new Product();
			product1.setName("A");
			product1.setPrice(10000);
			product1.setStockAmount(5);
			em.persist(product1);

			Product product2 = new Product();
			product2.setName("B");
			product2.setPrice(20000);
			product2.setStockAmount(9);
			em.persist(product2);

			Product product3 = new Product();
			product3.setName("C");
			product3.setPrice(30000);
			product3.setStockAmount(10);
			em.persist(product3);

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

			// 컬렉션일 때. 즉, 일대다 관계에서 DB 입장에서 데이터가 중복 됨 -> DISTINCT 로 해결
//			String query2 = "select t from Team t join fetch t.members";
			String query2 = "select distinct t from Team t join fetch t.members";
			List<Team> result2 = em.createQuery(query2, Team.class)
								   .getResultList();
			for (Team team : result2) {
				System.out.println("team = " + team.getName() + ", 팀원 수 = " + team.getMembers().size());

				for (Member member : team.getMembers()) {
					System.out.println(team.getName() + "'s member = " + member.getUsername());
				}
			}

			// 엔티티 직접 사용: 기본 키(PK) 값 사용
			String query3 = "select m from Member m where m = :member";
			Member findMember = em.createQuery(query3, Member.class)
								  .setParameter("member", member1)
								  .getSingleResult();
			System.out.println("findMember = " + findMember);

			// 엔티티 직접 사용: 외래 키(FK) 값 사용
			Team findTeam = em.find(Team.class, 1L);
			String query1 = "select m from Member m where m.team = :team";
			List<Member> result3 = em.createQuery(query1, Member.class)
									 .setParameter("team", findTeam)
									 .getResultList();
			for (Member member : result3) {
				System.out.println("member.Username = " + member.getUsername());
			}

			// Named 쿼리: 어노테이션 사용
			Member result4 = em.createNamedQuery("Member.findByUsername", Member.class)
							   .setParameter("username", "member3")
							   .getSingleResult();
			System.out.println("result4 = " + result4.getUsername());

			// 벌크 연산
			String query4 = "update Product p "
				+ "set p.price = p.price * 1.1 "
				+ "where p.stockAmount < :stockAmount";
			int cnt = em.createQuery(query4)
						.setParameter("stockAmount", 10)
						.executeUpdate();
			System.out.println("cnt = " + cnt);

			// 벌크 연산 주의점: 데이터 정합성 문제가 발생할 수 있다
			System.out.println("product1.price = " + product1.getPrice()); // 10,000 그대로

			// 영속성 컨텍스트를 초기화하고 DB에서 다시 조회해야 함
			em.clear();
			Product findProduct = em.find(Product.class, product1.getId());
			System.out.println("find product1.price = " + findProduct.getPrice()); // 11,000

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
