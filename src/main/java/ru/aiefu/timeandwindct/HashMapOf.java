package ru.aiefu.timeandwindct;

import java.util.HashMap;

public class HashMapOf<K, V> extends HashMap<K, V> {
    public HashMapOf(K k1, V v1){
        putIfAbsent(k1, v1);
    }
    public HashMapOf(K k1, V v1, K k2, V v2){
        putIfAbsent(k1, v1);
        putIfAbsent(k2, v2);
    }
    public HashMapOf(K k1, V v1, K k2, V v2, K k3, V v3){
        putIfAbsent(k1, v1);
        putIfAbsent(k2, v2);
        putIfAbsent(k3, v3);
    }
    public HashMapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4){
        putIfAbsent(k1, v1);
        putIfAbsent(k2, v2);
        putIfAbsent(k3, v3);
        putIfAbsent(k4, v4);
    }
    public HashMapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5){
        putIfAbsent(k1, v1);
        putIfAbsent(k2, v2);
        putIfAbsent(k3, v3);
        putIfAbsent(k4, v4);
        putIfAbsent(k5, v5);
    }
}
