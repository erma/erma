package com.orbitz.monitoring.lib.processor;

import com.orbitz.monitoring.api.Attribute;
import com.orbitz.monitoring.api.Monitor;
import com.orbitz.monitoring.api.mappers.MonitorAttributeMapper;
import com.orbitz.monitoring.lib.mappers.MonitorAttributeMapperImpl;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

import org.apache.log4j.Logger;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Uses the mongo-java-driver to persist ERMA monitors to MongoDB
 *
 * @author Greg Opaczewski
 */
@ManagedResource(description = "MongoDBMonitorProcessor MBean")
public class MongoDBMonitorProcessor extends MonitorProcessorAdapter {
  private static final Logger logger = Logger.getLogger(MongoDBMonitorProcessor.class.getName());

  private String host;
  private int port;
  private String database;

  private boolean failFastOnStartup = false;
  private int bufferSize = 1024;

  private AtomicLong totalRejected = new AtomicLong(0);
  private AtomicLong totalReceived = new AtomicLong(0);
  private AtomicLong totalSampled  = new AtomicLong(0);

  private ExecutorService executor;
  private BlockingQueue enqueueBuffer;
  private boolean initialized = false;

  private MonitorAttributeMapper mapper;
  private Mongo mongo;
  private DB db;

  /* maps monitors instances into MongoDB collections */
  private NamespaceProvider namespaceProvider;
  /* defines monitor sampling logic */
  private MonitorSampler sampler;
  /* used to construct Mongo db client */
  private MongoFactory mongoFactory;


  /**
   * Construct a new processor that inserts monitor instances into the named
   * Mongo database.
   *
   * @param host server running mongod
   * @param port mongod listen port
   * @param database db name
   */
  public MongoDBMonitorProcessor(String host, int port, String database) {
    this.host = host;
    this.port = port;
    this.database = database;
  }

  @Override
  public void process(final Monitor monitor) {
    if (! initialized) { return; }

    incrementCounter(totalReceived);

    if (monitor == null) { return; }

    // sample monitors
    if (! sampler.accept(monitor)) { return; }

    incrementCounter(totalSampled);

    // keep all the work in the background thread(s) so as not to add to latency
    // of the business txn executing in the calling thread
    executor.execute(new Runnable() {
      public void run() {
        try {
          DBObject dbObject = new BasicDBObject(mapper.map(monitor));
          String ns     = namespaceProvider.getNamespaceFor(monitor);

          DBCollection collection = db.getCollection(ns);
          collection.insert(dbObject);

        } catch (MongoException e) {
          logger.debug("Mongo client threw exception while attempting to insert : " + monitor, e);
        } catch (Throwable t) {
          boolean logged = false;
          StackTraceElement[] stackTrace = t.getStackTrace();
          if (stackTrace != null && stackTrace.length > 0) {
            StackTraceElement top = stackTrace[0];
            if (top.getClassName() != null && top.getClassName().equals("org.bson.BSONEncoder")) {
              logger.info(String.format("Encoder exception caught: %s", monitor), t);
              logged = true;
            }
          }
          if (!logged) {
            logger.debug("Caught throwable processing : + " + monitor, t);
          }
        }
      }
    });
  }

  @Override
  public void startup() {
    if (namespaceProvider == null) {
      namespaceProvider = new DefaultNamespaceProvider();
    } else {
      logger.debug("Using custom NamespaceProvider : " + namespaceProvider.getClass());
    }

    executor = createThreadPoolExecutor();

    if (mongoFactory == null) {
      mongoFactory = new DefaultMongoFactory();
    }

    if (mapper == null) {
      mapper = new MonitorAttributeMapperImpl(null);
    }

    if (sampler == null) {
      sampler = new MonitorSampler() {
        public boolean accept(Monitor monitor) {
          return true;
        }
      };
    }

    try {
      mongo = mongoFactory.getMongo(host, port);
      db = mongo.getDB(database);

      initialized = true;

    } catch (UnknownHostException e) {
      handleStartupException(e);
    } catch (RuntimeException e) {
      handleStartupException(e);
    }
  }

