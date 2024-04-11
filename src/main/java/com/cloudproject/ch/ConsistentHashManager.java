package com.cloudproject.ch;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class ConsistentHashManager {

    TreeMap<Integer, String> serverHash = new TreeMap<>();
    TreeMap<Integer, String> keyHash = new TreeMap<>();
    Map<String, String> keyToServerMap = new HashMap<>();


    public static int RING_LIMIT = 10;


    public void addNode(String server)
    {
        int hash = this.getHash(server);


        Map.Entry<Integer, String> floorEntry = this.serverHash.floorEntry(hash);


        this.serverHash.put(hash, server);


        System.out.println("hash for server : "+server+" is : "+hash);

        if(floorEntry != null){
            SortedMap<Integer, String> subMap = this.keyHash.subMap(floorEntry.getKey(), hash);
            if(subMap.isEmpty()){
                System.out.println("no rehashing required after adding server :"+server);
            }else {
                System.out.println("Rehashing is required after adding server :"+server);
                for(Map.Entry<Integer, String> entry : subMap.entrySet()) {
                    this.addKey(entry.getValue());
                }
            }
        }
    }

    public void removeServer(String server)
    {
        int hash = this.getHash(server);

        this.serverHash.remove(hash);

        Map.Entry<Integer, String> floorEntry = this.serverHash.floorEntry(hash);
        Map.Entry<Integer, String> ceilingEntry = this.serverHash.ceilingEntry(hash);

        int start = floorEntry != null ? floorEntry.getKey() : this.serverHash.lastKey();
        int end = ceilingEntry != null ? ceilingEntry.getKey() : this.serverHash.firstKey();

        if(start < end){
            for(Map.Entry<Integer, String> entry : this.keyHash.subMap(start, end).entrySet()){
                this.addKey(entry.getValue());
            }
        }else {
            for(Map.Entry<Integer, String> entry : this.keyHash.subMap(Integer.MIN_VALUE, end).entrySet()){
                this.addKey(entry.getValue());
            }

            for(Map.Entry<Integer, String> entry : this.keyHash.subMap(start, Integer.MAX_VALUE).entrySet()){
                this.addKey(entry.getValue());
            }
        }
    }

    public void addKey(String key)
    {
        int hash = this.getHash(key);
        this.keyHash.put(hash, key);

        System.out.println("hash for key : "+key+" is : "+hash);


        Map.Entry<Integer, String> ceilingEntry = this.serverHash.ceilingEntry(hash);
        if(ceilingEntry != null){
            this.keyToServerMap.put(key, ceilingEntry.getValue());
        }else {
            this.keyToServerMap.put(key, this.serverHash.firstEntry().getValue());
        }
    }

    public String getNode(String key)
    {
        if (keyToServerMap.containsKey(key))
            return keyToServerMap.get(key);
        else
        {
            addKey(key);
            return keyToServerMap.get(key);
        }
    }

    private int getHash(String key) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < key.length(); i++) {
            hash = (hash ^ key.charAt(i)) * p;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        hash &= 0x7fffffff;
        return hash % RING_LIMIT;
    }

    public void removeKey(String key) {
        if (keyToServerMap.containsKey(key)) {
            int hash = getHash(key);
            keyHash.remove(hash);
            keyToServerMap.remove(key);
            System.out.println("Removed key: " + key);
        } else {
            System.out.println("Key " + key + " does not exist.");
        }
    }





}
