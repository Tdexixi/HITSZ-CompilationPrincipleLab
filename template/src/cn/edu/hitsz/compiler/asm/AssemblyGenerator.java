package cn.edu.hitsz.compiler.asm;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.ir.IRValue;
import cn.edu.hitsz.compiler.ir.IRVariable;
import cn.edu.hitsz.compiler.ir.Instruction;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.util.*;


/**
 * TODO: 实验四: 实现汇编生成
 * <br>
 * 在编译器的整体框架中, 代码生成可以称作后端, 而前面的所有工作都可称为前端.
 * <br>
 * 在前端完成的所有工作中, 都是与目标平台无关的, 而后端的工作为将前端生成的目标平台无关信息
 * 根据目标平台生成汇编代码. 前后端的分离有利于实现编译器面向不同平台生成汇编代码. 由于前后
 * 端分离的原因, 有可能前端生成的中间代码并不符合目标平台的汇编代码特点. 具体到本项目你可以
 * 尝试加入一个方法将中间代码调整为更接近 risc-v 汇编的形式, 这样会有利于汇编代码的生成.
 * <br>
 * 为保证实现上的自由, 框架中并未对后端提供基建, 在具体实现时可自行设计相关数据结构.
 *
 * @see AssemblyGenerator#run() 代码生成与寄存器分配
 */
public class AssemblyGenerator {
	
	private List<Instruction> instructions;
	private List<AssemblyInstruction> assemblyInstructions = new ArrayList<>();

    private HashMap<String,Integer> indexTimes = new HashMap<>();
    private Registers registers = new Registers();
    /**
     * 加载前端提供的中间代码
     * <br>
     * 视具体实现而定, 在加载中或加载后会生成一些在代码生成中会用到的信息. 如变量的引用
     * 信息. 这些信息可以通过简单的映射维护, 或者自行增加记录信息的数据结构.
     *
     * @param originInstructions 前端提供的中间代码
     */
    public void loadIR(List<Instruction> originInstructions) {
        // TODO: 读入前端提供的中间代码并生成所需要的信息
        instructions = originInstructions;
    }


