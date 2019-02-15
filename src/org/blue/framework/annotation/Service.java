package org.blue.framework.annotation;

import java.lang.annotation.*;

/**
 * @author 浅蓝
 * @email blue@ixsec.org
 * @since 2019/2/15 15:39
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Service {
    String value() default "";
}
