package org.blue.framework.core;

import org.blue.framework.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 浅蓝
 * @email blue@ixsec.org
 * @since 2019/2/15 15:38
 */
public class DispatcherServerlet extends HttpServlet {

    private Properties properties;

    private BeanContainer beanContainer = new BeanContainer();

    private Map<String,Map<String,Method>> handlerMapping = new ConcurrentHashMap<>();

    private ClassLoader contextClassLoader;

    private List<String> classNames = new ArrayList<>();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String uri = req.getRequestURI();

        if (uri.contains("?")){
            uri = uri.substring(0,uri.indexOf("?"));
        }

        if (handlerMapping.containsKey(uri)){

            Map<String, Method> map = handlerMapping.get(uri);
            for (Map.Entry<String, Method> entry : map.entrySet()) {
                if (beanContainer.containsKey(entry.getKey())){
                    Object o = beanContainer.get(entry.getKey());
                    Method method = entry.getValue();
                    try {
                        Object invoke = method.invoke(o, req);

                        resp.getWriter().print(invoke);

                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }

                }


            }


        }

    }

    @Override
    public void init(ServletConfig config) throws ServletException {

        initProperties(config.getInitParameter("configLocation"));

        initScanner();

        initInjection();

        initHandlerMapping();

    }

    private void initHandlerMapping() {

        for (Map.Entry<String, Object> bean : beanContainer.entrySet()) {

            Class<?> clazz = bean.getValue().getClass();

            if (clazz.isAnnotationPresent(RequestMapping.class)){

                RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);

                String value = requestMapping.value();


                for (Method method : clazz.getMethods()) {

                    if (method.isAnnotationPresent(RequestMapping.class)){
                        RequestMapping methodMapping = method.getAnnotation(RequestMapping.class);
                        String methodValue = methodMapping.value();
                        String mapping = value.concat("/"+methodValue);
                        HashMap<String, Method> map = new HashMap<>();
                        map.put(bean.getKey(),method);
                        handlerMapping.put(mapping , map);
                    }

                }


            }
        }


    }

    private void initInjection() {

        if (!beanContainer.isEmpty()){

            for (Map.Entry<String, Object> bean : beanContainer.entrySet()) {

                //String beanName = bean.getKey();
                Object beanObject = bean.getValue();

                for (Field field : beanObject.getClass().getDeclaredFields()) {

                    if (!field.isAnnotationPresent(Autowired.class)){
                        continue;
                    }

                    field.setAccessible(true);
                    Autowired autowired = field.getAnnotation(Autowired.class);
                    String value = autowired.value().trim();
                    String beanName = value !=null && !value.isEmpty() ? value :field.getName();

                    if (!beanContainer.containsKey(beanName)){
                        new Throwable().printStackTrace();
                    }

                    Object o = beanContainer.get(beanName);

                    try {
                        field.set(beanObject,o);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }


            }


        }

    }

    private void initScanner() {
        String scanPackage;
        System.out.println(properties.getProperty("scanPackage"));
        if ( (scanPackage=properties.getProperty("scanPackage"))!=null){

            scanPackage = scanPackage.replace('.','/');

            handleScanner(scanPackage);

            if (!classNames.isEmpty()){
                initRegister();
            }

        }

    }


    private void initRegister() {
        for (String className : classNames) {

            try {
                Class<?> clazz = contextClassLoader.loadClass(className);

                if (clazz.isAnnotationPresent(Component.class)){

                    Component component = clazz.getAnnotation(Component.class);
                    String value = component.value();
                    String simpleName = clazz.getSimpleName().substring(0,1).toLowerCase()+clazz.getSimpleName().substring(1,clazz.getSimpleName().length());
                    String beanName = value!=null && !value.isEmpty() ? value : simpleName;

                    Object object = clazz.newInstance();

                    beanContainer.put(beanName,object);

                    System.out.println("正在注册"+beanName);
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }

        }
    }

    private void handleScanner(String packName){
        URL resource = contextClassLoader.getResource(packName);

        File resourceFile = new File(resource.getFile());

        if (resourceFile.exists() && resourceFile.isDirectory()){

            File[] files = resourceFile.listFiles();

            for (File file : files) {

                if (file.isDirectory()){
                    handleScanner(packName+"/"+file.getName());
                }else if(file.getName().endsWith(".class")){

                    String className = file.getName().replaceAll("\\.class", "");
                    classNames.add(packName.replace('/','.')+"."+className);

                }

            }
        }
    }


    private void initProperties(String configLocation) {
        properties = new Properties();
        if (properties!=null){
            try {
                InputStream propFile = this.getClass().getClassLoader().getResourceAsStream(configLocation);

                properties.load(propFile);

                contextClassLoader = this.getClass().getClassLoader();
            }catch (Exception e){
                e.printStackTrace();
                return;
            }

        }

    }
}
