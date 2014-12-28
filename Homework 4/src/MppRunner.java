
public class MppRunner {

	public static void main(String[] args) {
		
		final String experiment1Runtime = "2000";
		System.out.println("Counter Test 1: Idle Lock Overhead");
		
		String[] arguments = new String[5];
		arguments[0] = experiment1Runtime;
		
		System.out.println("SerialCounter");
		SerialCounter.main(arguments);
		
		System.out.println("ParallelCounter");
		
		// TAS
		arguments[1] = "1";
		arguments[2] = "0";
		System.out.println("n = " + arguments[1]);
		ParallelCounter.main(arguments);
		// Backoff
		arguments[1] = "1";
		arguments[2] = "1";
		System.out.println("n = " + arguments[1]);
		ParallelCounter.main(arguments);
		// CLH
		arguments[1] = "1";
		arguments[2] = "4";
		System.out.println("n = " + arguments[1]);
		ParallelCounter.main(arguments);
		// MCS
		arguments[1] = "1";
		arguments[2] = "5";
		System.out.println("n = " + arguments[1]);
		ParallelCounter.main(arguments);

		System.out.println("Counter Test 2: Lock Scaling");
		arguments[1] = "1";

		arguments[2] = "0";
		System.out.println("n = " + arguments[1]);
		ParallelCounter.main(arguments);
		arguments[2] = "1";
		System.out.println("n = " + arguments[1]);
		ParallelCounter.main(arguments);
		arguments[2] = "4";
		System.out.println("n = " + arguments[1]);
		ParallelCounter.main(arguments);
		arguments[2] = "5";
		System.out.println("n = " + arguments[1]);
		ParallelCounter.main(arguments);

		arguments[1] = "8";

		arguments[2] = "0";
		System.out.println("n = " + arguments[1]);
		ParallelCounter.main(arguments);
		arguments[2] = "1";
		System.out.println("n = " + arguments[1]);
		ParallelCounter.main(arguments);
		arguments[2] = "4";
		System.out.println("n = " + arguments[1]);
		ParallelCounter.main(arguments);
		arguments[2] = "5";
		System.out.println("n = " + arguments[1]);
		ParallelCounter.main(arguments);

		arguments[1] = "64";

		arguments[2] = "0";
		System.out.println("n = " + arguments[1]);
		ParallelCounter.main(arguments);
		arguments[2] = "1";
		System.out.println("n = " + arguments[1]);
		ParallelCounter.main(arguments);
		arguments[2] = "4";
		System.out.println("n = " + arguments[1]);
		ParallelCounter.main(arguments);
		arguments[2] = "5";
		System.out.println("n = " + arguments[1]);
		ParallelCounter.main(arguments);
	}

}
