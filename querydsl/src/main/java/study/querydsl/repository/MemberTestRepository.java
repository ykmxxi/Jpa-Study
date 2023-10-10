package study.querydsl.repository;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;

import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.entity.Member;
import study.querydsl.repository.support.Querydsl4RepositorySupport;

@Repository
public class MemberTestRepository extends Querydsl4RepositorySupport {

	public MemberTestRepository() {
		super(Member.class);
	}

	public List<Member> basicSelect() {
		return select(member)
			.from(member)
			.fetch();
	}

	public List<Member> basicSelectFrom() {
		return selectFrom(member)
			.fetch();
	}

	public Page<Member> searchPageByApplyPage(MemberSearchCondition condition, Pageable pageable) {
		JPAQuery<Member> query = selectFrom(member)
			.leftJoin(member.team, team)
			.where(usernameEq(condition.getUsername()),
				   teamNameEq(condition.getTeamName()),
				   ageGoe(condition.getAgeGoe()),
				   ageLoe(condition.getAgeLoe())
			);
		List<Member> content = getQuerydsl().applyPagination(pageable, query)
											.fetch();
		return PageableExecutionUtils.getPage(content, pageable, query::fetchCount);
	}

	/**
	 * Querydsl4RepositorySupport 기능 사용
	 */
	public Page<Member> applyPagination(MemberSearchCondition condition, Pageable pageable) {
		return applyPagination(pageable, query ->
			query.selectFrom(member)
				 .leftJoin(member.team, team)
				 .where(usernameEq(condition.getUsername()),
						teamNameEq(condition.getTeamName()),
						ageGoe(condition.getAgeGoe()),
						ageLoe(condition.getAgeLoe())
				 )
		);
	}

	/**
	 * applyPagination, count 쿼리 추가
	 */
	public Page<Member> applyPagination2(MemberSearchCondition condition, Pageable pageable) {
		return applyPagination(
			pageable,
			contentQuery ->
				contentQuery.selectFrom(member)
							.leftJoin(member.team, team)
							.where(usernameEq(condition.getUsername()),
								   teamNameEq(condition.getTeamName()),
								   ageGoe(condition.getAgeGoe()),
								   ageLoe(condition.getAgeLoe())
							),
			countQuery ->
				countQuery.select(member.id)
						  .from(member)
						  .leftJoin(member.team, team)
						  .where(usernameEq(condition.getUsername()),
								 teamNameEq(condition.getTeamName()),
								 ageGoe(condition.getAgeGoe()),
								 ageLoe(condition.getAgeLoe())
						  )
		);
	}

	private BooleanExpression usernameEq(String username) {
		return hasText(username) ? member.username.eq(username) : null;
	}

	private BooleanExpression teamNameEq(String teamName) {
		return hasText(teamName) ? team.name.eq(teamName) : null;
	}

	private BooleanExpression ageGoe(Integer ageGoe) {
		return ageGoe != null ? member.age.goe(ageGoe) : null;
	}

	private BooleanExpression ageLoe(Integer ageLoe) {
		return ageLoe != null ? member.age.loe(ageLoe) : null;
	}

}
