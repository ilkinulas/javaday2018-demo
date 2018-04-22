package net.ilkinulas.redis;

import java.util.List;

public interface Queue {

    String poll();

    long add(String s);

    long size();

    long add(List<String> l);

    List<String> poll(int count);

    void clear();
    
}
