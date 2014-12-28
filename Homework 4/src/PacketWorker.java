public interface PacketWorker extends Runnable {
	public void run();
}

class SerialPacketWorker implements PacketWorker {
	PaddedPrimitiveNonVolatile<Boolean> done;
	final PacketSource pkt;
	final Fingerprint residue = new Fingerprint();
	long fingerprint = 0;
	long totalPackets = 0;
	final int numSources;
	final boolean uniformBool;

	public SerialPacketWorker(PaddedPrimitiveNonVolatile<Boolean> done,
			PacketSource pkt, boolean uniformBool, int numSources) {
		this.done = done;
		this.pkt = pkt;
		this.uniformBool = uniformBool;
		this.numSources = numSources;
	}

	public void run() {
		Packet tmp;
		while (!done.value) {
			for (int i = 0; i < numSources; i++) {
				if (uniformBool)
					tmp = pkt.getUniformPacket(i);
				else
					tmp = pkt.getExponentialPacket(i);
				totalPackets++;
				fingerprint += residue.getFingerprint(tmp.iterations, tmp.seed);
			}
		}
	}
}

class SerialQueuePacketWorker implements PacketWorker {
	final Fingerprint residue = new Fingerprint();
	long fingerprint = 0;
	long totalPackets = 0;
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

	public SerialQueuePacketWorker(PaddedPrimitiveNonVolatile<Boolean> done,
			PacketSource pkt,
			boolean uniformBool,
			int numSources,
			PacketQueue[] queueBank) {
		this.done = done;
		this.pkt = pkt;
		this.uniformBool = uniformBool;
		this.numSources = numSources;
		this.queueBank = queueBank;
		workerPacketNumArray = new long[numSources]; //elements initialized to 0 by default
		pendingPacketArray = new Packet[numSources];
		for (int i = 0; i < numSources; i++) {
			pendingPacketArray[i] = getPacket(i);
		}
	}

	public void run() {
		Packet tmp;
		while (!done.value) {
			for (int i = 0; i < numSources; i++) {
				try {
					queueBank[i].enq(pendingPacketArray[i]);
					pendingPacketArray[i] = getPacket(i);
				} catch (FullException e) {;}
				try {
					tmp = queueBank[i].deq();
					fingerprint += residue.getFingerprint(tmp.iterations, tmp.seed);
				} catch (EmptyException e) {;}
			}
		}
		for (int i = 0; i < numSources; i++) {
			tmp = pendingPacketArray[i];
			fingerprint += residue.getFingerprint(tmp.iterations, tmp.seed);
		}
	}
}

class ParallelPacketWorker implements PacketWorker {
	final Fingerprint residue = new Fingerprint();
	long fingerprint = 0;
	PaddedPrimitiveNonVolatile<Boolean> done;
	PacketQueue queue;

	public ParallelPacketWorker(PaddedPrimitiveNonVolatile<Boolean> done,
			PacketQueue queue) {
		this.done = done;
		this.queue = queue;
	}

	public void run() {
		Packet tmp;
		while (!done.value) {
			try {
				tmp = queue.deq();
				fingerprint += residue.getFingerprint(tmp.iterations, tmp.seed);
			} catch (EmptyException e) {;}
		}
		while (true) {
			try {
				tmp = queue.deq();
				fingerprint += residue.getFingerprint(tmp.iterations, tmp.seed);
			} catch (EmptyException e) {
				return;
			}
		}
	}
}