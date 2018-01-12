package cukes.dto;

import org.apache.commons.collections.map.MultiValueMap;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ComplexHashMap<K,V> implements Map<K,V>, Cloneable, Serializable {

    MultiValueMap multiMap;

    public ComplexHashMap() {
        this.multiMap = new MultiValueMap();
    }

    @Override
    public int size() {
        return multiMap.size();
    }

    @Override
    public boolean isEmpty() {
        return multiMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return multiMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return multiMap.containsValue(value);
    }

    @Override
    public V get(Object key) {

        Object object = multiMap.get(key);

        if(object instanceof List) {
            List<?> list = (List<?> ) object;

            if(list.size() > 0) {
                return (V) list.get(0);
            }
        }

        return null;
    }

    public List<V> getAll(Object key) {

        Object object = multiMap.get(key);

        if(object instanceof List) {
            return (List<V>) object;
        }
        return null;
    }

    @Override
    public V put(K key, V value) {
        return (V) multiMap.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return (V) multiMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        multiMap.putAll(m);
    }

    @Override
    public void clear() {
        multiMap.clear();
    }

    @Override
    public Set<K> keySet() {
        return (Set<K>) multiMap.keySet();
    }

    @Override
    public Collection<V> values() {
        return (Collection<V>) multiMap.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return (Set<Entry<K, V>>) multiMap.entrySet();
    }
}
