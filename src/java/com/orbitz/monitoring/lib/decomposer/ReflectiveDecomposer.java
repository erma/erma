package com.orbitz.monitoring.lib.decomposer;

import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.LazyDynaBean;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.WrapDynaBean;
import org.apache.commons.beanutils.WrapDynaClass;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.IdentityHashMap;

/**
 * Given an object, this class reflects over its JavaBean attributes and creates
 * a Serializable representation containing serializable versions of the 
 * JavaBean attributes.
 *
 * @author Doug Barth
 */
class ReflectiveDecomposer extends AbstractAttributeDecomposerStep {
    private static final Logger log = Logger.getLogger(ReflectiveDecomposer.class);
    
    private AttributeDecomposer.Step _delegate;

    public ReflectiveDecomposer(AttributeDecomposer.Step delegate) {
        _delegate = delegate;
    }

    Serializable createMutableContainer(Object o) {
        return new LazyDynaBean();
    }

    /**
     * Will decompose only if the Object is Serializable
     * @param o object to decompose
     * @param container Serializable container
     * @param alreadyDecomposed hashmap of decomposed objects
     */
    void decomposeInto(
            Object o, Serializable container, IdentityHashMap alreadyDecomposed) {

        if (!Serializable.class.isAssignableFrom(o.getClass())) return;

        LazyDynaBean decomposed = (LazyDynaBean) container;

        WrapDynaBean bean = new WrapDynaBean(o);
        WrapDynaClass dynaClass = (WrapDynaClass) bean.getDynaClass();
		DynaProperty[] properties = dynaClass.getDynaProperties();
        for (int i = 0; i < properties.length; i++) {
            DynaProperty property = properties[i];

            String name = property.getName();
            Method readMethod = dynaClass.getPropertyDescriptor(name).getReadMethod();
            if (MethodUtils.getAccessibleMethod(readMethod) != null) {
                Object beanProperty;
                try {
                    beanProperty = bean.get(name);
                } catch (RuntimeException e) {
                    throw new RuntimeException("Unable to decompose " + o.getClass().getName() + "." + name, e);
                }
                
                Serializable decomposedProperty = 
                    _delegate.decompose(beanProperty, alreadyDecomposed);
                decomposed.set(name, decomposedProperty);
            }
        }
    }
}
