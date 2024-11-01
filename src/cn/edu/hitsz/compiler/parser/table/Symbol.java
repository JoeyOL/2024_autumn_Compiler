package cn.edu.hitsz.compiler.parser.table;

import cn.edu.hitsz.compiler.ir.IRValue;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.symtab.SourceCodeType;

public class Symbol {
    private Token token;
    private NonTerminal nonTerminal;
    private boolean isToken;
    private IRValue value;
    private Symbol(Token token, NonTerminal nonTerminal){
        this.token = token;
        this.nonTerminal = nonTerminal;
        this.isToken = token!=null;
    }
    public Symbol(Token token) {this(token, null);}
    public Symbol(NonTerminal nonTerminal) {this(null, nonTerminal);}
    public boolean isToken() {return isToken;}
    public boolean isTerminal() {return !isToken;}
    public Token getToken() {return token;}
    public NonTerminal getNonTerminal() {return nonTerminal;}
    public void setIRValue(IRValue value) {this.value = value;}
    public IRValue getIRValue() {return this.value;}
}
