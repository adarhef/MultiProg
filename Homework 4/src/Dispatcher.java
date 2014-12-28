public class Dispatcher implements Runnable {
	long fingerprint = 0;
	long totalPackets = 0;
	long maxWorkerPacketNum = 0;
	PaddedPrimitiveNonVolatile<Boolean> done;
	final PacketSource pkt;
	final int numSources;
	final boolean uniformBool;
	PacketQueue[] queueBank;
	long[] workerPacketNumArray;
	Packet[] pendingPacketArray;
	
	private Packet getPacket(int sourceNum) {
		totalPackets++;
		workerPacketNumArray[sourceNum]++;
		if (uniformBool)
			return pkt.getUniformPacket(sourceNum);
		else
			return pkt.getExponentialPacket(sourceNum);
	}

	public Dispatcher(PaddedPrimitiveNonVolatile<Boolean> done,
			PacketSource pkt,
			boolean uniformBool,
			int numSources,
			PacketQueue[] queueBank) {
		this.done = done;
		this.pkt = pkt;
		this.numSources = numSources;
		this.uniformBool = uniformBool;
		this.queueBank = queueBank;
		workerPacketNumArray = new long[numSources]; //elements initialized to 0 by default
		pendingPacketArray = new Packet[numSources];
		for (int i = 0; i < numSources; i++) {
			pendingPacketArray[i] = getPacket(i);
		}
	}

	public void run() {
		while (!done.value) {
			for (int i = 0; i < numSources; i++) {
				try {
					queueBank[i].enq(pendingPacketArray[i]);
					pendingPacketArray[i] = getPacket(i);
					if (workerPacketNumArray[i] > maxWorkerPacketNum)
						maxWorkerPacketNum = workerPacketNumArray[i];
				} catch (FullException e) {;}
			}
		}
		
		boolean workersEqual = true;
		for (int i = 0; i < numSources; i++) {
			if (workerPacketNumArray[i] < maxWorkerPacketNum) {
				workersEqual = false;
				break;
			}
		}
		while (!workersEqual) {
			for (int i = 0; i < numSources; i++) {
				if (workerPacketNumArray[i] < maxWorkerPacketNum) {
					try {
						queueBank[i].enq(pendingPacketArray[i]);
						pendingPacketArray[i] = getPacket(i);
					} catch(FullException e) {;}
				}
			}
			
			workersEqual = true;
			for (int i = 0; i < numSources; i++) {
				if (workerPacketNumArray[i] < maxWorkerPacketNum) {
					workersEqual = false;
					break;
				}
			}
		}
	}
}