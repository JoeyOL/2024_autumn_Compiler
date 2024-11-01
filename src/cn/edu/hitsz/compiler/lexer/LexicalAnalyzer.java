package cn.edu.hitsz.compiler.lexer;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.io.FileInputStream;
import java.io.InputStream;
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
    private InputStream input;
    private List<Token> tokens = new ArrayList<>();

    public LexicalAnalyzer(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }


    /**
     * 从给予的路径中读取并加载文件内容
     *
     * @param path 路径
     */
    public void loadFile(String path) {
        // TODO: 词法分析前的缓冲区实现
        try{
            this.input = new FileInputStream(path);
        }catch (Exception e) {
            e.getStackTrace();
        }
        // 可自由实现各类缓冲区
        // 或直接采用完整读入方法
//        throw new NotImplementedException();
    }

    /**
     * 执行词法分析, 准备好用于返回的 token 列表 <br>
     * 需要维护实验一所需的符号表条目, 而得在语法分析中才能确定的符号表条目的成员可以先设置为 null
     */
    public void run() {
        int putback = 0;
        char putbackch = ' ', currentch;
        try{
            int current = input.read();
            int state = 0;
            String text = "";
            while (current != -1) {
                currentch = (char)current;
                if (state == 0) {
                    if (currentch == ' ' || currentch == '\n' || currentch == '\t') state = 0;
                    else if (currentch == '=') tokens.add(Token.simple("="));
                    else if (currentch == '+') tokens.add(Token.simple("+"));
                    else if (currentch == '-') tokens.add(Token.simple("-"));
                    else if (currentch == '/') tokens.add(Token.simple("/"));
                    else if (currentch == '*') tokens.add(Token.simple("*"));
                    else if (currentch == ';') tokens.add(Token.simple("Semicolon"));
                    else if (currentch == '(') tokens.add(Token.simple("("));
                    else if (currentch == ')') tokens.add(Token.simple(")"));
                    else if (currentch >= '0' && currentch <='9') {
                        text = text + currentch;
                        state = 1;
                    }
                    else if (currentch >= 'a' && currentch <= 'z') {
                        text = text + currentch;
                        state = 2;
                    }
                }
                else if (state == 1) {
                    if (currentch >= '0' && currentch <= '9') text = text + currentch;
                    else if (currentch >= 'a' && currentch <= 'z') {
                        text = text + currentch;
                        state = 2;
                    }
                    else {
                        tokens.add(Token.normal("IntConst", text));
                        putback = 1;
                        putbackch = currentch;
                        text = "";
                        state = 0;
                    }
                }
                else if (state == 2) {
                    if ((currentch >= '0' && currentch <= '9') || (currentch >= 'a' && currentch <= 'z')) text = text + currentch;
                    else {
                        if (text.equals("int")) tokens.add(Token.simple("int"));
                        else if (text.equals("return")) tokens.add(Token.simple("return"));
                        else {
                            tokens.add(Token.normal("id", text));
                            if (!symbolTable.has(text)) symbolTable.add(text);
                        }
                        putback = 1;
                        putbackch = currentch;
                        text = "";
                        state = 0;

                    }
                }

                if (putback == 1) {
                    current = (int) putbackch;
                    putback = 0;
                    putbackch = ' ';
                }
                else current = input.read();
            }
        }
        catch (Exception e) {e.getStackTrace();}
        tokens.add(Token.eof());
        // TODO: 自动机实现的词法分析过程
//        throw new NotImplementedException();
    }

    /**
     * 获得词法分析的结果, 保证在调用了 run 方法之后调用
     *
     * @return Token 列表
     */
    public Iterable<Token> getTokens() {
        // TODO: 从词法分析过程中获取 Token 列表
        return tokens;
        // 词法分析过程可以使用 Stream 或 Iterator 实现按需分析
        // 亦可以直接分析完整个文件
        // 总之实现过程能转化为一列表即可
//        throw new NotImplementedException();
    }

    public void dumpTokens(String path) {
        FileUtils.writeLines(
            path,
            StreamSupport.stream(getTokens().spliterator(), false).map(Token::toString).toList()
        );
    }


}
