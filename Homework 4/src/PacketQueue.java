import java.util.concurrent.atomic.AtomicBoolean;
/**
 * A wait-free queue implementation.
 */
class FullException extends Exception {
}

class EmptyException extends Exception {
}

public class PacketQueue {
	volatile int head = 0, tail = 0;
	Packet[] items;
	Lock lock;
	volatile boolean done = false;

	public PacketQueue(int capacity, int lockType) {
		items = new Packet[capacity];
		head = 0;
		tail = 0;
		this.lock = new LockAllocator().getLock(lockType);
	}

	public void enq(Packet x) throws FullException {
		if (tail - head == items.length)
			throw new FullException();

		items[tail % items.length] = x;
		tail++;
	}

	public Packet deq() throws EmptyException {
		if (tail - head == 0)
			throw new EmptyException();

		Packet x = items[head % items.length];
		head++;
		return x;
	}
}