  @Override
  public void shutdown() {
    executor.shutdown();
  }

  public void setBufferSize(int bufferSize) {
    this.bufferSize = bufferSize;
  }

  public void setFailFastOnStartup(boolean failFastOnStartup) {
    this.failFastOnStartup = failFastOnStartup;
  }

  public void setNamespaceProvider(NamespaceProvider namespaceProvider) {
    this.namespaceProvider = namespaceProvider;
  }

  public void setMongoFactory(MongoFactory mongoFactory) {
    this.mongoFactory = mongoFactory;
  }

  private void handleStartupException(Exception e) {
    /*
     * By default, failFastOnStartup is false, instead this processor will effectively
     * be disabled, allowing the rest of the MonitoringEngine to function normally
     */
    if (failFastOnStartup) {
      throw new RuntimeException("Startup of MongoDBMonitorProcessor failed", e);
    } else {
      logger.warn("Failed to initialize Mongo client, processor disabled, " 
          + "application and MonitoringEngine unaffected.", e);
    }
  }

  private ThreadPoolExecutor createThreadPoolExecutor() {
    // create bounded buffer of size _enqueueBufferSize
    enqueueBuffer = new ArrayBlockingQueue(bufferSize);

    // create policy for full buffer scenario - log a warning msg
    RejectedExecutionHandler rejectedExecutionHandler = new RejectedExecutionHandler() {
      private static final long MIN_LOG_WAIT_MILLIS = 60000;
      private long lastLogEventTime = 0;

      public void rejectedExecution(Runnable runnable,
          ThreadPoolExecutor threadPoolExecutor) {
        incrementCounter(totalRejected);
        boolean shouldLog = false;
        long currentTime = System.currentTimeMillis();

        synchronized (this) {
          if ((currentTime - lastLogEventTime) > MIN_LOG_WAIT_MILLIS) {
            lastLogEventTime = currentTime;
            shouldLog = true;
          }
        }

        if (shouldLog) {
          logger.debug("Rejected erma monitor due to full event queue.");
        }
      }
    };

    // create a thread pool of size 1 with a bounded buffer
    return new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, enqueueBuffer,
        rejectedExecutionHandler);
  }

  public void setMapper(MonitorAttributeMapper mapper) {
    this.mapper = mapper;
  }

  public void setSampler(MonitorSampler sampler) {
    this.sampler = sampler;
  }

  @ManagedAttribute(description = "Get the maximum monitor queue size")
  public int getBufferSize() {
    return bufferSize;
  }

  @ManagedAttribute(description = "Get the current monitor queue size")
  public int getMonitorQueueSize() {
    return enqueueBuffer.size();
  }

  @ManagedAttribute(description = "Get total sampled monitors")
  public long getTotalSampled() {
    return totalSampled.get();
  }

  @ManagedAttribute(description = "Get total received monitors")
  public long getTotalReceived() {
    return totalReceived.get();
  }


  @ManagedAttribute(description = "Get total rejected monitors")
  public long getTotalRejected() {
    return totalRejected.get();
  }

  /**
   * Implementing an atomic increment that prevents Long overflow
   * @param atomicLong
   */
  private void incrementCounter(AtomicLong atomicLong) {
    long currentValue;
    long newValue;

    do {
      currentValue = atomicLong.get();
      newValue = (currentValue == Long.MAX_VALUE) ? 1 : (currentValue + 1);

    } while (! atomicLong.compareAndSet(currentValue, newValue) );
  }

  public interface NamespaceProvider {
    String getNamespaceFor(Monitor monitor);
  }

  interface MongoFactory {
    Mongo getMongo(String host, int port) throws UnknownHostException;
  }

  private class DefaultNamespaceProvider implements NamespaceProvider {
    public String getNamespaceFor(Monitor monitor) {
      if (monitor.hasAttribute(Attribute.NAME)) {
        return monitor.getAsString(Attribute.NAME);
      } else {
        return "null";
      }
    }
  }

  private class DefaultMongoFactory implements MongoFactory {
    public Mongo getMongo(String host, int port) throws UnknownHostException {
      return new Mongo(host, port);
    }
  }

}
