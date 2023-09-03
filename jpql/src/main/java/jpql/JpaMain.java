package jpql;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

public class JpaMain {

	public static void main(String[] args) {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();

		tx.begin();
		try {

			Member member = new Member();
			member.setUsername("member1");
			member.setAge(10);
			em.persist(member);

			// 반환 타입이 명확
			TypedQuery<Member> typedQuery = em.createQuery("select m from Member m", Member.class);
			// 반환 타입이 명확하지 않음
			Query query = em.createQuery("select m.username, m.age from Member m");

			List<Member> result = typedQuery.getResultList();
			for (Member m : result) {
				System.out.println("member.name = " + m.getUsername());
			}

			// 파라미터 바인딩: 이름 기준을 사용하자
			Member resultMember = em.createQuery("select m from Member m where m.username = :username", Member.class)
									.setParameter("username", "member1")
									.getSingleResult();
			System.out.println("resultMember = " + resultMember.getUsername());

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
