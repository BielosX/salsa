import opcodes.Dec;
import opcodes.Inc;

import static opcodes.Registers.R1;

public class Main {

    @Inc(line = 0, reg = R1, value = 5)
    @Dec(line = 1, reg = R1, value = 1)
    @Inc(line = 2, reg = R1, value = 5)
    public static void start() {}

    public static void main(String[] args) {
        VirtualMachine virtualMachine = new VirtualMachine();
        int result = virtualMachine.runProgram(Main.class);
        System.out.println(result);
    }
}
