import java.io.PrintStream;


public class MppRunner {

	public static void main(String[] args) {
		
		final String experiment1Runtime = "2000";
		String[] arguments = new String[8];
		arguments[0] = experiment1Runtime;
		final String[] lockTypes = {"0", "1", "4", "5"};
		final String[] strategies = {"1", "2", "3", "4"};
		
		System.out.println("Counter Test 1: Idle Lock Overhead");
		System.out.println();
		
		System.out.println("SerialCounter:");
		SerialCounter.main(arguments);
		System.out.println("ParallelCounter (n = 1):");
		arguments[1] = "1";
		for (int i = 0; i < lockTypes.length; i++) {
			arguments[2] = lockTypes[i];
			ParallelCounter.main(arguments);
		}
		System.out.println();
		
		System.out.println("Counter Test 2: Lock Scaling");
		System.out.println();
		String[] n = {"1", "8", "64"};
		for (int i = 0; i < lockTypes.length; i++) {
			arguments[2] = lockTypes[i];
			for (int j = 0; j < n.length; j++) {
				arguments[1] = n[j];
				System.out.println("n = " + arguments[1]);
				ParallelCounter.main(arguments);
			}
			System.out.println();
		}
		
		System.out.println("Counter Test 3: Fairness (n = 32)");
		System.out.println();
		
		arguments[1] = "32";
		for (int i = 0; i < lockTypes.length; i++) {
			arguments[2] = lockTypes[i];
			ParallelCounter.main(arguments);
		}
		System.out.println();
		
		final String queueDepth = "8";
		arguments[5] = queueDepth;
		
		System.out.println("Packet Test 1: Idle Lock Overhead (n = 1, uniform packets)");
		System.out.println();
	
		arguments[1] = "1";
		arguments[3] = "false";
		String[] W = {"25", "200", "800"};
			
		for (int i = 0; i < lockTypes.length; i++) {
			arguments[6] = lockTypes[i];
			LockAllocator.printLockType(Integer.parseInt(arguments[6]));
			for (int k = 0; k < W.length; k++) {
				arguments[2] = W[k];
				System.out.println("W = " + W[k]);
				for (Integer trialNumber = 0; trialNumber < 3; trialNumber++) {
					arguments[4] = trialNumber.toString();
					System.out.println("Trial number " + trialNumber);
					for (Short strategy = 0; strategy <= 1; strategy++) {
						arguments[7] = strategy.toString();
						
					}
				}
				System.out.println();
			}
			System.out.println();
		}
		
		System.out.println("Packet Test 4: Speedup with Uniform Load");
		System.out.println();
		
		
	}
}
