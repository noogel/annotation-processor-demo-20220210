package noogel.xyz.provider;

import java.lang.annotation.*;

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD})
public @interface Strengthen {
    Class<? extends Provider> value();

    Class<?> param();
}
