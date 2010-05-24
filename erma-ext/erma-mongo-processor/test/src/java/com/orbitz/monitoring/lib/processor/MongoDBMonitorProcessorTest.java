package com.orbitz.monitoring.lib.processor;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.orbitz.monitoring.api.monitor.EventMonitor;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static junit.framework.Assert.*;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;


/**
 * MongoDBMonitorProcessor test cases
 *
 * @author Greg Opaczewski
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest( { com.mongodb.DB.class })
public class MongoDBMonitorProcessorTest {

    private MongoDBMonitorProcessor processor;
    private DB mockDB;
    private DBCollection mockCollection;

    @Before
    public void setUp() {
        processor = new MongoDBMonitorProcessor("localhost", 27017, "test");

        final Mongo mockMongo = mock(Mongo.class);

        // must use PowerMock here because DB.getCollection is final
        mockDB = mock(DB.class);
        mockCollection = mock(DBCollection.class);

        when(mockMongo.getDB("test")).thenReturn(mockDB);
        when(mockDB.getCollection(anyString())).thenReturn(mockCollection);

        processor.setMongoFactory(new MongoDBMonitorProcessor.MongoFactory() {
            public Mongo getMongo(String host, int port) throws UnknownHostException {
                return mockMongo;
            }
        });

        processor.setExecutor(new SynchronousExecutor());
        processor.setFailFastOnStartup(true);
    }

    @After
    public void tearDown() {
        processor.shutdown();    
    }

    @Test
    public void testStartupExceptionIgnored() {
        MongoDBMonitorProcessor mp = new MongoDBMonitorProcessor("localhost", 27017, "test");

        final Mongo mockMongo = mock(Mongo.class);

        when(mockMongo.getDB("test")).thenThrow(new MongoException("Mongo client exception"));

        mp.setMongoFactory(new MongoDBMonitorProcessor.MongoFactory() {
            public Mongo getMongo(String host, int port) throws UnknownHostException {
                return mockMongo;
            }
        });

        // this will result in a test failure if the processor actually tries to process anything
        // after a failed startup
        mp.setExecutor(new SynchronousExecutor(true));

        mp.setFailFastOnStartup(false);
        mp.startup();

        // monitor will just be ignored
        mp.process(new EventMonitor("foo"));
    }

    @Test
    public void testStartupExceptionRethrown() {
        MongoDBMonitorProcessor mp = new MongoDBMonitorProcessor("localhost", 27017, "test");

        final Mongo mockMongo = mock(Mongo.class);

        when(mockMongo.getDB("test")).thenThrow(new MongoException("Mongo client exception"));

        mp.setMongoFactory(new MongoDBMonitorProcessor.MongoFactory() {
            public Mongo getMongo(String host, int port) throws UnknownHostException {
                return mockMongo;
            }
        });

        // this will result in a test failure if the processor actually tries to process anything
        // after a failed startup
        mp.setExecutor(new SynchronousExecutor(true));

        mp.setFailFastOnStartup(true);

        try {
            mp.startup();
            fail("startup should throw RTE");
        } catch (RuntimeException e) {
            // expected            
        }
    }

    @Test
    public void testNulls() {
        processor.startup();
        processor.process(null);

        EventMonitor monitor = new EventMonitor(null);
        monitor.set("foo", (String)null);
        processor.process(monitor);
    }

    @Test
    public void testProcessSimpleMonitor() {
        processor.startup();

        processor.process(new EventMonitor("foo"));

        verify(mockDB, times(1)).getCollection("foo");
        verify(mockCollection, times(1)).insert(isA(DBObject.class));
    }

    @Test
    public void testProcessMultipleMonitors() {
        processor.startup();

        processor.process(new EventMonitor("foo"));
        processor.process(new EventMonitor("bar"));
        processor.process(new TransactionMonitor("baz"));

        verify(mockDB, times(3)).getCollection(anyString());
        verify(mockCollection, times(3)).insert(isA(DBObject.class));
    }

    @Test
    public void testAsyncProcess() throws Exception {
        final int n = 100;
        CountDownLatch latch = new CountDownLatch(n);

        processor.setExecutor(new TaskCountingExecutor(4, latch));
        processor.startup();

        for (int i=0; i < n; i++) {
            processor.process(new EventMonitor("foo"));
        }

        latch.await();

        verify(mockDB, times(n)).getCollection("foo");
        verify(mockCollection, times(n)).insert(isA(DBObject.class));
    }

    @Test
    public void testCustomNamespaceProvider() {
        processor.setAttributeFilter(new MongoDBMonitorProcessor.AttributeFilter() {
            @Override
            public boolean includeAttribute(String key, Object value) {
                return value.toString().startsWith("a");
            }
        });
        processor.startup();

        EventMonitor m = new EventMonitor("aName");
        m.set("aKey", "aValue");
        m.set("bKey", "bValue");

        ArgumentCaptor<DBObject> argument = ArgumentCaptor.forClass(DBObject.class);

        processor.process(m);

        verify(mockCollection).insert(argument.capture());

        // BasicDBObject.toString() yields a json rendering of the object
        String json = argument.getValue().toString();
        
        assertTrue(json.contains("aKey"));
        assertTrue(json.contains("aName"));
        assertFalse(json.contains("bKey"));
    }

    @Test
    public void testDefaultAttributeFilter() {
        processor.startup();
        
        TransactionMonitor tm = new TransactionMonitor("tm");

        tm.set("aKey", "aValue");
        tm.set("someCount", 100);
        tm.set("bool", true);
        tm.set("someFloat", 10.0f);
        tm.set("someDouble", 20.0d);
        tm.set("someDate", new Date());

        tm.set("excludeObject", new Object());
        tm.set("excludeCollection", new ArrayList());
        tm.set("excludeArray", new String[] {});

        ArgumentCaptor<DBObject> argument = ArgumentCaptor.forClass(DBObject.class);

        processor.process(tm);

        verify(mockCollection).insert(argument.capture());

        // BasicDBObject.toString() yields a json rendering of the object
        String json = argument.getValue().toString();

        String[] includedKeys = new String[] {"aKey", "someCount", "bool", "someFloat", "someDouble", "someDate"};
        String[] excludedKeys = new String[] {"excludeObject", "excludeCollection", "excludeArray"};

        for (String k : includedKeys) {
            assertTrue(json.contains(k));
        }

        for (String k : excludedKeys) {
            assertFalse(json.contains(k));
        }
    }

    private class TaskCountingExecutor extends ThreadPoolExecutor {
        private CountDownLatch latch;

        TaskCountingExecutor(int corePoolSize, CountDownLatch latch) {
            super(corePoolSize, corePoolSize, 0, TimeUnit.SECONDS, new LinkedBlockingQueue());
            this.latch = latch;
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            latch.countDown();
        }
    }

    private class SynchronousExecutor implements ExecutorService {
        private boolean failOnRun = false;

        public SynchronousExecutor() {}

        public SynchronousExecutor(boolean failOnRun) {
            this.failOnRun = failOnRun;
        }

        @Override
        public void execute(Runnable command) {
            if (failOnRun) {
                fail("Should not be reached");
            } else {
                command.run();
            }
        }

        @Override
        public void shutdown() {}

        @Override
        public List<Runnable> shutdownNow() {
            return null;
        }

        @Override
        public boolean isShutdown() {
            return false;
        }

        @Override
        public boolean isTerminated() {
            return false;
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            return false;
        }

        @Override
        public <T> Future<T> submit(Callable<T> task) {
            return null;
        }

        @Override
        public <T> Future<T> submit(Runnable task, T result) {
            return null;
        }

        @Override
        public Future<?> submit(Runnable task) {
            return null;
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
            return null;
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
            return null;
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
            return null;
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return null;
        }
    }

}
