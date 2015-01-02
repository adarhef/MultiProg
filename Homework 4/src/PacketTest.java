import java.io.PrintStream;


class SerialPacket {
  public static void main(String[] args) {

    final int numMilliseconds = Integer.parseInt(args[0]);    
    final int numSources = Integer.parseInt(args[1]);
    final long mean = Long.parseLong(args[2]);
    final boolean uniformFlag = Boolean.parseBoolean(args[3]);
    final short experimentNumber = Short.parseShort(args[4]);

    @SuppressWarnings({"unchecked"})
    StopWatch timer = new StopWatch();
    PacketSource pkt = new PacketSource(mean, numSources, experimentNumber);
    PaddedPrimitiveNonVolatile<Boolean> done = new PaddedPrimitiveNonVolatile<Boolean>(false);
    PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);
        
    SerialPacketWorker workerData = new SerialPacketWorker(done, pkt, uniformFlag, numSources);
    Thread workerThread = new Thread(workerData);
    
    workerThread.start();
    timer.startTimer();
    try {
      Thread.sleep(numMilliseconds);
    } catch (InterruptedException ignore) {;}
    done.value = true;
    memFence.value = true;  // memFence is a 'volatile' forcing a memory fence
    try {                   // which means that done.value is visible to the workers
      workerThread.join();
    } catch (InterruptedException ignore) {;}      
    timer.stopTimer();
    final long totalCount = workerData.totalPackets;
    System.out.println("count: " + totalCount);
    System.out.println("time: " + timer.getElapsedTime());
    System.out.println(totalCount/timer.getElapsedTime() + " pkts / ms");
  }
}

class ParallelPacket {
  public static void main(String[] args) {

    final int numMilliseconds = Integer.parseInt(args[0]);    
    final int numSources = Integer.parseInt(args[1]);
    final long mean = Long.parseLong(args[2]);
    final boolean uniformFlag = Boolean.parseBoolean(args[3]);
    final short experimentNumber = Short.parseShort(args[4]);
    final int queueDepth = Integer.parseInt(args[5]);
    final int lockType = Integer.parseInt(args[6]);
    final short strategy = Short.parseShort(args[7]);

    @SuppressWarnings({"unchecked"})
    //
    // Allocate and initialize your Lamport queues
    //
    PacketQueue[] queueBank = new PacketQueue[numSources];
    for (int i = 0; i < numSources; i++) {
        queueBank[i] = new PacketQueue(queueDepth, lockType);
    }

    StopWatch timer = new StopWatch();
    PacketSource pkt = new PacketSource(mean, numSources, experimentNumber);
    // 
    // Allocate and initialize locks and any signals used to marshal threads (eg. done signals)
    // 
    PaddedPrimitiveNonVolatile<Boolean> timeOver = new PaddedPrimitiveNonVolatile<Boolean>(false);
    PaddedPrimitiveNonVolatile<Boolean> dispatcherDone = new PaddedPrimitiveNonVolatile<Boolean>(false);
    PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);

    // Allocate and initialize Dispatcher and Worker threads
    //
    Dispatcher dispatcherData = new Dispatcher(timeOver, pkt, uniformFlag, numSources, queueBank);
    Thread dispatcherThread = new Thread(dispatcherData);

    ParallelPacketWorker[] workerArray = new ParallelPacketWorker[numSources];
    Thread[] workerThreadArray = new Thread[numSources];
    
    for (int i = 0; i < numSources; i++) {
        workerArray[i] = new ParallelPacketWorker(dispatcherDone, queueBank, strategy, i);
        workerThreadArray[i] = new Thread(workerArray[i]);
    }
    ParallelPacketWorker.queues = queueBank;


    // call .start() on your Workers
    //
    for (int i = 0; i < numSources; i++) {
        workerThreadArray[i].start();
    }
    timer.startTimer();
    // 
    // call .start() on your Dispatcher
    // 

    dispatcherThread.start();

    try {
      Thread.sleep(numMilliseconds);
    } catch (InterruptedException ignore) {;}
    // 
    // assert signals to stop Dispatcher - remember, Dispatcher needs to deliver an 
    // equal number of packets from each source
    //
    timeOver.value=true;
    memFence.value=true;

    // call .join() on Dispatcher
    //
    try {
        dispatcherThread.join();
    } catch(InterruptedException ignore) {;}

    // assert signals to stop Workers - they are responsible for leaving the queues
    // empty - use whatever protocol you like, but one easy one is to have each
    // worker verify that it's corresponding queue is empty after it observes the
    // done signal set to true
    //
    dispatcherDone.value=true;
    memFence.value=false;

    // call .join() for each Worker
    for (int i = 0; i < numSources; i++) {
        try{
            workerThreadArray[i].join();
        } catch(InterruptedException ignore) {;}
    }
    timer.stopTimer();
    final long totalCount = dispatcherData.totalPackets;
    printStrategy(strategy);
    System.out.println("count: " + totalCount);
    System.out.println("time: " + timer.getElapsedTime());
    System.out.println(totalCount/timer.getElapsedTime() + " pkts / ms");
  }
  
  static public void printStrategy(short strategy) {
		if (strategy == 1) {
			System.out.println("LockFree");
		} else if (strategy == 2) {
			System.out.println("HomeQueue");
		} else if (strategy == 3) {
			System.out.println("RandomQueue");
		} else if (strategy == 4) {
			System.out.println("LastQueue");
		} else {
			System.out.println("This is not a valid strategy:");
			System.out.println(strategy);
		}
	}
}