package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.parser.table.Production;
import cn.edu.hitsz.compiler.parser.table.Status;
import cn.edu.hitsz.compiler.parser.table.Symbol;
import cn.edu.hitsz.compiler.parser.table.Term;
import cn.edu.hitsz.compiler.symtab.SourceCodeType;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.symtab.SymbolTableEntry;

import java.util.Stack;

// TODO: 实验三: 实现语义分析
public class SemanticAnalyzer implements ActionObserver {
    private SymbolTable new_table;
    private Stack<Symbol> tokens = new Stack<>();
    @Override
    public void whenAccept(Status currentStatus) {
        // TODO: 该过程在遇到 Accept 时要采取的代码动作
        return;
//        throw new NotImplementedException();
    }

    @Override
    public void whenReduce(Status currentStatus, Production production) {
        // TODO: 该过程在遇到 reduce production 时要采取的代码动作
        Symbol curSym;
        switch (production.index()){
            case 4: // S->D id
                curSym = tokens.peek();
                if (!new_table.has(curSym.getToken().getText())) throw new RuntimeException("variable is not defined");
                new_table.get(curSym.getToken().getText()).setType(SourceCodeType.Int);
                break;
            case 14: // B->id
                curSym = tokens.peek();
                if (!new_table.has(curSym.getToken().getText())) throw new RuntimeException("variable is not defined");
                break;
        }
        for (int i=0;i<production.body().size();i++) tokens.pop();
        tokens.push(new Symbol(production.head()));
//        throw new NotImplementedException();
    }

    @Override
    public void whenShift(Status currentStatus, Token currentToken) {
        // TODO: 该过程在遇到 shift 时要采取的代码动作
        tokens.push(new Symbol(currentToken));
//        throw new NotImplementedException();
    }

    @Override
    public void setSymbolTable(SymbolTable table) {
        // TODO: 设计你可能需要的符号表存储结构
        // 如果需要使用符号表的话, 可以将它或者它的一部分信息存起来, 比如使用一个成员变量存储
        new_table = table;
//        throw new NotImplementedException();
    }
}

