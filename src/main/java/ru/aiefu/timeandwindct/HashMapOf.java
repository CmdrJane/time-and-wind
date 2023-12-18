package ru.aiefu.timeandwindct;

import java.util.HashMap;

public class HashMapOf<K, V> extends HashMap<K, V> {
    public HashMapOf(K k1, V v1){
        putIfAbsent(k1, v1);
    }
}