    /**
     * 执行代码生成.
     * <br>
     * 根据理论课的做法, 在代码生成时同时完成寄存器分配的工作. 若你觉得这样的做法不好,
     * 也可以将寄存器分配和代码生成分开进行.
     * <br>
     * 提示: 寄存器分配中需要的信息较多, 关于全局的与代码生成过程无关的信息建议在代码生
     * 成前完成建立, 与代码生成的过程相关的信息可自行设计数据结构进行记录并动态维护.
     */
    public void run() {
        // TODO: 执行寄存器分配与代码生成
        // 初始化寄存器
        registers.initRegisters();

        // 判断指令类型
        for(Instruction i : instructions){
            boolean flag = false;
            switch(i.getKind()){
                case MOV -> {
                    // 如果MOV的operands是立即数，生成li指令
                    if (i.getFrom().isImmediate()){
                        // 如果变量原本就在寄存器中
                        if (registers.getRegisters().containsValue(i.getResult().toString())) {
                            for(Register r:registers.getRegisters().keySet()){
                                if(Objects.equals(registers.getRegisters().get(r), i.getResult().toString())){
                                    assemblyInstructions.add(AssemblyInstruction.createLi(i, r.getName(), null));
                                    break;
                                }
                            }
                        }
                        // 变量不在寄存器中
                        else{
                            // 寻找没有使用的寄存器
                            for(Register r:registers.getRegisters().keySet()){

                                if(!r.getUsed()&& !r.getName().equals("a0")){
                                    r.setUsedValid();
                                    registers.getRegisters().replace(r, i.getResult().toString());
                                    assemblyInstructions.add(AssemblyInstruction.createLi(i, r.getName(), null));
                                    flag = true;
                                    break;
                                }
                            }
                            // 如果寄存器已被占满，需要替换
                            if(!flag){
                                for(Register r:registers.getRegisters().keySet()){
                                    // 检查寄存器中哪个变量之后将不再使用
                                    String value = registers.getRegisters().get(r);
                                    // 如果之后某个变量不再使用，则将其替换掉
                                    if(!ifIndex(IRVariable.named(value),instructions.indexOf(i))&& !r.getName().equals("a0")){
                                        registers.getRegisters().replace(r, i.getResult().toString());
                                        assemblyInstructions.add(AssemblyInstruction.createLi(i, r.getName(), null));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    // 如果MOV的operands是变量，生成mv指令
                    else if (i.getFrom().isIRVariable()){
                        // 如果变量原本就在寄存器中
                        String oper = "default";
                        for(Register r:registers.getRegisters().keySet()){
                            if(Objects.equals(registers.getRegisters().get(r), i.getFrom().toString())){
                                oper = r.getName();
                                break;
                            }
                        }

                        if (registers.getRegisters().containsValue(i.getResult().toString())) {
                            for(Register r:registers.getRegisters().keySet()){
                                if(Objects.equals(registers.getRegisters().get(r), i.getResult().toString())){
                                    assemblyInstructions.add(AssemblyInstruction.createMv(i, r.getName(), oper));
                                    break;
                                }
                            }
                        }
                        // 变量不在寄存器中
                        else{
                            // 寻找没有使用的寄存器
                            for(Register r:registers.getRegisters().keySet()){

                                if(!r.getUsed()&& !r.getName().equals("a0")){
                                    r.setUsedValid();
                                    registers.getRegisters().replace(r, i.getResult().toString());
                                    assemblyInstructions.add(AssemblyInstruction.createMv(i, r.getName(), oper));
                                    flag = true;
                                    break;
                                }
                            }
                            // 如果寄存器已被占满，需要替换
                            if(!flag){
                                for(Register r:registers.getRegisters().keySet()){
                                    // 检查寄存器中哪个变量之后将不再使用
                                    String value = registers.getRegisters().get(r);
                                    // 如果之后某个变量不再使用，则将其替换掉
                                    if(!ifIndex(IRVariable.named(value), instructions.indexOf(i))&& !r.getName().equals("a0")){
                                        registers.getRegisters().replace(r, i.getResult().toString());
                                        assemblyInstructions.add(AssemblyInstruction.createMv(i, r.getName(), oper));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    break;
                }

                case ADD -> {
                    String left = "default";
                    for(Register r:registers.getRegisters().keySet()){
                        if(Objects.equals(registers.getRegisters().get(r), i.getRHS().toString())){
                            left = r.getName();
                            break;
                        }
                    }

                    // 如果变量原本就在寄存器中
                    if (registers.getRegisters().containsValue(i.getResult().toString())) {
                        for(Register r:registers.getRegisters().keySet()){
                            if(Objects.equals(registers.getRegisters().get(r), i.getResult().toString())){
                                assemblyInstructions.add(AssemblyInstruction.createAddi(i, r.getName(), left));
                                break;
                            }
                        }
                    }
                    // 变量不在寄存器中
                    else{
                        // 寻找没有使用的寄存器
                        for(Register r:registers.getRegisters().keySet()){
                            if(!r.getUsed()&& !r.getName().equals("a0")){
                                r.setUsedValid();
                                registers.getRegisters().replace(r, i.getResult().toString());
                                assemblyInstructions.add(AssemblyInstruction.createAddi(i, r.getName(), left));
                                flag = true;
                                break;
                            }
                        }
                        // 如果寄存器已被占满，需要替换
                        if(!flag){
                            for(Register r:registers.getRegisters().keySet()){
                                // 检查寄存器中哪个变量之后将不再使用
                                String value = registers.getRegisters().get(r);
                                // 如果之后某个变量不再使用，则将其替换掉
                                if(!ifIndex(IRVariable.named(value),instructions.indexOf(i))&& !r.getName().equals("a0")){
                                    registers.getRegisters().replace(r, i.getResult().toString());
                                    assemblyInstructions.add(AssemblyInstruction.createAddi(i, r.getName(), left));
                                    break;
                                }
                            }
                        }
                    }
                    break;
                }
                case MUL -> {
                    String left = "default";
                    String right = "default";
                    for(Register r:registers.getRegisters().keySet()){
                        if(Objects.equals(registers.getRegisters().get(r), i.getLHS().toString())){
                            left = r.getName();
                        }
                        if(Objects.equals(registers.getRegisters().get(r), i.getRHS().toString())){
                            right = r.getName();
                        }
                    }

                    // 如果变量原本就在寄存器中
                    if (registers.getRegisters().containsValue(i.getResult().toString())) {
                        for(Register r:registers.getRegisters().keySet()){
                            if(Objects.equals(registers.getRegisters().get(r), i.getResult().toString())){
                                assemblyInstructions.add(AssemblyInstruction.createMul(i, r.getName(), left, right));
                                break;
                            }
                        }
                    }
                    // 变量不在寄存器中
                    else{
                        // 寻找没有使用的寄存器
                        for(Register r:registers.getRegisters().keySet()){
                            if(!r.getUsed()&& !r.getName().equals("a0")){
                                r.setUsedValid();
                                registers.getRegisters().replace(r, i.getResult().toString());
                                assemblyInstructions.add(AssemblyInstruction.createMul(i, r.getName(), left, right));
                                flag = true;
                                break;
                            }
                        }
                        // 如果寄存器已被占满，需要替换
                        if(!flag){
                            for(Register r:registers.getRegisters().keySet()){
                                // 检查寄存器中哪个变量之后将不再使用
                                String value = registers.getRegisters().get(r);
                                // 如果之后某个变量不再使用，则将其替换掉
                                if(!ifIndex(IRVariable.named(value),instructions.indexOf(i))&& !r.getName().equals("a0")){
                                    registers.getRegisters().replace(r, i.getResult().toString());
                                    assemblyInstructions.add(AssemblyInstruction.createMul(i, r.getName(), left, right));
                                    break;
                                }
                            }
                        }
                    }
                    break;
                }
                case RET -> {
                    String reValue = "default";

                    for(Register r:registers.getRegisters().keySet()){
                        if(Objects.equals(registers.getRegisters().get(r), i.getReturnValue().toString())){
                            reValue = r.getName();
                        }
                    }
                    for(Register r:registers.getRegisters().keySet()){
                        if(Objects.equals(r.getName(), "a0")){
                            assemblyInstructions.add(AssemblyInstruction.createMv(i, r.getName(), reValue));
                            break;
                        }
                    }
                    break;
                }
                case SUB -> {
                    if(i.getLHS().isImmediate()){
                        // 如果变量原本就在寄存器中
                        if (registers.getRegisters().containsValue(i.getLHS().toString())) {
                            for(Register r:registers.getRegisters().keySet()){
                                if(Objects.equals(registers.getRegisters().get(r), i.getLHS().toString())){
                                    assemblyInstructions.add(AssemblyInstruction.createLi(i, r.getName(), i.getLHS().toString()));
                                    break;
                                }
                            }
                        }
                        // 变量不在寄存器中
                        else{
                            // 寻找没有使用的寄存器
                            for(Register r:registers.getRegisters().keySet()){

                                if(!r.getUsed()&& !r.getName().equals("a0")){
                                    r.setUsedValid();
                                    registers.getRegisters().replace(r, i.getLHS().toString());
                                    assemblyInstructions.add(AssemblyInstruction.createLi(i, r.getName(), i.getLHS().toString()));
                                    flag = true;
                                    break;
                                }
                            }
                            // 如果寄存器已被占满，需要替换
                            if(!flag){
                                for(Register r:registers.getRegisters().keySet()){
                                    // 检查寄存器中哪个变量之后将不再使用
                                    String value = registers.getRegisters().get(r);
                                    // 如果之后某个变量不再使用，则将其替换掉
                                    if(!ifIndex(IRVariable.named(value),instructions.indexOf(i))&& !r.getName().equals("a0")){
                                        registers.getRegisters().replace(r, i.getLHS().toString());
                                        assemblyInstructions.add(AssemblyInstruction.createLi(i, r.getName(), i.getLHS().toString()));
                                        break;
                                    }
                                }
                            }
                        }
                    }





                    String left = "default";
                    String right = "default";
                    for(Register r:registers.getRegisters().keySet()){
                        if(Objects.equals(registers.getRegisters().get(r), i.getLHS().toString())){
                            left = r.getName();
                        }
                        if(Objects.equals(registers.getRegisters().get(r), i.getRHS().toString())){
                            right = r.getName();
                        }
                    }

                    // 如果变量原本就在寄存器中
                    if (registers.getRegisters().containsValue(i.getResult().toString())) {
                        for(Register r:registers.getRegisters().keySet()){
                            if(Objects.equals(registers.getRegisters().get(r), i.getResult().toString())){
                                assemblyInstructions.add(AssemblyInstruction.createSub(i, r.getName(), left, right));
                                break;
                            }
                        }
                    }
                    // 变量不在寄存器中
                    else{
                        // 寻找没有使用的寄存器
                        for(Register r:registers.getRegisters().keySet()){
                            if(!r.getUsed()&& !r.getName().equals("a0")){
                                r.setUsedValid();
                                registers.getRegisters().replace(r, i.getResult().toString());
                                assemblyInstructions.add(AssemblyInstruction.createSub(i, r.getName(), left, right));
                                flag = true;
                                break;
                            }
                        }
                        // 如果寄存器已被占满，需要替换
                        if(!flag){
                            for(Register r:registers.getRegisters().keySet()){
                                // 检查寄存器中哪个变量之后将不再使用
                                String value = registers.getRegisters().get(r);
                                // 如果之后某个变量不再使用，则将其替换掉
                                if(!ifIndex(IRVariable.named(value),instructions.indexOf(i))&& !r.getName().equals("a0")){
                                    registers.getRegisters().replace(r, i.getResult().toString());
                                    assemblyInstructions.add(AssemblyInstruction.createSub(i, r.getName(), left, right));
                                    break;
                                }
                            }
                        }
                    }
                    break;
                }
            }


        }
    }
    
    /**
     * 输出汇编代码到文件
     *
     * @param path 输出文件路径
     */
    public void dump(String path) {
        // TODO: 输出汇编代码到文件
    	FileUtils.writeLines(path, getInstructions().stream().map(AssemblyInstruction::toString).toList());
    }
    
    public List<AssemblyInstruction> getInstructions() {
        // TODO
        return assemblyInstructions;
    }


    /**
     *  检查变量在之后的指令中是否仍然使用
     * @param ir 输入的变量
     * @param x  当前指令的索引
     */
    public boolean ifIndex(IRValue ir, int x){
        for(int i = x; i<instructions.size(); i++){
            if(instructions.get(x).getOperands().contains(ir)){
                return true;
            }
        }
        return false;
    }
}

