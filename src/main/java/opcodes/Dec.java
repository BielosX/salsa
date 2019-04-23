package opcodes;

import java.lang.annotation.*;

@Repeatable(value = Decs.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Dec {
    int line();
    Registers reg();
    int value();
}
