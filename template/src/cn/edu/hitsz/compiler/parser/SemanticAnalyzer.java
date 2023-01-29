package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.lexer.TokenKind;
import cn.edu.hitsz.compiler.parser.table.Production;
import cn.edu.hitsz.compiler.parser.table.Status;
import cn.edu.hitsz.compiler.symtab.SourceCodeType;
import cn.edu.hitsz.compiler.symtab.SymbolTable;

import java.util.Stack;

// TODO: 实验三: 实现语义分析
public class SemanticAnalyzer implements ActionObserver {

    // 符号表
    private SymbolTable myTable;
    // 语义分析栈
    private Stack<String> stack = new Stack<String>();
    // token栈
    private Stack<Token> tk_stack = new Stack<Token>();

    @Override
    public void whenAccept(Status currentStatus) {
        // TODO: 该过程在遇到 Accept 时要采取的代码动作
    }

    @Override
    public void whenReduce(Status currentStatus, Production production) {
        // TODO: 该过程在遇到 reduce production 时要采取的代码动作
        switch (production.index()){
            case 1:// P -> S_list
                break;
            case 2:// S_list -> S Semicolon S_list;
            case 6:// S -> id = E;
            case 8:// E -> E + A;
            case 9:// E -> E - A;
            case 11:// A -> A * B;
            case 13:// B -> ( E );
                stack.pop();
                tk_stack.pop();
                stack.pop();
                tk_stack.pop();
                break;
            case 3:// S_list -> S Semicolon;
            case 7:// S -> return E;
                stack.pop();
                tk_stack.pop();
                break;
            case 4:// S -> D id;
                stack.pop();
                myTable.get(tk_stack.peek().getText()).setType(SourceCodeType.Int);
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
            default:;
        }
    }

    @Override
    public void whenShift(Status currentStatus, Token currentToken) {
        // TODO: 该过程在遇到 shift 时要采取的代码动作
        stack.push(currentToken.getKindId());
        tk_stack.push(currentToken);
    }

    @Override
    public void setSymbolTable(SymbolTable table) {
        // TODO: 设计你可能需要的符号表存储结构
        // 如果需要使用符号表的话, 可以将它或者它的一部分信息存起来, 比如使用一个成员变量存储
        myTable = table;

    }
}

