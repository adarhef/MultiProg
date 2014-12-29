class SerialFirewall {
  public static void main(String[] args) {
    final int numMilliseconds = Integer.parseInt(args[0]);   
    final int numSources = Integer.parseInt(args[1]);
    final long mean = Long.parseLong(args[2]);
    final boolean uniformFlag = Boolean.parseBoolean(args[3]);
    final short experimentNumber = Short.parseShort(args[4]);
    
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

class SerialQueueFirewall {
  public static void main(String[] args) {
    final int numMilliseconds = Integer.parseInt(args[0]);   
    final int numSources = Integer.parseInt(args[1]);
    final long mean = Long.parseLong(args[2]);
    final boolean uniformFlag = Boolean.parseBoolean(args[3]);
    final int queueDepth = Integer.parseInt(args[4]);
    final short experimentNumber = Short.parseShort(args[5]);
   
    StopWatch timer = new StopWatch();
    PacketSource pkt = new PacketSource(mean, numSources, experimentNumber);
    PaddedPrimitiveNonVolatile<Boolean> done = new PaddedPrimitiveNonVolatile<Boolean>(false);
    PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);

    // ...
    // allocate and initialize bank of numSources Lamport queues
    // each with depth queueDepth
    // they should throw FullException and EmptyException upon those conditions
    PacketQueue[] queueBank = new PacketQueue[numSources];
    for (int i = 0; i < numSources; i++) {
    	queueBank[i] = new PacketQueue(queueDepth, null);
    }

    // Create a SerialQueuePackerWorker workerData 
    // as SerialPackerWorker, but be sure to Pass the lamport queues
    SerialQueuePacketWorker workerData = new SerialQueuePacketWorker(done, pkt, uniformFlag, numSources, queueBank);
    

    // The rest of the code looks as in Serial Firewall
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

class ParallelFirewall {
  public static void main(String[] args) {
    final int numMilliseconds = Integer.parseInt(args[0]);     
    final int numSources = Integer.parseInt(args[1]);
    final long mean = Long.parseLong(args[2]);
    final boolean uniformFlag = Boolean.parseBoolean(args[3]);
    final int queueDepth = Integer.parseInt(args[4]);
    final short experimentNumber = Short.parseShort(args[5]);

    StopWatch timer = new StopWatch();
    PacketSource pkt = new PacketSource(mean, numSources, experimentNumber);
    
    // Allocate and initialize bank of Lamport queues, as in SerialQueueFirewall
    PacketQueue[] queueBank = new PacketQueue[numSources];
    for (int i = 0; i < numSources; i++) {
    	queueBank[i] = new PacketQueue(queueDepth);
    }
    
    // Allocate and initialize any signals used to marshal threads (eg. done signals)
    PaddedPrimitiveNonVolatile<Boolean> timeOver = new PaddedPrimitiveNonVolatile<Boolean>(false);
    PaddedPrimitiveNonVolatile<Boolean> dispatcherDone = new PaddedPrimitiveNonVolatile<Boolean>(false);
    PaddedPrimitive<Boolean> memFence = new PaddedPrimitive<Boolean>(false);
    
    // Allocate and initialize a Dispatcher class implementing Runnable
    // and a corresponding Dispatcher Thread
    Dispatcher dispatcherData = new Dispatcher(timeOver, pkt, uniformFlag, numSources, queueBank);
    Thread dispatcherThread = new Thread(dispatcherData);
    
    // Allocate and initialize an array of Worker classes (ParallelPacketWorker), 
    // implementing Runnable and the corresponding Worker Threads
    ParallelPacketWorker[] workerArray = new ParallelPacketWorker[numSources];
    Thread[] workerThreadArray = new Thread[numSources];
    for (int i = 0; i < numSources; i++) {
    	workerArray[i] = new ParallelPacketWorker(dispatcherDone, queueBank[i]);
    	workerThreadArray[i] = new Thread(workerArray[i]);
    }
    
    // Call start() for each worker
    for (int i = 0; i < numSources; i++) {
    	workerThreadArray[i].start();
    }
    timer.startTimer();
    
    // Call start() for the Dispatcher thread
    dispatcherThread.start();
    try {
      Thread.sleep(numMilliseconds);
    } catch (InterruptedException ignore) {;}
    
    // assert signals to stop Dispatcher - remember, Dispatcher needs to deliver an 
    // equal number of packets from each source
    timeOver.value=true;
    memFence.value=true;
    // call .join() on Dispatcher
    try {
    	dispatcherThread.join();
    } catch(InterruptedException ignore) {;}
    // assert signals to stop Workers - they are responsible for leaving the queues
    // empty - use whatever protocol you like, but one easy one is to have each
    // worker verify that it's corresponding queue is empty after it observes the
    // done signal set to true
    dispatcherDone.value=true;
    memFence.value=false;
    // call .join() for each Worker
    for (int i = 0; i < numSources; i++) {
    	try{
    		workerThreadArray[i].join();
    	} catch(InterruptedException ignore) {;}
    }
    //If we wanted the total fingerprint, now would be a good place to sum the parts up.
    timer.stopTimer();
    
    // Output the statistics
    System.out.println("count: " + dispatcherData.totalPackets);
    System.out.println("time: " + timer.getElapsedTime());
    System.out.println(dispatcherData.totalPackets/timer.getElapsedTime() + " pkts / ms");
  }
}