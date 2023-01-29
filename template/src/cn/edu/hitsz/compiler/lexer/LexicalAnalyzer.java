package cn.edu.hitsz.compiler.lexer;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.io.*;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * TODO: 实验一: 实现词法分析
 * <br>
 * 你可能需要参考的框架代码如下:
 *
 * @see Token 词法单元的实现
 * @see TokenKind 词法单元类型的实现
 */
public class LexicalAnalyzer {
    private final SymbolTable symbolTable;
    private StringBuilder stringBuilder = null;
    private List<Token> list = new ArrayList<>();

    public LexicalAnalyzer(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }


    /**
     * 从给予的路径中读取并加载文件内容
     *
     * @param path 路径
     */
    public void loadFile(String path) throws IOException {
        // TODO: 词法分析前的缓冲区实现
        // 可自由实现各类缓冲区
        // 或直接采用完整读入方法
        BufferedReader reader = new BufferedReader(new FileReader(path));
        stringBuilder = new StringBuilder();
        String line;
        while((line = reader.readLine())!=null){
            stringBuilder.append(line + "\n");
        }
        System.out.println(stringBuilder.toString());
        reader.close();
    }

    /**
     * 执行词法分析, 准备好用于返回的 token 列表 <br>
     * 需要维护实验一所需的符号表条目, 而得在语法分析中才能确定的符号表条目的成员可以先设置为 null
     */
    public void run() {
        // TODO: 自动机实现的词法分析过程
        String temp = "";
        if(stringBuilder!=null) {
            // 分析读入字符串中的每一个字符
            for (int i = 0; i < stringBuilder.length(); i++) {
                //判断读入字符是否为空格或者换行符，如果是，就检查temp，如果不为空，就说明有一个id或者关键字或者数字常量
                if(stringBuilder.charAt(i)==' '||stringBuilder.charAt(i)=='\n'){
                    processTemp(temp);
                    temp = "";

                }
                // 如果读入字符是字符或者数字，那么就将其加入temp
                else if(Character.isLetter(stringBuilder.charAt(i))||Character.isDigit(stringBuilder.charAt(i))){
                    temp = temp.concat(Character.toString(stringBuilder.charAt(i)));
                }
                else{
                    // 当读入字符为其他时
                    switch(stringBuilder.charAt(i)){
                        case '*':processTemp(temp);
                                list.add(Token.simple("*"));
                                break;
                        case '=':processTemp(temp);
                                list.add(Token.simple("="));
                                break;
                        case '(':processTemp(temp);
                                list.add(Token.simple("("));
                                break;
                        case ')':processTemp(temp);
                                list.add(Token.simple(")"));
                                break;
                        case ',':processTemp(temp);
                                list.add(Token.simple(","));
                                break;
                        case '-':processTemp(temp);
                                list.add(Token.simple("-"));
                                break;
                        case '+':processTemp(temp);
                                list.add(Token.simple("+"));
                                break;
                        case '/':processTemp(temp);
                                list.add(Token.simple("/"));
                                break;
                        case ';':processTemp(temp);
                                list.add(Token.simple("Semicolon"));
                                break;
                        default:;
                    }
                    temp  = "";
                }
            }
            list.add(Token.eof());
        }
    }

    /**
     * 获得词法分析的结果, 保证在调用了 run 方法之后调用
     *
     * @return Token 列表
     */
    public Iterable<Token> getTokens() {
        // TODO: 从词法分析过程中获取 Token 列表
        // 词法分析过程可以使用 Stream 或 Iterator 实现按需分析
        // 亦可以直接分析完整个文件
        // 总之实现过程能转化为一列表即可
        return list;
    }

    public void dumpTokens(String path) {
        FileUtils.writeLines(
            path,
            StreamSupport.stream(getTokens().spliterator(), false).map(Token::toString).toList()
        );
    }

    private void processTemp(String temp){
        if(temp != ""){
            // 判断temp第一个字母是数字还是字母，如果是数字就是整个认定为数字常量
            if(Character.isLetter(temp.charAt(0))) {
                // 判断是否为关键字
                if(TokenKind.isAllowed(temp)){
                    list.add(Token.simple(temp));
                }
                // temp为id
                else {
                    list.add(Token.normal("id", temp));
                    if(!symbolTable.has(temp)){
                        symbolTable.add(temp);
                    }
                }
            }
            else if(Character.isDigit(temp.charAt(0))){
                list.add(Token.normal("IntConst",temp));
            }
        }
    }
}
