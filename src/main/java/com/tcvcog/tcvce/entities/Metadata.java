/*
 * Copyright (C) 2020 Turtle Creek Valley
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.tcvcog.tcvce.entities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.EnumMap;

/**
 * Stores the metadata of a blob using an EnumMap
 * @author Nathan Dietz
 */
public class Metadata implements Serializable{
    
    private int bytesID;

    private BlobType type;
    private EnumMap<MetadataKey, String> properties;
    
    /**
     * Automatically creates an empty metadata map to prevent null pointers.
     */
    public Metadata(){
        properties = new EnumMap<>(MetadataKey.class);
    }
    
    //We're going to encapsulate the properties field because we want to make sure 
    //it's easy to grab values
    
    /**
     * Replaces the current dataMap with a clone of the supplied dataMap
     * @param dataMap 
     */
    public void replaceDataMap(EnumMap<MetadataKey, String> dataMap) {
        properties = new EnumMap<>(dataMap);
    }
    
    /**
     * This method should only be used in the integrator
     * @return 
     * @throws java.io.IOException 
     */
    public byte[] getMapBytes() throws IOException{
        
        byte[] output;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(properties);
            out.flush();
            output = bos.toByteArray();
        }
        
        return output;
    }
    
    /**
     * Checks if the metadata collection contains a specific property
     * @param key
     * @return 
     */
    public boolean hasProperty(MetadataKey key){
        return properties.containsKey(key);
    }
    
    /**
     * Checks if the metadata collection is empty
     * @return 
     */
    public boolean isEmpty(){
        return properties.isEmpty();
    }
    
    /**
     * Get a specific property from the metadata collection
     * @param key
     * @return the property or null if the property does not exist
     */
    public String getProperty(MetadataKey key){
        return properties.get(key);
    }
    
    /**
     * Set a specific property in the metadata collection
     * @param key The property you would like to set
     * @param value The value of the property
     */
    public void setProperty(MetadataKey key, String value){
        properties.put(key, value);
    }
    
    /**
     * Removes a specific property from the metadata collection
     * @param key 
     */
    public void removeProperty(MetadataKey key){
        properties.remove(key);
    }
    
    /**
     * Clears all properties from the collection, rendering it empty.
     */
    public void clearProperties(){
        properties.clear();
    }
    
    public int getBytesID() {
        return bytesID;
    }

    public void setBytesID(int bytesID) {
        this.bytesID = bytesID;
    }

    public BlobType getType() {
        return type;
    }

    public void setType(BlobType type) {
        this.type = type;
    }
    
}