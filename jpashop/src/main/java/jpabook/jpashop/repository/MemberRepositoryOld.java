package jpabook.jpashop.repository;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryOld {

	private final EntityManager em;

	/**
	 * 회원 저장
	 */
	public void save(Member member) {
		em.persist(member);
	}

	/**
	 * 회원 단건 조회
	 */
	public Member findOne(Long id) {
		return em.find(Member.class, id);
	}

	/**
	 * 회원 전체 조회
	 */
	public List<Member> findAll() {
		return em.createQuery("select m from Member m", Member.class)
				 .getResultList();
	}

	/**
	 * 회원 이름으로 조회
	 */
	public List<Member> findByName(String name) {
		return em.createQuery("select m from Member m where m.name = :name", Member.class)
				 .setParameter("name", name)
				 .getResultList();
	}

}
