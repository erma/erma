package com.orbitz.monitoring.lib.processor;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.lang.reflect.Field;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import junit.framework.AssertionFailedError;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.mappers.MonitorAttributeMapper;
import com.orbitz.monitoring.api.monitor.EventMonitor;
import com.orbitz.monitoring.api.monitor.TransactionMonitor;
import com.orbitz.monitoring.lib.processor.MongoDBMonitorProcessor.NamespaceProvider;
import java.math.BigDecimal;


/**
 * MongoDBMonitorProcessor test cases
 *
 * @author Greg Opaczewski
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest( { com.mongodb.DB.class })
public class MongoDBMonitorProcessorTest  {

    private Logger log = Logger.getLogger(getClass());

    private MongoDBMonitorProcessor mongoProcessor;
    private DB mockDB;
    private DBCollection mockCollection;
    private ExecutorService mockMongoProcessorExecutor;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        mockMongoProcessorExecutor = mock(ThreadPoolExecutor.class);
        Mockito.doAnswer(new Answer() {

            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                Runnable command = (Runnable)args[0];
                command.run();

                return null;
            }
        }).when(mockMongoProcessorExecutor).execute((Runnable)anyObject());

        when(mockMongoProcessorExecutor.awaitTermination(anyLong(), (TimeUnit) anyObject())).thenReturn(true);

        mongoProcessor = createProcessor();
        mongoProcessor.startup();
        setFieldValue(mongoProcessor, "executor", mockMongoProcessorExecutor);
    }

    @After
    public void tearDown() {
        mongoProcessor.shutdown();
    }

    @Test
    public void testEncodingExceptionLogging()
    throws Exception {
        final EventMonitor monitor = new EventMonitor("fooMonitor");

        monitor.set("foo", "bar");

        final RuntimeException exception = new RuntimeException();
        exception.setStackTrace(new StackTraceElement[] {
            new StackTraceElement("org.bson.BSONEncoder", "putNumber", "BSONEncoder.java", 269),
          });

        final MonitorSampler sampler = new PercentageMonitorSampler(100);
        final MonitorAttributeMapper bigDecimalMapper = new MonitorAttributeMapper() {
            public Map<String, Object> map(Monitor monitor) {
              throw exception;
            }
          };

        mongoProcessor.setMapper(bigDecimalMapper);
        mongoProcessor.startup();
        mongoProcessor.setSampler(sampler);
        mongoProcessor.process(monitor);
    }


    @Test
    public void testMongoProcessorMongoExceptionHandling()
    throws Exception {
        final EventMonitor monitor = new EventMonitor("fooMonitor");

        monitor.set("foo", "fooValue");

        final MonitorSampler sampler = new PercentageMonitorSampler(100);
        final NamespaceProvider mongoExceptionNameSpaceProvider = new NamespaceProvider() {

            public String getNamespaceFor(Monitor monitor) {
                throw new MongoException("mongo exception");
            }
        };

        mongoProcessor.setNamespaceProvider(mongoExceptionNameSpaceProvider);
        mongoProcessor.startup();
        mongoProcessor.setSampler(sampler);
        mongoProcessor.process(monitor);
    }

    @Test
    public void testMongoProcessorThrowableHandling()
    throws Exception {
        final EventMonitor monitor = new EventMonitor("fooMonitor");

        monitor.set("foo", "fooValue");

        final MonitorSampler sampler = new PercentageMonitorSampler(100);
        final NamespaceProvider runtimeExceptionNameSpaceProvider = new NamespaceProvider() {

            public String getNamespaceFor(Monitor monitor) {
                throw new RuntimeException("runtime exception");
            }
        };

        mongoProcessor.setNamespaceProvider(runtimeExceptionNameSpaceProvider);
        mongoProcessor.startup();
        mongoProcessor.setSampler(sampler);
        mongoProcessor.process(monitor);
    }

    @Test
    public void testStartupExceptionIgnored()
    throws Exception {
        MongoDBMonitorProcessor processor = new MongoDBMonitorProcessor("localhost", 27017, "test");

        final Mongo mockMongo = mock(Mongo.class);

        when(mockMongo.getDB("test")).thenThrow(new MongoException("Mongo client exception"));

        processor.setMongoFactory(new MongoDBMonitorProcessor.MongoFactory() {
            public Mongo getMongo(String host, int port) throws UnknownHostException {
                return mockMongo;
            }
        });

        ExecutorService mockExecutor = mock(ThreadPoolExecutor.class);
        Mockito.doThrow(new AssertionFailedError()).when(mockExecutor).execute((Runnable) anyObject());

        // this will result in a test failure if the processor actually tries to process anything
        // after a failed startup
        processor.setFailFastOnStartup(false);
        processor.startup();
        setFieldValue(processor, "executor", mockExecutor);

        // monitor will just be ignored
        processor.process(new EventMonitor("foo"));
    }

    @Test(expected=RuntimeException.class)
    public void testStartupExceptionRethrown()
    throws Exception {
        MongoDBMonitorProcessor processor = new MongoDBMonitorProcessor("localhost", 27017, "test");

        final Mongo mockMongo = mock(Mongo.class);

        when(mockMongo.getDB("test")).thenThrow(new MongoException("Mongo client exception"));

        processor.setMongoFactory(new MongoDBMonitorProcessor.MongoFactory() {
            public Mongo getMongo(String host, int port) throws UnknownHostException {
                return mockMongo;
            }
        });

        // this will result in a test failure if the processor actually tries to process anything
        // after a failed startup
        processor.setFailFastOnStartup(true);

        processor.startup();
    }

    @Test
    public void testNullMonitor()
    throws Exception {
        mongoProcessor.process(null);
    }

    @Test
    public void testRejectedMonitorCounter()
    throws Exception {
        final EventMonitor monitor = new EventMonitor("fooMonitor");
        final int totalRuns        = 10;
        final int queueSize        = 5;
        final int expectedRejects  = totalRuns - queueSize;
        final Object lock          = new Object();

        monitor.set("foo", "fooValue");

        final MonitorSampler sampler = new PercentageMonitorSampler(100);
        final MongoDBMonitorProcessor processor = createProcessor(queueSize);

        processor.startup();
        processor.setSampler(sampler);

        final ExecutorService mongoProcessorExecutor = (ExecutorService) getFieldValue(processor, "executor");
        mongoProcessorExecutor.execute(new Runnable() {

            public void run() {
                synchronized (lock) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        });

        for (int i=0; i< totalRuns; i++) {
            processor.process(monitor);
        }

        assertEquals("current queue size", queueSize, processor.getMonitorQueueSize());
        synchronized (lock) {
            lock.notifyAll();
        }
        waitForExecutor(mongoProcessor);
        assertEquals("num of rejected monitors", expectedRejects, processor.getTotalRejected());
    }

    @Test
    public void testReceivedMonitorCounter()
    throws Exception {
        final EventMonitor monitor = new EventMonitor("fooMonitor");
        final int totalRuns        = 10;
        final int numOfThreads     = 3;
        final int expectedReceived = 30;

        monitor.set("foo", "fooValue");

        MonitorSampler sampler = new PercentageMonitorSampler(5);
        mongoProcessor.setSampler(sampler);

        ExecutorService threadPool = Executors.newFixedThreadPool(numOfThreads);

        for (int i=0; i< numOfThreads;i++) {
            threadPool.submit(new Runnable() {

                public void run() {
                    for (int i=0; i< totalRuns; i++) {
                        mongoProcessor.process(monitor);
                    }
                }
            });
        }

        threadPool.shutdown();
        threadPool.awaitTermination(5*60, TimeUnit.SECONDS);
        assertEquals("num of received monitors", expectedReceived, mongoProcessor.getTotalReceived());

    }

    @Test
    public void testSampledMonitorCounter()
    throws Exception {
        final EventMonitor monitor = new EventMonitor("fooMonitor");
        final int totalRuns        = 10;
        final int numOfThreads     = 4;
        final int expectedSampled  = 30;

        monitor.set("foo", "fooValue");

        MonitorSampler sampler = new PercentageMonitorSampler(75);

        mongoProcessor.setSampler(sampler);

        ExecutorService threadPool = Executors.newFixedThreadPool(numOfThreads);

        for (int i=0; i< numOfThreads;i++) {
            threadPool.submit(new Runnable() {

                public void run() {
                    for (int i=0; i< totalRuns; i++) {
                        mongoProcessor.process(monitor);
                    }
                }
            });
        }

        threadPool.shutdown();
        threadPool.awaitTermination(5*60, TimeUnit.SECONDS);
        assertEquals("num of sampled monitors", expectedSampled, mongoProcessor.getTotalSampled());
    }

    @Test
    public void testSampledMonitorCounterOverflow()
    throws Exception {
        final EventMonitor monitor = new EventMonitor("fooMonitor");
        final int totalRuns        = 10;
        final int numOfThreads     = 4;
        final int offset           = 10;
        final int expectedSampled  = 30 - offset;

        monitor.set("foo", "fooValue");

        MonitorSampler sampler = new PercentageMonitorSampler(75);

        mongoProcessor.setSampler(sampler);
        setFieldValue(mongoProcessor, "totalSampled", new AtomicLong(Long.MAX_VALUE-offset));

        ExecutorService threadPool = Executors.newFixedThreadPool(numOfThreads);

        for (int i=0; i< numOfThreads;i++) {
            threadPool.submit(new Runnable() {

                public void run() {
                    for (int i=0; i< totalRuns; i++) {
                        mongoProcessor.process(monitor);
                    }
                }
            });
        }

        threadPool.shutdown();
        threadPool.awaitTermination(5*60, TimeUnit.SECONDS);
        assertEquals("num of sampled monitors", expectedSampled, mongoProcessor.getTotalSampled());
    }

    @Test
    public void testProcessSimpleMonitor()
    throws Exception {
        mongoProcessor.process(new EventMonitor("foo"));

        verify(mockDB, times(1)).getCollection("foo");
        verify(mockCollection, times(1)).insert(isA(DBObject.class));
    }

    @Test
    public void testProcessMultipleMonitors()
    throws Exception {
        mongoProcessor.process(new EventMonitor("foo"));
        mongoProcessor.process(new EventMonitor("bar"));
        mongoProcessor.process(new TransactionMonitor("baz"));

        verify(mockDB, times(3)).getCollection(anyString());
        verify(mockCollection, times(3)).insert(isA(DBObject.class));
    }

    @Test
    public void testAsyncProcess() throws Exception {
        final int n = 100;
        CountDownLatch latch = new CountDownLatch(n);

        //processor.setExecutor(new TaskCountingExecutor(4, latch));
        setFieldValue(mongoProcessor, "executor", new TaskCountingExecutor(4, latch));

        for (int i=0; i < n; i++) {
            mongoProcessor.process(new EventMonitor("foo"));
        }

        latch.await();

        verify(mockDB, times(n)).getCollection("foo");
        verify(mockCollection, times(n)).insert(isA(DBObject.class));
    }

    @Test
    public void testCustomNamespaceProvider() throws Exception {
        EventMonitor m = new EventMonitor("aName");
        m.set("aKey", "aValue");
        m.set("bKey", "bValue");

        ArgumentCaptor<DBObject> argument = ArgumentCaptor.forClass(DBObject.class);

        mongoProcessor.process(m);

        verify(mockCollection).insert(argument.capture());

        // BasicDBObject.toString() yields a json rendering of the object
        String json = argument.getValue().toString();

        assertTrue(json.contains("aKey"));
        assertTrue(json.contains("aName"));
        assertTrue(json.contains("bKey"));
    }

    @Test
    public void testAttributeMapper()
    throws Exception {
        mongoProcessor.setMapper(new MonitorAttributeMapper() {

            public Map<String, Object> map(Monitor k) {
                ErmaComplexObject o     = (ErmaComplexObject) k.get("foo");
                Map<String, Object> map = new HashMap<String, Object>();

                map.put("the_id", o.getId());
                map.put("the_name", o.getName());

                return map;
            }
        });

        ErmaComplexObject foo = ErmaComplexObject.newRandomInstance();
        EventMonitor monitor  = new EventMonitor(null);

        monitor.set("foo", foo);

        ArgumentCaptor<DBObject> argument = ArgumentCaptor.forClass(DBObject.class);

        mongoProcessor.process(monitor);

        verify(mockCollection).insert(argument.capture());

        BasicDBObject dbObject = (BasicDBObject)argument.getValue();

        assertEquals("the_id", foo.getId(), dbObject.getLong("the_id"));
        assertEquals("the_name", foo.getName(), dbObject.get("the_name"));

    }

    @Test
    public void testMonitorWithNoNameAttribute()
    throws Exception {
        mongoProcessor.setMapper(new MonitorAttributeMapper() {

            public Map<String, Object> map(Monitor k) {
                return new HashMap<String, Object>();
            }
        });

        ErmaComplexObject foo = ErmaComplexObject.newRandomInstance();
        Monitor monitor  = mock(Monitor.class);

        when(monitor.hasAttribute(anyString())).thenReturn(false);

        mongoProcessor.process(monitor);

        final ExecutorService mongoProcessorExecutor = (ExecutorService) getFieldValue(mongoProcessor, "executor");
        mongoProcessorExecutor.shutdown();
        mongoProcessorExecutor.awaitTermination(100, TimeUnit.SECONDS);

        verify(mockDB).getCollection("null");

    }

    public static class ErmaComplexObject {
        private int id;
        private String name;
        public static ErmaComplexObject newRandomInstance() {
            ErmaComplexObject obj = new ErmaComplexObject();

            obj.id   = RandomUtils.nextInt();
            obj.name = RandomStringUtils.randomAlphabetic(10);

            return obj;
        }
        public int getId() {
            return id;
        }
        public void setId(int id) {
            this.id = id;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
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


    private Field makeFieldAccessible(final Object o, final String fieldName)
    throws Exception {
        Field field = null;
        field = o.getClass().getDeclaredField(fieldName);
        if (null != field) {
            field.setAccessible(true);
        }
        return field;
    }

    private Object setFieldValue(final Object o, final String fieldName, Object newValue)
    throws Exception {
        Field field = makeFieldAccessible(o, fieldName);
        field.set(o, newValue);
        return newValue;
    }

    private Object getFieldValue(final Object o, final String fieldName)
    throws Exception {
        Field field = makeFieldAccessible(o, fieldName);
        Object value = field.get(o);
        return value;
    }

    private void waitForExecutor(MongoDBMonitorProcessor processor)
    throws Exception {
        ExecutorService mongoProcessorExecutor = (ExecutorService) getFieldValue(processor, "executor");
        mongoProcessorExecutor.shutdown();
        if (!mongoProcessorExecutor.awaitTermination(120, TimeUnit.SECONDS)) {
            fail("timeout occured waiting for processor executor to complete");
        }
    }

    private MongoDBMonitorProcessor createProcessor() {
        return createProcessor(1024);
    }

    private MongoDBMonitorProcessor createProcessor(int bufferSize) {
        MongoDBMonitorProcessor processor = new MongoDBMonitorProcessor("localhost", 27017, "test");
        processor.setBufferSize(bufferSize);

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

        processor.setFailFastOnStartup(true);

        return processor;
    }
}
