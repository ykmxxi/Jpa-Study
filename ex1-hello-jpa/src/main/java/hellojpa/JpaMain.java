package hellojpa;

import java.time.LocalDateTime;

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

			Member member = new Member();
			member.setUsername("임베디드");
			member.setHomeAddress(new Address("city", "street", "zipcode"));
			member.setWorkPeriod(new Period(LocalDateTime.now(), LocalDateTime.now()));

			em.persist(member);

			tx.commit(); // 트랜잭션 커밋
		} catch (Exception e) {
			tx.rollback(); // 트랜잭션 롤백
		} finally {
			em.close();
		}

		emf.close();
	}

}
