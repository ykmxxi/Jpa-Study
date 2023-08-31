package hellojpa;

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
			member.setName("hello");

			em.persist(member);

			em.flush();
			em.clear();

			// 데이터베이스를 통해서 실제 엔티티 객체 조회
//			Member findMember = em.find(Member.class, member.getId());
//			System.out.println("findMember.Name = " + findMember.getName());

			// 데이터베이스 조회를 미루는 가짜(프록시) 엔티티 객체 조회
			Member referenceMember = em.getReference(Member.class, member.getId());
			System.out.println("findMember = " + referenceMember.getClass()); // hellojpa.Member$HibernateProxy$baUfHhoA
			System.out.println("findMember.id = " + referenceMember.getId()); // 이때 쿼리를 날리지 않음(값이 이미 있기 때문)
			System.out.println("findMember.Name = " + referenceMember.getName()); // 이때 쿼리를 날림(실제 엔티티가 생성되어 있지 않고 DB에 값이 있기 때문)

			tx.commit(); // 트랜잭션 커밋
		} catch (Exception e) {
			tx.rollback(); // 트랜잭션 롤백
		} finally {
			em.close();
		}

		emf.close();
	}

}
