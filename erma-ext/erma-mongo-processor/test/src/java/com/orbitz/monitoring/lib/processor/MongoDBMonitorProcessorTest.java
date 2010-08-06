package com.orbitz.monitoring.lib.processor;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import junit.framework.AssertionFailedError;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
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


/**
 * MongoDBMonitorProcessor test cases
 *
 * @author Greg Opaczewski
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest( { com.mongodb.DB.class })
public class MongoDBMonitorProcessorTest  {

	private MongoDBMonitorProcessor processor;
	private DB mockDB;
	private DBCollection mockCollection;

	@SuppressWarnings("unchecked")
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

		ExecutorService mockExecutor = mock(ExecutorService.class);

		Mockito.doAnswer(new Answer() {

			public Object answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				Runnable command = (Runnable)args[0];
				command.run();

				return null;
			}
		}).when(mockExecutor).execute((Runnable)anyObject());

		processor.setExecutor(mockExecutor);
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

		ExecutorService mockExecutor = mock(ExecutorService.class);
		Mockito.doThrow(new AssertionFailedError()).when(mockExecutor).execute((Runnable) anyObject());

		// this will result in a test failure if the processor actually tries to process anything
		// after a failed startup
		mp.setExecutor(mockExecutor);
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

		ExecutorService mockExecutor = mock(ExecutorService.class);
		Mockito.doThrow(new AssertionFailedError()).when(mockExecutor).execute((Runnable) anyObject());

		// this will result in a test failure if the processor actually tries to process anything
		// after a failed startup
		mp.setExecutor(mockExecutor);
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
		assertTrue(json.contains("bKey"));
	}

	@Test
	public void testAttributeMapper()
	throws Exception
	{
		processor.startup();
		processor.setMapper(new MonitorAttributeMapper() {

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

		processor.process(monitor);

		verify(mockCollection).insert(argument.capture());

		BasicDBObject dbObject = (BasicDBObject)argument.getValue();

		assertEquals("the_id", foo.getId(), dbObject.getLong("the_id"));
		assertEquals("the_name", foo.getName(), dbObject.get("the_name"));

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
}
