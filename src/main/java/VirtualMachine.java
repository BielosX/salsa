import com.google.common.collect.ImmutableMap;
import lombok.EqualsAndHashCode;
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
    private final Map<Class<? extends Annotation>, Consumer<Annotation>> containerUnwr = ImmutableMap.of(
            Incs.class, this::unwrapIncs,
            Decs.class, this::unwrapDecs
    );
    private final Map<Class<? extends Annotation>, Consumer<Annotation>> unwrappers = ImmutableMap.of(
            Inc.class, this::unwrapInc,
            Dec.class, this::unwrapDec
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
                if (containerUnwr.keySet().contains(annotation.annotationType())) {
                    containerUnwr.get(annotation.annotationType()).accept(annotation);
                }
                else {
                    unwrappers.get(annotation.annotationType()).accept(annotation);
                }
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

    private void unwrapInc(Annotation annotation) {
        Inc inc = (Inc)annotation;
        process.put(inc.line(), functions.get(inc.annotationType()).apply(inc));
    }

    private void unwrapDec(Annotation annotation) {
        Dec dec = (Dec)annotation;
        process.put(dec.line(), functions.get(dec.annotationType()).apply(dec));
    }

    private void unwrapIncs(Annotation annotation) {
        Incs incs = (Incs)annotation;
        Arrays.stream(incs.value()).forEach(i -> process.put(i.line(), functions.get(i.annotationType()).apply(i)));
    }

    private void unwrapDecs(Annotation annotation) {
        Decs decs = (Decs)annotation;
        Arrays.stream(decs.value()).forEach(d -> process.put(d.line(), functions.get(d.annotationType()).apply(d)));
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
