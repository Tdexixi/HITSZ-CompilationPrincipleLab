package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.ir.IRImmediate;
import cn.edu.hitsz.compiler.ir.IRValue;
import cn.edu.hitsz.compiler.ir.IRVariable;
import cn.edu.hitsz.compiler.ir.Instruction;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.lexer.TokenKind;
import cn.edu.hitsz.compiler.parser.table.Production;
import cn.edu.hitsz.compiler.parser.table.Status;
import cn.edu.hitsz.compiler.symtab.SourceCodeType;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

// TODO: 实验三: 实现 IR 生成

/**
 *
 */
public class IRGenerator implements ActionObserver {

    // 符号表
    private SymbolTable table;
    // 生成的中间代码列表
    private List<Instruction> codes = new ArrayList<>();
    // 中间代码值栈
    private Stack<IRValue> ir_stack = new Stack<>();

    @Override
    public void whenShift(Status currentStatus, Token currentToken) {
        // TODO
        if(currentToken.getKind().equals(TokenKind.fromString("IntConst"))){
            ir_stack.push(IRImmediate.of(Integer.valueOf(currentToken.getText())));
        }
        else if(currentToken.getKind().equals(TokenKind.fromString("id"))){
            if(TokenKind.isAllowed(currentToken.getKind().toString())) {
                ir_stack.push(IRVariable.named(currentToken.getText()));
            }
        }
    }

    @Override
    public void whenReduce(Status currentStatus, Production production) {
        // TODO
        switch (production.index()) {
            case 1:// P -> S_list
                break;
            case 2:// S_list -> S Semicolon S_list;
                break;
            case 6:// S -> id = E;
                IRValue r1 = ir_stack.pop();
                IRValue l1 = ir_stack.pop();
                codes.add(Instruction.createMov((IRVariable) l1, r1));
                ir_stack.push(r1);
                break;
            case 8:// E -> E + A;
                IRVariable temp1 = IRVariable.temp();
                IRValue r2 = ir_stack.pop();
                IRValue l2 = ir_stack.pop();
                codes.add(Instruction.createAdd(temp1,l2,r2));
                ir_stack.push(temp1);
                break;
            case 9:// E -> E - A;
                IRVariable temp2 = IRVariable.temp();
                IRValue r3 = ir_stack.pop();
                IRValue l3 = ir_stack.pop();
                codes.add(Instruction.createSub(temp2,l3,r3));
                ir_stack.push(temp2);
                break;
            case 11:// A -> A * B;
                IRVariable temp3 = IRVariable.temp();
                IRValue r4 = ir_stack.pop();
                IRValue l4 = ir_stack.pop();
                codes.add(Instruction.createMul(temp3,l4,r4));
                ir_stack.push(temp3);
                break;
            case 13:// B -> ( E );
                break;
            case 3:// S_list -> S Semicolon;
                break;
            case 7:// S -> return E;
                IRValue result = ir_stack.pop();
                codes.add(Instruction.createRet(result));
                break;
            case 4:// S -> D id;
                break;
            case 5:// D -> int;
                break;
            case 10:// E -> A;
                break;
            case 12:// A -> B;
                break;
            case 14:// B -> id;
                break;
            case 15:// B -> IntConst;
                break;
            default:
                ;
        }
    }


    @Override
    public void whenAccept(Status currentStatus) {
        // TODO

    }

    @Override
    public void setSymbolTable(SymbolTable table) {
        // TODO
        this.table = table;
    }

    public List<Instruction> getIR() {
        // TODO
        return codes;
    }

    public void dumpIR(String path) {
        FileUtils.writeLines(path, getIR().stream().map(Instruction::toString).toList());
    }
}

