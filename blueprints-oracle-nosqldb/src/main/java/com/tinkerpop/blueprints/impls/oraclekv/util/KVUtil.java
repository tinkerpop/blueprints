package com.tinkerpop.blueprints.impls.oraclekv.util;


import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.Key;
import oracle.kv.Value;
import oracle.kv.ValueVersion;

import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.*;
/**
 * @author Dan McClary
 */
public class KVUtil {
    
    public static Key keyFromString(String key)
    {
        List<String> majorComponents = new ArrayList<String>();
        List<String> minorComponents = new ArrayList<String>();
        String [] keyComponents = StringUtils.split(key,"/");
        
        for (int i = 0; i < keyComponents.length - 1; i ++)
            majorComponents.add(keyComponents[i]);
            
        minorComponents.add(keyComponents[keyComponents.length - 1]);
        Key kvKey = Key.createKey(majorComponents, minorComponents);
        return kvKey;
        
    }
    
    public static Key majorKeyFromString(String key)
    {
        List<String> majorComponents = new ArrayList<String>();
        String [] keyComponents = StringUtils.split(key,"/");
        
        for (int i = 0; i < keyComponents.length; i ++)
            majorComponents.add(keyComponents[i]);
            
        Key kvKey = Key.createKey(majorComponents);
        List<String> vertexAddress = kvKey.getFullPath();
        String vId = StringUtils.join(vertexAddress, ",");
        //System.out.println("major key is:" +vId);
        return kvKey;
        
    }
    
    public static Key keyFromString(String key, int splitIndex)
    {
        List<String> majorComponents = new ArrayList<String>();
        List<String> minorComponents = new ArrayList<String>();
        String [] keyComponents = StringUtils.split(key,"/");
        
        for (int i = 0; i < keyComponents.length - splitIndex; i ++)
            majorComponents.add(keyComponents[i]);
        for (int i = splitIndex; i < keyComponents.length; i++)
            minorComponents.add(keyComponents[i]);
            
        Key kvKey = Key.createKey(majorComponents, minorComponents);
        return kvKey;
        
    }
    
    public static byte[] toByteArray(Object obj)
    {
        byte [] returnBytes = null;
        ByteArrayOutputStream byteArrayOutStream = new ByteArrayOutputStream();
        ObjectOutput objectOut = null;
        try {
          objectOut = new ObjectOutputStream(byteArrayOutStream);   
          objectOut.writeObject(obj);
          returnBytes = byteArrayOutStream.toByteArray();
        } 
        catch (java.io.IOException ioe)
        {
            return null;
        }
        finally {
            try{
                objectOut.close();
                byteArrayOutStream.close();
            }
            catch (java.io.IOException ioe)
            {
             
            }
        }
        return returnBytes;
    }
    
    public static Object fromByteArray(byte [] inputBytes)
    {
        Object obj = null;
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(inputBytes);
        ObjectInput objIn = null;
        try {
          objIn = new ObjectInputStream(byteArrayInputStream);
          obj = objIn.readObject(); 
        } 
        catch (java.io.IOException ioe)
        {
            return null;
        }
        catch (java.lang.ClassNotFoundException cnfe)
        {
            return null;   
        }
        finally {
            try{
                byteArrayInputStream.close();
                objIn.close();
            }
            catch (java.io.IOException closeIOE)
            {
                
            }
        }
        return obj;
    }
    
    public static Object getValue(KVStore s, Key k)
    {
        Object returnVal = null;
        if (k != null && s != null)
        {
            ValueVersion vv = s.get(k);        
            if (vv != null)
                returnVal = fromByteArray(vv.getValue().getValue());
        }
        return returnVal;
    }
    
    public static void putValue(KVStore s, Key k, Object v)
    {
        Value val = Value.createValue(toByteArray(v));
        s.put(k,val);
    }
}


