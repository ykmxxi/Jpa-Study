package hellojpa;

import java.util.List;
import java.util.Set;

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
			member.setUsername("member1");
			member.setHomeAddress(new Address("homeCity", "street1", "10000"));

			member.getFavoriteFoods().add("치킨");
			member.getFavoriteFoods().add("족발");
			member.getFavoriteFoods().add("피자");

			member.getAddressHistory().add(new Address("old1", "street1", "10000"));
			member.getAddressHistory().add(new Address("old2", "street1", "10000"));

			em.persist(member);

			em.flush();
			em.clear();

			System.out.println("================= START =================");
			Member findMember = em.find(Member.class, member.getId());
			System.out.println("================= END =================");

			// 컬렉션들을 지연 로딩
			List<Address> addressHistory = findMember.getAddressHistory();
			for (Address address : addressHistory) {
				System.out.println("address = " + address.getCity());
			}

			Set<String> favoriteFoods = findMember.getFavoriteFoods();
			for (String favoriteFood : favoriteFoods) {
				System.out.println("favoriteFood = " + favoriteFood);
			}

			// 값 타입 수정: homeCity -> newCity, 객체를 새로운 객체로 교체해야 함
			Address oldAddr = findMember.getHomeAddress();
			findMember.setHomeAddress(new Address("newCity", oldAddr.getStreet(), oldAddr.getZipcode()));

			// 값 타입 컬렉션 수정: 치킨 -> 한식, old1 -> newCity1
			findMember.getFavoriteFoods().remove("치킨");
			findMember.getFavoriteFoods().add("한식");

			findMember.getAddressHistory().remove(new Address("old1", "street1", "10000"));
			findMember.getAddressHistory().add(new Address("newCity1", "street", "10000"));

			tx.commit(); // 트랜잭션 커밋
		} catch (Exception e) {
			tx.rollback(); // 트랜잭션 롤백
		} finally {
			em.close();
		}

		emf.close();
	}

}
