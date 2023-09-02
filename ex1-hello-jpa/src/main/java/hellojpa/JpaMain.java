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

			Address address = new Address("city", "street", "10000");

			Member member1 = new Member();
			member1.setUsername("member1");
			member1.setHomeAddress(address);
			em.persist(member1);

			// 객체를 공유하지 않고 복사해서 사용해야 한다
			Address copyAddress = new Address(address.getCity(), address.getStreet(), address.getZipcode());
			Member member2 = new Member();
			member2.setUsername("member2");
			member2.setHomeAddress(copyAddress);
			em.persist(member2);

			member1.getHomeAddress().setCity("newCity"); // member1의 city만 변경하고 싶음

			// 복사해서 사용했기 때문에 member2는 변경되지 않음
			System.out.println("member2.City() = " + member2.getHomeAddress().getCity());

			tx.commit(); // 트랜잭션 커밋
		} catch (Exception e) {
			tx.rollback(); // 트랜잭션 롤백
		} finally {
			em.close();
		}

		emf.close();
	}

}
