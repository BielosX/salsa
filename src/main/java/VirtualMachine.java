import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import opcodes.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Optional.empty;
import static java.util.function.Function.identity;
import static opcodes.Registers.R1;
import static opcodes.Registers.ZERO_FLAG;

@EqualsAndHashCode
public class VirtualMachine {
    private final static String MAIN_METHOD = "start";
    private final Map<Registers, Integer> registerFile;
    private int programCounter;
    private final Map<Class<? extends Annotation>, Function<Annotation, Supplier<Optional<Integer>>>> functions = Maps.asMap(
            Maps.Pair.of(Inc.class, this::inc),
            Maps.Pair.of(Dec.class, this::dec),
            Maps.Pair.of(CmpZero.class, this::cmpZero),
            Maps.Pair.of(Jnz.class, this::jnz),
            Maps.Pair.of(Add.class, this::add),
            Maps.Pair.of(Mov.class, this::mov)
    );
    private final Map<Class<? extends Annotation>, Consumer<Annotation>> unwrappers = Maps.asMap(
            Maps.Pair.of(Inc.class, this::unwrapSingle),
            Maps.Pair.of(Dec.class, this::unwrapSingle),
            Maps.Pair.of(CmpZero.class, this::unwrapSingle),
            Maps.Pair.of(Jnz.class, this::unwrapSingle),
            Maps.Pair.of(Add.class, this::unwrapSingle),
            Maps.Pair.of(Mov.class, this::unwrapSingle),
            Maps.Pair.of(Incs.class, this::unwrapContainer),
            Maps.Pair.of(Decs.class, this::unwrapContainer),
            Maps.Pair.of(CmpZeros.class, this::unwrapContainer),
            Maps.Pair.of(Jnzs.class, this::unwrapContainer),
            Maps.Pair.of(Adds.class, this::unwrapContainer),
            Maps.Pair.of(Movs.class, this::unwrapContainer)
    );
    private final Map<Integer, Supplier<Optional<Integer>>> process;

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
            Optional<Supplier<Optional<Integer>>> line = maybeGet(process, programCounter);
            while (line.isPresent()) {
                Optional<Integer> newPC = line.get().get();
                programCounter = newPC.orElse(programCounter + 1);
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

    private <T> Optional<Integer> noJump(T t) {
        return empty();
    }

    private Supplier<Optional<Integer>> inc(Annotation a) {
        Inc inc = (Inc)a;
        return () -> noJump(registerFile.compute(inc.reg(), (k,v) -> v + inc.value()));
    }

    private Supplier<Optional<Integer>> dec(Annotation a) {
        Dec inc = (Dec)a;
        return () -> noJump(registerFile.compute(inc.reg(), (k,v) -> v - inc.value()));
    }

    private Supplier<Optional<Integer>> cmpZero(Annotation a) {
        CmpZero cmp = (CmpZero)a;
        return () -> {
            if (registerFile.get(cmp.reg()) == 0) {
                registerFile.compute(ZERO_FLAG, (k,v) -> 1);
            }
            return empty();
        };
    }

    private Supplier<Optional<Integer>> jnz(Annotation a) {
        Jnz jnz = (Jnz)a;
        return () -> {
            if (registerFile.get(ZERO_FLAG) != 1) {
                return Optional.of(jnz.jumpTo());
            }
            return Optional.empty();
        };
    }

    private Supplier<Optional<Integer>> add(Annotation a) {
        Add add = (Add)a;
        return () -> {
            int op1 = registerFile.get(add.op1());
            int op2 = registerFile.get(add.op2());
            registerFile.compute(add.dest(), (k,v) -> op1 + op2);
            return empty();
        };
    }

    private Supplier<Optional<Integer>> mov(Annotation a) {
        Mov mov = (Mov)a;
        return () -> noJump(registerFile.compute(mov.dest(), (k,v) -> mov.val()));
    }
}
