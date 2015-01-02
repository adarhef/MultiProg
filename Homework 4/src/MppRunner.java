import java.io.PrintStream;


public class MppRunner {

	public static void main(String[] args) {
		
		final String experiment1Runtime = "2000";
		String[] arguments = new String[8];
		arguments[0] = experiment1Runtime;
		final String[] lockTypes = {"0", "1", "4", "5"};
		/*
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
		*/
		final String queueDepth = "8";
		arguments[5] = queueDepth;
		
		System.out.println("Packet Test 1: Idle Lock Overhead (n = 1, uniform packets)");
		System.out.println();
	
		arguments[1] = "1";
		arguments[3] = "false";
		String[] W = {"25", "200", "800"};
		String[] S = {"1", "2"};
		
		Integer trialNumber = 5;
		/*
		for (int i = 0; i < lockTypes.length; i++) {
			arguments[6] = lockTypes[i];
			LockAllocator.printLockType(Integer.parseInt(arguments[6]));
			System.out.println();
			
			for (int k = 0; k < W.length; k++) {
				arguments[2] = W[k];
				System.out.println("W = " + W[k]);
					arguments[4] = trialNumber.toString();
					System.out.println("Trial number " + trialNumber);
					for (int l = 0; l < S.length; l++) {
						arguments[7] = S[l];
						ParallelPacket.printStrategy(Short.parseShort(arguments[7]));
						ParallelPacket.main(arguments);
					}
				System.out.println();
			}
		}

		System.out.println("Packet Test 2: Speedup with Uniform Load");
		System.out.println();
		*/
		String[] n2 = {"1", "4", "10"};
		String[] W2 = {"1000", "6000"};
		String[] S2 = {"1", "3", "4"};
		/*
		System.out.println("SerialPacket:");
		System.out.println();
		
		for (int i = 0; i < W2.length; i++) {
			arguments[2] = W2[i];
			System.out.println("W = " + arguments[2]);
			System.out.println();
			
			for (int j = 0; j < n2.length; j++) {
				arguments[1] = n2[j];
				System.out.println("n = " + arguments[1]);
					arguments[4] = trialNumber.toString();
					System.out.println("Trial number " + arguments[4]);
					SerialPacket.main(arguments);
				System.out.println();
			}
		}
		
		System.out.println("ParallelPacket:");
		System.out.println();
		
		for (int i = 0; i < W2.length; i++) {
			arguments[2] = W2[i];
			System.out.println("W = " + arguments[2]);
			System.out.println();
			
			for (int j = 0; j < n2.length; j++) {
				arguments[1] = n2[j];
				System.out.println("n = " + arguments[1]);
				for (int k = 0; k < S2.length; k++) {
					arguments[7] = S2[k];
					ParallelPacket.printStrategy(Short.parseShort(arguments[7]));
					for (int l = 0; l <= 2; l++) {
						arguments[6] = lockTypes[l];
						LockAllocator.printLockType(Integer.parseInt(arguments[6]));
							arguments[4] = trialNumber.toString();
							System.out.println("Trial number " + arguments[4]);
							ParallelPacket.main(arguments);
					}
				}
				System.out.println();
			}
		}
		*/
		System.out.println("Packet Test 3: Speedup with Exponential Load");
		System.out.println();
		
		n2[1] = "8"; n2[2] = "64";
		arguments[3] = "true";
		
		System.out.println("SerialPacket:");
		System.out.println();
		
		for (int i = 0; i < W2.length; i++) {
			arguments[2] = W2[i];
			System.out.println("W = " + arguments[2]);
			System.out.println();
			
			for (int j = 0; j < n2.length; j++) {
				arguments[1] = n2[j];
				System.out.println("n = " + arguments[1]);
					arguments[4] = trialNumber.toString();
					System.out.println("Trial number " + arguments[4]);
					SerialPacket.main(arguments);
				System.out.println();
			}
		}
		
		System.out.println("ParallelPacket:");
		System.out.println();
		
		for (int i = 0; i < W2.length; i++) {
			arguments[2] = W2[i];
			System.out.println("W = " + arguments[2]);
			System.out.println();
			
			for (int j = 0; j < n2.length; j++) {
				arguments[1] = n2[j];
				System.out.println("n = " + arguments[1]);
				for (int k = 0; k < S2.length; k++) {
					arguments[7] = S2[k];
					ParallelPacket.printStrategy(Short.parseShort(arguments[7]));
					for (int l = 0; l <= 2; l++) {
						arguments[6] = lockTypes[l];
						LockAllocator.printLockType(Integer.parseInt(arguments[6]));
							arguments[4] = trialNumber.toString();
							System.out.println("Trial number " + arguments[4]);
							ParallelPacket.main(arguments);
					}
				}
				System.out.println();
			}
		}
	}
}
