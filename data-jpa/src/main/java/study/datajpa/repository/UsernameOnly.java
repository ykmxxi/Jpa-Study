package study.datajpa.repository;

public interface UsernameOnly {

	/**
	 * Closed Projection
	 */
	String getUsername();

	/**
	 * Open Projection
	 */
	// @Value("#{target.username + ' ' + target.age + ' ' + target.team.name}")
	// String getUsername();

}
