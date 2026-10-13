package com.ems.config;

import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuthStore {
    private final Set<String> tokens = ConcurrentHashMap.newKeySet();
    public String issue() { String t = UUID.randomUUID().toString(); tokens.add(t); return t; }
    public boolean valid(String t) { return t != null && tokens.contains(t); }
    public void revoke(String t) { tokens.remove(t); }
}
