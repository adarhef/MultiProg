

public interface BucketList<T,K> {
  public boolean contains(K key);
  public boolean remove(K key);
  public void add(K key, T item);
  public int getSize();
  public abstract class Iterator {
    public abstract boolean hasNext();
    public abstract Iterator getNext();
  }
}

// ====================== Parallel Blocking Bucket List =========================
class LockingParallelBucketList<T,K> implements BucketList<T,K> {
    size = 0;
    LockingParallelBucketList<T,K>.Iterator<T,K> head;
    ReadWriteLock lock;

    public LockingParallelBucketList() {
        this.head = null;
        this.size = 0;
        this.lock = new SimpleReadWriteLock();
    }

    public Iterator<T,K> getHead() {
        return head;
    }
    
    public Iterator<T,K> getItem(K key) {
        LockingParallelBucketList<T,K>.Iterator<T,K> iterator = head;

        while (iterator != null) {
            if (iterator.key.equals(key)) {
                return iterator;
            } else {
                iterator = iterator.next;
            }            
        }
        return null;

    }
    public boolean contains(K key) {
        LockingParallelBucketList<T,K>.Iterator<T,K> iterator;
        
        lockRead();
        try {
            iterator = getItem(key);
        } finally {
            unlockRead();
        }
        
        if (iterator == null) {
            return false;
        } else {
            return true;
        }
    }
    public boolean remove(K key) {
        
        LockingParallelBucketList<T,K>.Iterator<T,K> iterator = head;
        if (iterator == null) {
          return false;
        }

        lockWrite();
        try {
            if (contains(key) == false) {
                return false;      
            }
            if (head.key.equals(key)) {
                head = head.getNext();
                size--;
                return true;
            }
            while (iterator.hasNext()) {
                if (iterator.getNext().key.equals(key)) {
                    iterator.setNext(iterator.getNext().getNext());
                    size--;
                    return true;
                } else { 
                    iterator = iterator.getNext();
                }
            }
            return false;
        } finally {
            unlockWrite();
        }
    }
    public void add(K key, T item) {
        LockingParallelBucketList<T,K>.Iterator<T,K> iterator = head;

        lockWrite();
        try {
            iterator = getItem(key);
            if (iterator != null) { // Item's already there
                return;
            } else {
                LockingParallelBucketList<T,K>.Iterator<T,K> firstItem = new Iterator<T,K>(key, item, head);
                head = firstItem;
                size++;
            }   
        } finally {
            unlockWrite();
        }        
    }
  
    public int getSize() {
        return size;
    }

    

    public void lockRead() {
        lock.readLock().lock();
    }
    public void lockWrite() {
        lock.writeLock().lock();   
    }
    public void unlockRead() {
        lock.readLock().unlock();
    }
    public void unlockWrite() {
        lock.writeLock().unlock();   
    }

    public class Iterator<T,K> {
    @SuppressWarnings("unchecked")
    public final K key;
    private T item;
    private Iterator<T,K> next;
    public Iterator(K key, T item, Iterator<T,K> next) {
      this.key = key;
      this.item = item;
      this.next = next;
    }
    @SuppressWarnings("unchecked")
    public Iterator() {
      this.key = (K) new Object();
      this.item = (T) new Object();
      this.next = null;
    }
    public boolean hasNext() {
      return next != null;
    }
    @SuppressWarnings("unchecked")
    public Iterator getNext() {
      return next;
    }
    public void setNext(Iterator<T,K> next) {this.next = next; }
    public T getItem() { return item; }
    public void setItem(T item) { this.item = item; }
  }


}
// ====================== Serial Bucket List =========================
class SerialList<T,K> implements BucketList<T,K> {
  int size = 0;
  SerialList<T,K>.Iterator<T,K> head;

  public SerialList() {
    this.head = null;
    this.size = 0;
  }
  public SerialList(K key, T item) {
    this.head = new Iterator<T,K>(key,item,null);
    this.size = 1;
  }
  public Iterator<T,K> getHead() {
    return head;
  }
  public Iterator<T,K> getItem(K key) {
    SerialList<T,K>.Iterator<T,K> iterator = head;
    while( iterator != null ) {
      if( iterator.key.equals(key) )
        return iterator;
      else
        iterator = iterator.next;
    }
    return null;
  }
  public boolean contains(K key) {
    SerialList<T,K>.Iterator<T,K> iterator = getItem(key);
    if( iterator == null )
      return false;
    else
      return true;
  }
  @SuppressWarnings("unchecked")
  public boolean remove(K key) {
    if( contains(key) == false )
      return false;
    SerialList<T,K>.Iterator<T,K> iterator = head;
    if( iterator == null )
      return false;
    if( head.key.equals(key) ) {
      head = head.getNext();
      size--;
      return true;
    }
    while( iterator.hasNext() ) {
      if( iterator.getNext().key.equals(key) ) {
        iterator.setNext(iterator.getNext().getNext());
        size--;
        return true;
      }
      else
        iterator = iterator.getNext();
    }
    return false;
  }
  public void add(K key, T item) {
    SerialList<T,K>.Iterator<T,K> tmpItem = getItem(key);
    if( tmpItem != null ) {
      tmpItem.item = item; // we're overwriting, so the size stays the same
    }
    else {
      @SuppressWarnings("unchecked")      
      SerialList<T,K>.Iterator<T,K> firstItem = new Iterator<T,K>(key, item, head);
      head = firstItem;
      size++;
    }
  }
  public void addNoCheck(K key, T item) {
    SerialList<T,K>.Iterator<T,K> firstItem = new Iterator<T,K>(key, item, head);
    head = firstItem;
    size++;
  }
  public int getSize() {
    return size;
  }
  @SuppressWarnings("unchecked")
  public void printList() {
    SerialList<T,K>.Iterator<T,K> iterator = head;
    System.out.println("Size: " + size);
    while( iterator != null ) {
      System.out.println(iterator.getItem());
      iterator = iterator.getNext();
    }
  }
  public class Iterator<T,K> {
    @SuppressWarnings("unchecked")
    public final K key;
    private T item;
    private Iterator<T,K> next;
    public Iterator(K key, T item, Iterator<T,K> next) {
      this.key = key;
      this.item = item;
      this.next = next;
    }
    @SuppressWarnings("unchecked")
    public Iterator() {
      this.key = (K) new Object();
      this.item = (T) new Object();
      this.next = null;
    }
    public boolean hasNext() {
      return next != null;
    }
    @SuppressWarnings("unchecked")
    public Iterator getNext() {
      return next;
    }
    public void setNext(Iterator<T,K> next) {this.next = next; }
    public T getItem() { return item; }
    public void setItem(T item) { this.item = item; }
  }
}

class BucketListTest {
  public static void main(String[] args) {  
    SerialList<Long,Long> list = new SerialList<Long,Long>();
    for( long i = 0; i < 15; i++ ) {
      list.add(i,i*i);
      list.printList();
    }
    for( long i = 14; i > 0; i -= 2 ) {
      list.remove(i);
      list.printList();
    }
  }
}
