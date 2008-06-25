package com.orbitz.monitoring.lib.interceptor;

import com.orbitz.monitoring.api.annotation.Monitored;

/**
 * DOCUMENT ME!
 *
 * @author Ray Krueger
 */
public class SimpleServiceImpl implements SimpleService {

    @Monitored("HELLO")
    public String sayHello(String name) {
        System.out.println("SimpleServiceImpl.sayHello");
        return "Hello " + name;
    }

    public String sayGoodbye(String name) {
        System.out.println("SimpleServiceImpl.sayGoodbye");
        return "Goodbye " + name;
    }
}
