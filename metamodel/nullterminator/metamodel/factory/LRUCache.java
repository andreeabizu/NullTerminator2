package nullterminator.metamodel.factory;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class LRUCache <K, V> extends LinkedHashMap <K, V> {
   private int capacity;
   public LRUCache(int capacity) {
       super(capacity+1, 1.0f, true);
       this.capacity = capacity;
   }
   public void setCapacity(int capacity) {this.capacity = capacity;}
   @Override
   protected boolean removeEldestEntry(Entry<K, V> entry) {
       return (size() > this.capacity);
   }
}
