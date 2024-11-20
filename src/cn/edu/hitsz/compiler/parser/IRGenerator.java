package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.ir.IRImmediate;
import cn.edu.hitsz.compiler.ir.IRValue;
import cn.edu.hitsz.compiler.ir.IRVariable;
import cn.edu.hitsz.compiler.ir.Instruction;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.parser.table.Production;
import cn.edu.hitsz.compiler.parser.table.Status;
import cn.edu.hitsz.compiler.parser.table.Symbol;
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
    private List<Instruction> irValues = new ArrayList<>();
    private Stack<Symbol> symbols = new Stack<>();

    @Override
    public void whenShift(Status currentStatus, Token currentToken) {
        // TODO
        Symbol curSymbol = new Symbol(currentToken);
        if (currentToken.getKindId().equals("IntConst"))
            curSymbol.setIRValue(IRImmediate.of(Integer.parseInt(currentToken.getText())));
        else if (currentToken.getKindId().equals("id"))
            curSymbol.setIRValue(IRVariable.named(currentToken.getText()));
        symbols.push(curSymbol);
//        throw new NotImplementedException();
    }

    @Override
    public void whenReduce(Status currentStatus, Production production) {
        // TODO
        Symbol ls, rs;
        IRVariable exp_val;
        Symbol head;
        switch (production.index()) {
            case 12:
                break; // A -> B;
            case 14:
                break; // B -> id;
            case 15: // B -> IntConst;
                break;
            case 10:
                break; // E -> A;

            case 6: // S -> id = E;
                rs = symbols.pop();
                symbols.pop();
                ls = symbols.pop();
                irValues.add(Instruction.createMov((IRVariable) ls.getIRValue(), rs.getIRValue()));
                symbols.push(new Symbol(production.head()));
                break;
            case 7: // S -> return E;
                rs = symbols.pop();
                symbols.pop();
                irValues.add(Instruction.createRet(rs.getIRValue()));
                symbols.push(new Symbol(production.head()));
                break;
            case 8: // E -> E + A;
                rs = symbols.pop();
                symbols.pop();
                ls = symbols.pop();
                exp_val = IRVariable.temp();
                irValues.add(Instruction.createAdd(exp_val, ls.getIRValue(), rs.getIRValue()));
                head = new Symbol(production.head());
                head.setIRValue(exp_val);
                symbols.push(head);
                break;
            case 9: // E -> E - A;
                rs = symbols.pop();
                symbols.pop();
                ls = symbols.pop();
                exp_val = IRVariable.temp();
                irValues.add(Instruction.createSub(exp_val, ls.getIRValue(), rs.getIRValue()));
                head = new Symbol(production.head());
                head.setIRValue(exp_val);
                symbols.push(head);
                break;

            case 11: // A -> A * B;
                rs = symbols.pop();
                symbols.pop();
                ls = symbols.pop();
                exp_val = IRVariable.temp();
                irValues.add(Instruction.createMul(exp_val, ls.getIRValue(), rs.getIRValue()));
                head = new Symbol(production.head());
                head.setIRValue(exp_val);
                symbols.push(head);
                break;

            case 13: // B -> ( E )
                symbols.pop();
                head = new Symbol(production.head());
                head.setIRValue(symbols.pop().getIRValue());
                symbols.pop();
                symbols.push(head);
                break;

            default:
                for (int i = 0; i < production.body().size(); i++) symbols.pop();
                symbols.push(new Symbol(production.head()));
        }
//        throw new NotImplementedException();
    }


    @Override
    public void whenAccept(Status currentStatus) {
        // TODO
        return;
//        throw new NotImplementedException();
    }

    @Override
    public void setSymbolTable(SymbolTable table) {
        // TODO
        return;
//        throw new NotImplementedException();
    }

    public List<Instruction> getIR() {
        // TODO
        return irValues;
//        throw new NotImplementedException();
    }

    public void dumpIR(String path) {
        FileUtils.writeLines(path, getIR().stream().map(Instruction::toString).toList());
    }
}

