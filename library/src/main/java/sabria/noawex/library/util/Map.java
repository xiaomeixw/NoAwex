package sabria.noawex.library.util;

/**
 * Created by xiong,An android project Engineer,on 31/5/2016.
 * Data:31/5/2016  下午 05:29
 * Base on clever-m.com(JAVA Service)
 * Describe:
 * Version:1.0
 * Open source
 */
public interface Map<K, V> {

    int size();

    V get(Object key);

    boolean containsKey(Object key);

    V put(K key, V value);

    V remove(K key);

    Iterable<V> values();

    ArrayMap<K, V> clone();

    void clear();

    class Provider{

        public static <K,V> Map<K,V> get(){
            return new ArrayMap<>();
        }

        public static <K,V> Map<K,V> getSync(){
            return new SyncArrayMap<>();
        }

    }



}
