package opcodes;

import java.lang.annotation.*;

@Repeatable(value = CmpZeros.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CmpZero {
    int line();
    Registers reg();
}
