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

			// 단순 값을 DTO로 조회
			List<MemberDTO> resultList =
				em.createQuery("select new jpql.MemberDTO(m.username, m.age) from Member m", MemberDTO.class)
				  .getResultList();

			MemberDTO memberDTO = resultList.get(0);
			System.out.println("member.name = " + memberDTO.getUsername());
			System.out.println("memberDTO.age = " + memberDTO.getAge());

			Team t = new Team();
			t.setName("teamA");
			em.persist(t);

			Member m = new Member();
			m.setUsername("member2");
			m.setAge(20);
			m.changeTeam(t);
			em.persist(m);

			em.flush();
			em.clear();

			// 조인: 내부 조인, 외부 조인, 세타 조인
			String inner = "select m from Member m join m.team t";
			String outer = "select m from Member m left join m.team t";
			String theta = "select m from Member m, Team t where m.username = t.name";
			List<Member> innerResult = em.createQuery(inner, Member.class)
										 .getResultList();

			List<Member> outerResult = em.createQuery(outer, Member.class)
										 .getResultList();

			List<Member> thetaResult = em.createQuery(theta, Member.class)
										 .getResultList();
			System.out.println("thetaResult.size() = " + thetaResult.size());

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
