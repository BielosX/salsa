package opcodes;

import java.lang.annotation.*;

@Repeatable(value = Jnzs.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Jnz {
    int line();
    int jumpTo();
}
