package hellojpa;

public class ValueMain {

	public static void main(String[] args) {

		int a = 10;
		int b = a; // a의 값이 복사가 되어서 b에 들어가는 것, 공유가 안되고 있음

		a = 20;

		System.out.println("a = " + a); // 20
		System.out.println("b = " + b); // 10
	}

}
