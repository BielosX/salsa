import com.google.common.collect.ImmutableMap;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import opcodes.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static opcodes.Registers.R1;

@EqualsAndHashCode
public class VirtualMachine {
    private final static String MAIN_METHOD = "start";
    private final Map<Registers, Integer> registerFile;
    private int programCounter;
    private final Map<Class<? extends Annotation>, Function<Annotation, Runnable>> functions = ImmutableMap.of(
            Inc.class, this::inc,
            Dec.class, this::dec
    );
    private final Map<Class<? extends Annotation>, Consumer<Annotation>> unwrappers = ImmutableMap.of(
            Inc.class, this::unwrapSingle,
            Dec.class, this::unwrapSingle,
            Incs.class, this::unwrapContainer,
            Decs.class, this::unwrapContainer
    );
    private final Map<Integer, Runnable> process;

    public VirtualMachine() {
        registerFile = Arrays.stream(Registers.values())
                .collect(Collectors.toMap(identity(), v -> 0));
        programCounter = 0;
        process = new HashMap<>();
    }

    public int runProgram(Class<?> programClass) {
        Optional<Method> method = Arrays.stream(programClass.getMethods())
                .filter(m -> m.getName().equals(MAIN_METHOD))
                .findFirst();
        if (method.isPresent()) {
            List<Annotation> program = Arrays.asList(method.get().getDeclaredAnnotations());
            for (int x = 0; x < program.size(); ++x) {
                Annotation annotation = program.get(x);
                unwrappers.get(annotation.annotationType()).accept(annotation);
            }
            Optional<Runnable> line = maybeGet(process, programCounter);
            while (line.isPresent()) {
                line.get().run();
                programCounter++;
                line = maybeGet(process, programCounter);
            }
            return registerFile.get(R1);
        }
        else {
            throw new IllegalStateException();
        }
    }

    private static <K,V> Optional<V> maybeGet(Map<K,V> map, K key) {
        return Optional.ofNullable(map.get(key));
    }

    private void unwrapSingle(Annotation annotation) {
        process.put(getLine(annotation), functions.get(annotation.annotationType()).apply(annotation));
    }

    @SneakyThrows
    private Integer getLine(Annotation annotation) {
        Method method = annotation.annotationType().getMethod("line");
        return (Integer)method.invoke(annotation);
    }

    @SneakyThrows
    private Annotation[] getInner(Annotation annotation) {
        Method method = annotation.annotationType().getMethod("value");
        return (Annotation[]) method.invoke(annotation);
    }

    private void unwrapContainer(Annotation annotation) {
        Arrays.stream(getInner(annotation))
                .forEach(i -> process.put(getLine(i), functions.get(i.annotationType()).apply(i)));
    }

    private Runnable inc(Annotation a) {
        Inc inc = (Inc)a;
        return () -> registerFile.compute(inc.reg(), (k,v) -> v + inc.value());
    }

    private Runnable dec(Annotation a) {
        Dec inc = (Dec)a;
        return () -> registerFile.compute(inc.reg(), (k,v) -> v - inc.value());
    }
}
