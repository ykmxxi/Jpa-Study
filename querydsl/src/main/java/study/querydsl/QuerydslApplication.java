package study.querydsl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class QuerydslApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuerydslApplication.class, args);
	}

	// 스프링 빈으로 등록해 사용해도 됨
	// @Bean
	// JPAQueryFactory jpaQueryFactory(EntityManager em) {
	// 	return new JPAQueryFactory(em);
	// }

}
