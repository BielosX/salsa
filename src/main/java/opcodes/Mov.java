package opcodes;

import java.lang.annotation.*;

@Repeatable(value = Movs.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Mov {
    int line();
    Registers dest();
    int val();
}
