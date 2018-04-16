package net.ilkinulas.redis;

import java.util.List;

public interface Queue {
    String poll();
    List<String> poll(int count);
    long size();
    long add(String s);
    long add(List<String> l);
    void clear();
}
