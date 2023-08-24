package hellojpa;

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

		try {
			tx.begin(); // 트랜잭션 시작

			// Member member = new Member();
			// member.setId(2L);
			// member.setName("HelloJPA");
			// em.persist(member); // 저장

			// 생성만 하면 member는 비영속 상태
			Member member = new Member();
			member.setId(3L);
			member.setName("UserA");

			// member가 영속 상태가 됨
			// 이때 DB에 저장되는 것이 아님
			em.persist(member);

			// 변경 감지(Dirty Checking)
			Member findMember = em.find(Member.class, 2L);
			findMember.setName("이름 수정");

			// 준영속(detached) 상태
			Member findMember1 = em.find(Member.class, 1L);
			findMember1.setName("준영속");

			em.detach(findMember1);

			List<Member> members = em.createQuery("select m from Member m", Member.class)
				.getResultList();

			for (Member m : members) {
				System.out.println("member.name = " + m.getName());
			}

			tx.commit(); // 트랜잭션 커밋
		} catch (Exception e) {
			tx.rollback(); // 트랜잭션 롤백
		} finally {
			em.close();
		}

		emf.close();
	}

}
