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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;
import org.nd4j.linalg.factory.Nd4jBackend.NoAvailableBackendException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Nd4jBackendTest {
    private static Logger log = LoggerFactory.getLogger(Nd4jBackendTest.class);

    @Test
    public void testPrioritization() {
        BackendBehavior behavior = new BackendBehavior();
        behavior.availabilityMap.put(Backend2.class, false);
        behavior.availabilityMap.put(Backend1.class, false);
        behaviorHolder.set(behavior);

        try {
            Nd4jBackend.load();
            fail();
        } catch (NoAvailableBackendException e) {
        }
        assertArrayEquals(new Object[] { Backend2.class, Backend1.class },
                behavior.invocationList.toArray());
    }

    @Test()
    public void testAvailability() {
        BackendBehavior behavior = new BackendBehavior();
        behavior.availabilityMap.put(Backend2.class, false);
        behavior.availabilityMap.put(Backend1.class, true);
        behaviorHolder.set(behavior);

        try {
            Nd4jBackend backend = Nd4jBackend.load();
            assertNotNull(backend);
            assertEquals(Backend1.class, backend.getClass());
        } catch (NoAvailableBackendException e) {
            fail();
        }
    }

    @Test(expected = Nd4jBackend.NoAvailableBackendException.class)
    public void testNoAvailableBackend() throws NoAvailableBackendException {
        BackendBehavior behavior = new BackendBehavior();
        behavior.availabilityMap.put(Backend2.class, false);
        behavior.availabilityMap.put(Backend1.class, false);
        behaviorHolder.set(behavior);

        Nd4jBackend.load();
    }

    private static final ThreadLocal<BackendBehavior> behaviorHolder = new ThreadLocal<>();

    private static class BackendBehavior {
        Map<Class<? extends Nd4jBackend>, Boolean> availabilityMap = new HashMap<>();
        List<Class<? extends Nd4jBackend>> invocationList = new ArrayList<>();
    }

    public static abstract class TestBackend extends Nd4jBackend {

        private int priority;

        protected TestBackend(int priority) {
            this.priority = priority;
        }

        @Override
        public int getPriority() {
            return this.priority;
        }

        @Override
        public boolean isAvailable() {
            BackendBehavior behavior = behaviorHolder.get();
            
            if(behavior == null) return false;
            assert (behavior != null);
            assert (behavior.availabilityMap.containsKey(this.getClass()));

            behavior.invocationList.add(this.getClass());
            return behavior.availabilityMap.get(this.getClass());
        }

        @Override
        public Properties getConfiguration() {
            throw new UnsupportedOperationException();
        }

    }

    public static class Backend1 extends TestBackend {
        public Backend1() {
            super(1);
        }
    }

    public static class Backend2 extends TestBackend {
        public Backend2() {
            super(2);
        }
    }

}
