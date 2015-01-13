import java.util.concurrent.locks.ReentrantReadWriteLock;

public interface HashTable<T> {
  public void add(int key, T x);
  public boolean remove(int key);
  public boolean contains(int key);
}

class LockingParallelHashTable<T> implements HashTable<T> {
    private SerialList<T,Integer>[] table;
    private int logSize;
    private int mask;
    private final int maxBucketSize;
    private ReentrantReadWriteLock[] locks;
    private int lockMask;

    @SuppressWarnings("unchecked")
    public LockingParallelHashTable(int logSize, int maxBucketSize, int numThreads) {
    	this.logSize = logSize;
        this.maxBucketSize = maxBucketSize;
        this.mask = (1 << logSize) - 1;
        this.table = new SerialList[1 << logSize];

        int numInExponent = (int) Math.ceil(Math.log(numThreads) / Math.log(2));
        int locksLength = (int) Math.pow(2, numInExponent);
        this.locks = new ReentrantReadWriteLock[locksLength];
        this.lockMask = (1 << numInExponent) - 1;
    }

    public void resizeIfNecessary(int key) {
        while (table[key & mask] != null && table[key & mask].getSize() >= maxBucketSize) {
            resize();
        }
    }
    @SuppressWarnings("unchecked")
	public void resize() {
        int oldTableLength = table.length;

        try {
            for(ReentrantReadWriteLock lock : locks) {
                lock.writeLock().lock();
            }

            // Check that no one finished resizing at this point
            if (table.length != oldTableLength) {
                return;
            }
            SerialList<T,Integer>[] newTable = new SerialList[2*oldTableLength];    
            logSize++;
            mask = (1 << logSize) - 1;
            for (SerialList<T, Integer> bucket : table) {
                
            	SerialList<T, Integer>.Iterator<T, Integer> curr = bucket.getHead();
            	
            	while (curr != null) {
                	newTable[curr.key & mask].add(curr.key, curr.getItem());
                }
            }
            
            table = newTable;
        
        } finally {
            for (ReentrantReadWriteLock lock : locks) {
                lock.writeLock().unlock();
            }

        }
    }
    public void add(int key, T item) {
        resizeIfNecessary(key);

        SerialList<T,Integer>[] tableBeforeLocking;  
        while (true) {
            try {
                tableBeforeLocking = table;    
                locks[key & lockMask].writeLock().lock();
                if (table.length == tableBeforeLocking.length) {
                    table[key & mask].add(key, item);
                }
            } finally {
                locks[key & lockMask].writeLock().unlock();
            }
        }

    }
    public boolean remove(int key) {
        SerialList<T,Integer>[] tableBeforeLocking;  
        while (true) {
            try {
                tableBeforeLocking = table;    
                locks[key & lockMask].writeLock().lock();
                if (table.length == tableBeforeLocking.length) {
                    return table[key & mask].remove(key);
                }
            } finally {
                locks[key & lockMask].writeLock().unlock();
            }
        }
    }
    public boolean contains(int key) {
        SerialList<T,Integer>[] tableBeforeLocking;  
        while (true) {
            try {
                tableBeforeLocking = table;    
                locks[key & lockMask].readLock().lock();
                if (table.length == tableBeforeLocking.length) {
                    return table[key & mask].contains(key);
                }
            } finally {
                locks[key & lockMask].readLock().unlock();
            }
        }
    }
    public void printTable() {
        for( int i = 0; i <= mask; i++ ) {
          System.out.println("...." + i + "....");
          if( table[i] != null)
            table[i].printList();
        }
      }

}

class SerialHashTable<T> implements HashTable<T> {
  private SerialList<T,Integer>[] table;
  private int logSize;
  private int mask;
  private final int maxBucketSize;
  @SuppressWarnings("unchecked")
  public SerialHashTable(int logSize, int maxBucketSize) {
    this.logSize = logSize;
    this.mask = (1 << logSize) - 1;
    this.maxBucketSize = maxBucketSize;
    this.table = new SerialList[1 << logSize];
  }
  public void resizeIfNecessary(int key) {
    while( table[key & mask] != null 
          && table[key & mask].getSize() >= maxBucketSize )
      resize();
  }
  private void addNoCheck(int key, T x) {
    int index = key & mask;
    if( table[index] == null )
      table[index] = new SerialList<T,Integer>(key,x);
    else
      table[index].addNoCheck(key,x);
  }
  public void add(int key, T x) {
    resizeIfNecessary(key);
    addNoCheck(key,x);
  }
  public boolean remove(int key) {
    resizeIfNecessary(key);
    if( table[key & mask] != null )
      return table[key & mask].remove(key);
    else
      return false;
  }
  public boolean contains(int key) {
    SerialList<T,Integer>[] myTable = table;
    int myMask = myTable.length - 1;
    if( myTable[key & myMask] != null )
      return myTable[key & myMask].contains(key);
    else
      return false;
  }
  @SuppressWarnings("unchecked")
  public void resize() {
    SerialList<T,Integer>[] newTable = new SerialList[2*table.length];
    for( int i = 0; i < table.length; i++ ) {
      if( table[i] == null )
        continue;
      SerialList<T,Integer>.Iterator<T,Integer> iterator = table[i].getHead();
      while( iterator != null ) {
        if( newTable[iterator.key & ((2*mask)+1)] == null )
          newTable[iterator.key & ((2*mask)+1)] = new SerialList<T,Integer>(iterator.key, iterator.getItem());
        else
          newTable[iterator.key & ((2*mask)+1)].addNoCheck(iterator.key, iterator.getItem());
        iterator = iterator.getNext();
      }
    }
    table = newTable;
    logSize++;
    mask = (1 << logSize) - 1;
  }
  public void printTable() {
    for( int i = 0; i <= mask; i++ ) {
      System.out.println("...." + i + "....");
      if( table[i] != null)
        table[i].printList();
    }
  }
}

class SerialHashTableTest {
  public static void main(String[] args) {  
    SerialHashTable<Integer> table = new SerialHashTable<Integer>(2, 8);
    for( int i = 0; i < 256; i++ ) {
      table.add(i,i*i);
    }
    table.printTable();    
  }
}
