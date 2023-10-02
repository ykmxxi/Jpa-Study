package study.querydsl;

import static org.assertj.core.api.Assertions.assertThat;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQueryFactory;

import study.querydsl.entity.Hello;
import study.querydsl.entity.QHello;

@SpringBootTest
@Transactional
class QuerydslApplicationTests {

	@Autowired private EntityManager em;

	@Test
	@DisplayName("Querydsl 동작 검증 테스트")
	void contextLoads() {
		// given
		Hello hello = new Hello();
		em.persist(hello);

		// when
		JPAQueryFactory query = new JPAQueryFactory(em);
		QHello qHello = new QHello("hello");

		Hello result = query.selectFrom(qHello)
							.fetchOne();

		// then
		assertThat(result).isEqualTo(hello);
		assertThat(result.getId()).isEqualTo(hello.getId());
	}

}
