package cn.edu.hitsz.compiler.asm;


import java.util.HashMap;

public class Registers {
    private static HashMap registers = new HashMap<Register,String>();

    public void initRegisters(){
        registers.put(Register.named("t0"),"#");
        registers.put(Register.named("t1"),"#");
        registers.put(Register.named("t2"),"#");
        registers.put(Register.named("t3"),"#");
        registers.put(Register.named("t4"),"#");
        registers.put(Register.named("t5"),"#");
        registers.put(Register.named("t6"),"#");
        registers.put(Register.named("a0"),"#");

    }

    public HashMap<Register,String> getRegisters(){
        return registers;
    }
}
