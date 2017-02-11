package org.easyarch.slardar.mapping;

import org.easyarch.slardar.annotation.sql.Mapper;
import org.easyarch.slardar.cache.CacheFactory;
import org.easyarch.slardar.cache.InterfaceCache;
import org.easyarch.slardar.session.impl.MapperDBSession;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Description :
 * Created by xingtianyu on 17-1-24
 * 下午8:17
 * description:
 */

public class MapperProxy<T> implements InvocationHandler {

    private MapperDBSession session;

    private Class<T> interfaceClass;

    private InterfaceCache interfaceCache = CacheFactory.getInstance().getInterfaceCache();

    public MapperProxy(MapperDBSession session, Class<T> interfaceClass){
        this.session = session;
        this.interfaceClass = interfaceClass;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MappedMethod mappedMethod = new MappedMethod(session);
        String methodName = method.getName();
        if (DefaultMethod.contains(methodName)){
            return method.invoke(proxy,args);
        }
        ClassItem item = interfaceCache.get(interfaceClass);
        if (item != null){
            return mappedMethod.delegateExecute(item.getItemName(),method,args);
        }
        Mapper mapper = interfaceClass.getAnnotation(Mapper.class);
        String namespace = interfaceClass.getName();
        if (mapper != null && !mapper.namespace().isEmpty()){
            namespace = mapper.namespace();
        }
        ClassItem classItem = new ClassItem(namespace,interfaceClass,interfaceClass.getDeclaredMethods());
        interfaceCache.set(interfaceClass,classItem);
        return mappedMethod.delegateExecute(namespace,method,args);
    }
}
