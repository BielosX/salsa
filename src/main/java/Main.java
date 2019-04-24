import opcodes.*;

import static opcodes.Registers.*;

public class Main {

    @Mov(line = 0, dest = R2, val = 10)
    @Mov(line = 1, dest = R1, val = 0)
    @Add(line = 2, dest = R1, op1 = R2, op2 = R1)
    @Dec(line = 3, reg = R2, value = 1)
    @CmpZero(line = 4, reg = R2)
    @Jnz(line = 5, jumpTo = 2)
    public static void start() {}

    public static void main(String[] args) {
        VirtualMachine virtualMachine = new VirtualMachine();
        int result = virtualMachine.runProgram(Main.class);
        System.out.println(result);
    }
}
