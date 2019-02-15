package org.blue.framework.core;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 浅蓝
 * @email blue@ixsec.org
 * @since 2019/2/15 17:52
 */
public class BeanContainer extends ConcurrentHashMap<String,Object> {

    public Object getBean(String name){
        return super.get(name);
    }

    public <T> T getBean(String name,Class<T> clazz){
        return clazz.cast(super.get(name));
    }

    public Object[] getBeanByAnnotation(Class clazz){
        ArrayList<Object> objects = new ArrayList<>();
        for (Object o : values()) {
            if (o.getClass().isAnnotationPresent(clazz)){
                objects.add(o);
            }
        }
        return objects.toArray();
    }
}
