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

		tx.begin();
		try {
			// 팀 저장
			Team team = new Team();
			team.setName("TeamA");
			em.persist(team);

			// 회원 저장
			Member member = new Member();
			member.setName("member1");
			member.setTeam(team); // 단방향 연관관계 설정, 참조 저장
			em.persist(member);

			em.flush();
			em.clear();

			Member findMember = em.find(Member.class, member.getId());
			List<Member> members = findMember.getTeam().getMembers();
			for (Member m : members) {
				System.out.println("m = " + m.getName());
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
