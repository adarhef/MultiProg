public class Dispatcher implements Runnable {
	PaddedPrimitiveNonVolatile<Boolean> done;
	final PacketSource pkt;
	long fingerprint = 0;
	long totalPackets;
	final int numSources;
	final boolean uniformBool;
	PacketQueue[] queueBank;
	Packet[] pendingPacketArray;
	long[] workerPacketNumArray;
	long maxWorkerPacketNum = 0;
	
	private Packet getPacket(int sourceNum) {
		if (uniformBool)
			return pkt.getUniformPacket(sourceNum);
		else
			return pkt.getExponentialPacket(sourceNum);
	}

	public Dispatcher(PaddedPrimitiveNonVolatile<Boolean> done,
			PacketSource pkt, boolean uniformBool, int numSources,
			PacketQueue[] queueBank) {
		this.done = done;
		this.pkt = pkt;
		this.uniformBool = uniformBool;
		this.numSources = numSources;
		this.queueBank = queueBank;
		this.pendingPacketArray = new Packet[numSources];
		for (int i = 0; i < numSources; i++) {
			pendingPacketArray[i] = getPacket(i);
		}
		totalPackets = numSources;
		workerPacketNumArray = new long[numSources]; //elements initialized to 0 by default
	}

	public void run() {
		while (!done.value) {
			for (int i = 0; i < numSources; i++) {
				try {
					queueBank[i].enq(pendingPacketArray[i]);
					pendingPacketArray[i] = getPacket(i);
					totalPackets++;
					workerPacketNumArray[i]++;
					if (workerPacketNumArray[i] > maxWorkerPacketNum)
						maxWorkerPacketNum = workerPacketNumArray[i];
				} catch (FullException e) {;}
			}
		}
		
		boolean workersEqual = true;
		for (int i = 0; i < numSources; i++) {
			if (workerPacketNumArray[i] < maxWorkerPacketNum)
				workersEqual = false;
			break;
		}
		while (!workersEqual) {
			for (int i = 0; i < numSources; i++) {
				if (workerPacketNumArray[i] < maxWorkerPacketNum) {
					try {
						queueBank[i].enq(pendingPacketArray[i]);
						pendingPacketArray[i] = getPacket(i);
						totalPackets++;
						workerPacketNumArray[i]++;
					} catch(FullException e) {;}
				}
			}
			workersEqual = true;
			for (int i = 0; i < numSources; i++) {
				if (workerPacketNumArray[i] < maxWorkerPacketNum)
					workersEqual = false;
				break;
			}
		}
	}
}