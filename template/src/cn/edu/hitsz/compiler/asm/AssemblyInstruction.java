package cn.edu.hitsz.compiler.asm;

import cn.edu.hitsz.compiler.ir.Instruction;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AssemblyInstruction {
	//============================== 不同种类 IR 的构造函数 ==============================
    public static AssemblyInstruction createLi(Instruction instruction, String register, String temp) {
        if (temp == null) {
            return new AssemblyInstruction(AssemblyInstructionKind.li, register, List.of(instruction.getFrom().toString()));
        }
        else{
            return new AssemblyInstruction(AssemblyInstructionKind.li, register, List.of(temp));
        }
    }

    public static AssemblyInstruction createMv(Instruction instruction, String register, String register2) {
        return new AssemblyInstruction(AssemblyInstructionKind.mv, register, List.of(register2));
    }

    public static AssemblyInstruction createMul(Instruction instruction, String register, String left, String right) {
        return new AssemblyInstruction(AssemblyInstructionKind.mul, register, List.of(left,right));
    }

    public static AssemblyInstruction createSub(Instruction instruction, String register, String left, String right) {
        return new AssemblyInstruction(AssemblyInstructionKind.sub, register, List.of(left, right));
    }

    public static AssemblyInstruction createAddi(Instruction instruction, String register, String left) {
        return new AssemblyInstruction(AssemblyInstructionKind.addi, register, List.of(left,instruction.getLHS().toString()));
    }




    //============================== 基础设施 ==============================
    @Override
    public String toString() {
        final var kindString = kind.toString();
        final var resultString = result == null ? "" : result.toString();
        final var operandsString = operands.stream().map(Objects::toString).collect(Collectors.joining(", "));
        return "%s %s, %s".formatted(kindString, resultString, operandsString);
    }

    public List<String> getOperands() {
        return Collections.unmodifiableList(operands);
    }

    private AssemblyInstruction(AssemblyInstructionKind kind, String result, List<String> operands) {
        this.kind = kind;
        this.result = result;
        this.operands = operands;
    }

    private final AssemblyInstructionKind kind;
    private final String result;
    private final List<String> operands;


}
