package org.nd4j.linalg.api.resources;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapMaker;

/**
 * Default resource manager
 * @author Adam Gibson
 */
public class DefaultResourceManager implements ResourceManager {
    private Map<String,DataBuffer> entries = new MapMaker().weakValues().concurrencyLevel(8).makeMap();
    private static Logger log = LoggerFactory.getLogger(DefaultResourceManager.class);
    private AtomicBoolean disabled = new AtomicBoolean(false);
    public DefaultResourceManager() {
    	URL url = this.getClass().getResource(NATIVE_PROPERTIES);
        if(url==null) {
            maxAllocated.set(2048);
        }
        else {
            try {
                Properties props = new Properties();
                props.load(url.openStream());
                for(String s : props.stringPropertyNames())
                    System.setProperty(s,props.getProperty(s));
                maxAllocated.set(Long.parseLong(System.getProperty(MAX_ALLOCATED,"2048")));
                memoryRatio.set(Double.parseDouble(System.getProperty(MEMORY_RATIO,"0.9")));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    @Override
    public double memoryRatio() {
        return memoryRatio.get();
    }

    @Override
    public void decrementCurrentAllocatedMemory(long decrement) {
        currentAllocated.getAndAdd(-decrement);
    }

    @Override
    public void incrementCurrentAllocatedMemory(long add) {
        currentAllocated.getAndAdd(add);
    }

    @Override
    public void setCurrentAllocated(long currentAllocated) {
        ResourceManager.currentAllocated.set(currentAllocated);
    }

    @Override
    public long maxAllocated() {
        return maxAllocated.get();
    }

    @Override
    public long currentAllocated() {
        return currentAllocated.get();
    }

    @Override
    public void remove(String id) {
        if(disabled.get())
            return;
        entries.remove(id);
    }

    @Override
    public void register(INDArray arr) {
        if(disabled.get())
            return;
        entries.put(arr.id(),arr.data());
    }

    @Override
    public void purge() {
        if(currentAllocated() > maxAllocated())
            throw new IllegalStateException("Illegal current allocated: " + currentAllocated() + " is greater than max " + maxAllocated());
       if(disabled.get())
           return;
        double ratio = Double.valueOf(currentAllocated()) / Double.valueOf(maxAllocated());
        if(ratio >= memoryRatio.get()) {
            log.trace("Amount of memory " + currentAllocated() + " out of " + maxAllocated());
            System.gc();

            for(String s : entries.keySet()) {
                try {
                    if (!entries.get(s).isPersist() && entries.get(s).references().isEmpty())
                        entries.get(s).destroy();
                }catch(Exception e) {

                }
            }

            System.runFinalization();
            log.trace("Amount after " + currentAllocated() + " out of " + maxAllocated());

        }
    }

    @Override
    public boolean shouldCollect(INDArray collect) {
        return !collect.data().isPersist() &&  collect.data().references().isEmpty();

    }

    @Override
    public void disable() {
        disabled.set(true);
    }

    @Override
    public void enable() {
        disabled.set(false);
    }

    @Override
    public boolean isEnabled() {
        return !disabled.get();
    }


}
