package org.blue.framework.annotation;

import java.lang.annotation.*;

/**
 * @author 浅蓝
 * @email blue@ixsec.org
 * @since 2019/2/15 15:39
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {
    String value() default "";

}
