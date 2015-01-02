import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.ReentrantLock;

public interface Lock {
	public void lock();

	public boolean tryLock();

	public void unlock();
}

class TASLock implements Lock {
	AtomicBoolean state = new AtomicBoolean(false);

	public void lock() {
		while (state.getAndSet(true)) {
			;
		} // keep trying to get the lock
	}

	public boolean tryLock() {
		if (!state.get()) { // check first if it the lock is free
			this.lock(); // grab it
			return true;
		} else
			return false;
	}

	public void unlock() {
		state.set(false);
	}
}

class Backoff { // helper class for the BackoffLock
	volatile int tmp = 100;
	final int minDelay, maxDelay;
	int limit;
	int seed;

	public Backoff(int minDelay, int maxDelay) {
		this.minDelay = minDelay;
		this.maxDelay = maxDelay;
		this.limit = minDelay;
	}

	public void backoff() throws InterruptedException {
		for (int i = 0; i < limit; i++) { // simple 'local' delay - replace if
											// you like...
			tmp += i;
		}
		if (2 * limit <= maxDelay)
			limit = 2 * limit;
	}
}

class BackoffLock implements Lock {
	private AtomicBoolean state = new AtomicBoolean(false);
	private static final int MIN_DELAY = 6193; // You should tune these
												// parameters...
	private static final int MAX_DELAY = 100000000;

	public void lock() {
		while (state.get()) {
			;
		} // try to get the lock once before allocating a new Backoff(...)
		if (!state.getAndSet(true)) {
			return;
		} else {
			Backoff backoff = new Backoff(MIN_DELAY, MAX_DELAY);
			try {
				backoff.backoff();
			} catch (InterruptedException ignore) {
				;
			}
			while (true) {
				while (state.get()) {
					;
				}
				if (!state.getAndSet(true)) {
					return;
				} else {
					try {
						backoff.backoff();
					} catch (InterruptedException ignore) {
						;
					}
				}
			}
		}
	}

	public boolean tryLock() {
		if (!state.get()) {
			this.lock();
			return true;
		} else
			return false;
	}

	public void unlock() {
		state.set(false);
	}
}

class CLHLock implements Lock {
	AtomicReference<QNode> tail;
	ThreadLocal<QNode> myPred;
	ThreadLocal<QNode> myNode;

	class QNode {
		volatile boolean locked;

		public QNode() {
			locked = true;
		}

		public QNode(boolean flag) {
			locked = flag;
		}
	}

	public CLHLock() {
		tail = new AtomicReference<QNode>(new QNode(false));

		myNode = new ThreadLocal<QNode>() {
			protected QNode initialValue() {
				return new QNode();
			}
		};

		myPred = new ThreadLocal<QNode>() {
			protected QNode initialValue() {
				return null;
			}
		};
	}

	public boolean tryLock() {
		if (tail.get().locked == false || tail.get() == null) {
			lock();
			return true;
		} else {
			return false;
		}
	}

	public void lock() {
		QNode qnode = myNode.get();
		qnode.locked = true;
		QNode pred = tail.getAndSet(qnode);
		myPred.set(pred);
		while (pred.locked) {
		}
	}

	public void unlock() {
		QNode qnode = myNode.get();
		qnode.locked = false;
		myNode.set(myPred.get());
	}
}

class MCSLock implements Lock {

	class QNode {
		volatile boolean locked = false;
		volatile QNode next = null;
	}

	AtomicReference<QNode> tail;
	ThreadLocal<QNode> myNode;

	public MCSLock() {
		tail = new AtomicReference<QNode>(null);
		myNode = new ThreadLocal<QNode>() {
			protected QNode initialValue() {
				return new QNode();
			}
		};
	}

	public boolean tryLock() {
		if (tail.get() == null) {
			lock();
			return true;
		} else {
			return false;
		}

	}

	public void lock() {
		QNode qnode = myNode.get();
		QNode pred = tail.getAndSet(qnode);
		if (pred != null) {
			qnode.locked = true;
			pred.next = qnode;
			while (qnode.locked) {
			}
		}
	}

	public void unlock() {
		QNode qnode = myNode.get();
		if (qnode.next == null) {
			if (tail.compareAndSet(qnode, null))
				return;
			while (qnode.next == null) {
			}
		}
		qnode.next.locked = false;
		qnode.next = null;
	}

}

class LockAllocator {
	public Lock getLock(int lockType) {
		return getLock(lockType, 128);
	}

	public Lock getLock(int lockType, int maxThreads) {
		Lock lock;
		if (lockType == 0) {
			lock = new TASLock();
		} else if (lockType == 1) {
			lock = new BackoffLock();
		} else if (lockType == 4) {
			lock = new CLHLock(); // You need to write these...
		} else if (lockType == 5) {
			lock = new MCSLock(); // You need to write these...
		} else {
			System.out.println("This is not a valid lockType:");
			System.out.println(lockType);
			lock = (Lock) new ReentrantLock();
		}
		return lock;
	}

	static public void printLockType(int lockType) {
		if (lockType == 0) {
			System.out.println("TASLock");
		} else if (lockType == 1) {
			System.out.println("BackoffLock");
		} else if (lockType == 4) {
			System.out.println("CLHLock");
		} else if (lockType == 5) {
			System.out.println("MCSLock");
		} else {
			System.out.println("This is not a valid lockType:");
			System.out.println(lockType);
		}
	}

	static public void printLockTypes() {
		for (int i = 0; i < 6; i++) {
			System.out.println(i + ":");
			printLockType(i);
		}
	}
}
