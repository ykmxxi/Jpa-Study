package study.querydsl.repository;

import static study.querydsl.entity.QMember.member;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import study.querydsl.entity.Member;

@Repository
public class MemberJpaRepository {

	private final EntityManager em;
	private final JPAQueryFactory queryFactory;

	public MemberJpaRepository(EntityManager em) {
		this.em = em;
		this.queryFactory = new JPAQueryFactory(em);
	}

	public void save(Member member) {
		em.persist(member);
	}

	public Optional<Member> findById(Long id) {
		return Optional.ofNullable(em.find(Member.class, id));
	}

	public List<Member> findAll() {
		return em.createQuery("select m from Member m", Member.class)
				 .getResultList();
	}

	public List<Member> findAll_Querydsl() {
		return queryFactory
			.selectFrom(member)
			.fetch();
	}

	public List<Member> findByUsername(String username) {
		return em.createQuery("select m from Member m where m.username = :username", Member.class)
				 .setParameter("username", username)
				 .getResultList();
	}

	public List<Member> findByUsername_Querydsl(String username) {
		return queryFactory
			.selectFrom(member)
			.where(member.username.eq(username))
			.fetch();
	}

}
