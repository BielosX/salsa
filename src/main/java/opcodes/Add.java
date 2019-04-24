package opcodes;

import java.lang.annotation.*;

@Repeatable(value = Adds.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Add {
    int line();
    Registers dest();
    Registers op1();
    Registers op2();
}
