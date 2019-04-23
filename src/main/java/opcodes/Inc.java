package opcodes;

import java.lang.annotation.*;

@Repeatable(value = Incs.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Inc {
    int line();
    Registers reg();
    int value();
}
