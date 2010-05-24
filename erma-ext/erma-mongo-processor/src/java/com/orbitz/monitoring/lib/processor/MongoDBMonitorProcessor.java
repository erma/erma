package com.orbitz.monitoring.lib.processor;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.orbitz.monitoring.api.Attribute;
import com.orbitz.monitoring.api.Monitor;
import org.apache.log4j.Logger;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Uses the mongo-java-driver to persist ERMA monitors to MongoDB
 *
 * @author Greg Opaczewski
 */
public class MongoDBMonitorProcessor extends MonitorProcessorAdapter {
    private static final Logger logger = Logger.getLogger(MongoDBMonitorProcessor.class.getName());

    private String host;
    private int port;
    private String database;

    private boolean failFastOnStartup = false;
    private int bufferSize = 1024;

    private ExecutorService executor;
    private boolean initialized = false;

    private Mongo mongo;
    private DB db;

    /* maps monitors instances into MongoDB collections */
    private NamespaceProvider namespaceProvider;
    /* used to filter the attribute set on each monitor before inserting to db */
    private AttributeFilter attributeFilter;
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
        if (! initialized) return;
        if (monitor == null) return;

        // keep all the work in the background thread(s) so as not to add to latency
        // of the business txn executing in the calling thread
        executor.execute(new Runnable() {
            public void run() {
                try {
                    DBObject dbObject = toDBObject(monitor);
                    String ns = namespaceProvider.getNamespaceFor(monitor);

                    DBCollection collection = db.getCollection(ns);
                    collection.insert(dbObject);

                } catch (MongoException e) {
                    logger.debug("Mongo client threw exception while attempting to insert : " + monitor, e);
                } catch (Throwable t) {
                    logger.debug("Caught throwable processing : + " + monitor, t);
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

        if (attributeFilter == null) {
            attributeFilter = new DefaultAttributeFilter();
        } else {
            logger.debug("Using custom AttributeFilter : " + attributeFilter.getClass());
        }

        if (executor == null) {
            executor = createThreadPoolExecutor();
        } else {
            logger.debug("Using custom executor : " + executor.getClass());
        }

        if (mongoFactory == null) {
            mongoFactory = new DefaultMongoFactory();
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

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    public void setNamespaceProvider(NamespaceProvider namespaceProvider) {
        this.namespaceProvider = namespaceProvider;
    }

    public void setAttributeFilter(AttributeFilter attributeFilter) {
        this.attributeFilter = attributeFilter;
    }

    public void setMongoFactory(MongoFactory mongoFactory) {
        this.mongoFactory = mongoFactory;
    }

    // helper classes

    public interface NamespaceProvider {
        String getNamespaceFor(Monitor monitor);
    }

    public interface AttributeFilter {
        boolean includeAttribute(String key, Object value);
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

    private class DefaultAttributeFilter implements AttributeFilter {
        Set<Class> allowedValueClasses = new HashSet<Class>();

        public DefaultAttributeFilter() {
            Class[] clazzes = new Class[] { String.class, Boolean.class, Date.class };
            allowedValueClasses = new HashSet<Class>(Arrays.asList(clazzes));
        }

        @Override
        public boolean includeAttribute(String key, Object value) {
            if (value == null) {
                return true;
            }

            Class clazz = value.getClass();
            if (Number.class.isAssignableFrom(clazz)) {
                return true;
            }

            return allowedValueClasses.contains(clazz);
        }
    }

    private class DefaultMongoFactory implements MongoFactory {
        @Override
        public Mongo getMongo(String host, int port) throws UnknownHostException {
            return new Mongo(host, port);
        }
    }

    // private methods

    private void handleStartupException(Exception e) {
        /*
         * By default, failFastOnStartup is false, instead this processor will effectively
         * be disabled, allowing the rest of the MonitoringEngine to function normally
         */
        if (failFastOnStartup) {
            throw new RuntimeException("Startup of MongoDBMonitorProcessor failed", e);
        } else {
            logger.warn("Failed to initialize Mongo client, processor disabled, " +
                    "application and MonitoringEngine unaffected.", e);
        }
    }
    
    private DBObject toDBObject(Monitor monitor) {
        BasicDBObject dbObject = new BasicDBObject();
        Map allAttributes = monitor.getAll();

        for (Object o : allAttributes.entrySet()) {
            Map.Entry attr = (Map.Entry) o;
            String key = (String) attr.getKey();
            Object value = attr.getValue();

            if (attributeFilter.includeAttribute(key, value)) {
                dbObject.put(key, value);
            }
        }

        return dbObject;
    }

    private ThreadPoolExecutor createThreadPoolExecutor() {
        // create bounded buffer of size _enqueueBufferSize
        BlockingQueue enqueueBuffer = new ArrayBlockingQueue(bufferSize);

        // create policy for full buffer scenario - log a warning msg
        RejectedExecutionHandler rejectedExecutionHandler = new RejectedExecutionHandler() {
            private static final long MIN_LOG_WAIT_MILLIS = 60000;
            private long lastLogEventTime = 0;

            public void rejectedExecution(Runnable runnable,
                                          ThreadPoolExecutor threadPoolExecutor) {
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

}
