/*
 * Copyright 2015 Skymind,Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.nd4j.linalg.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An ND4j backend.
 *
 */
public abstract class Nd4jBackend {

    public static final int BACKEND_PRIORITY_CPU =   0;
    public static final int BACKEND_PRIORITY_GPU = 100;

    private static final Logger log = LoggerFactory.getLogger(Nd4jBackend.class);

    /**
     * Gets a priority number for the backend.
     * 
     * Backends are loaded in priority order (highest first).
     * @return a priority number.
     */
    public abstract int getPriority();

    /**
     * Determines whether a given backend is available in the current environment.
     * @return true if the backend is available; false otherwise.
     */
    public abstract boolean isAvailable();


    public abstract Properties getConfiguration();

    /**
     * Loads the best available backend.
     * @return
     */
    public static Nd4jBackend load() throws NoAvailableBackendException {

        List<Nd4jBackend> backends = new ArrayList<>(1);
        ServiceLoader<Nd4jBackend> loader = ServiceLoader.load(Nd4jBackend.class);
        try {
            Iterator<Nd4jBackend> backendIterator = loader.iterator();
            while (backendIterator.hasNext()) {
                backends.add(backendIterator.next());
            }
        } catch (ServiceConfigurationError serviceError) {
            // a fatal error due to a syntax or provider construction error.
            // backends mustn't throw an exception during construction.
            throw new RuntimeException("failed to process available backends", serviceError);
        }

        Collections.sort(backends, new Comparator<Nd4jBackend>() {
            @Override
            public int compare(Nd4jBackend o1, Nd4jBackend o2) {
                // high-priority first
                return o2.getPriority() - o1.getPriority();
            }
        });

        for(Nd4jBackend backend: backends) {
            if(!backend.isAvailable()) {
                log.trace("Skipped [{}] backend (unavailable)", backend.getClass().getSimpleName());
                continue;
            }

            log.trace("Loaded [{}] backend", backend.getClass().getSimpleName());
            return backend;
        }

        throw new NoAvailableBackendException();
    }

    @SuppressWarnings("serial")
    public static class NoAvailableBackendException extends Exception {}
}
