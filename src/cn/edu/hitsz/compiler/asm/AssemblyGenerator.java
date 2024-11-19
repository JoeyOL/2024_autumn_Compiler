package cn.edu.hitsz.compiler.asm;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.ir.*;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;


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

    /**
     * 加载前端提供的中间代码
     * <br>
     * 视具体实现而定, 在加载中或加载后会生成一些在代码生成中会用到的信息. 如变量的引用
     * 信息. 这些信息可以通过简单的映射维护, 或者自行增加记录信息的数据结构.
     *
     * @param originInstructions 前端提供的中间代码
     */
    private List<Instruction> processedInst = new ArrayList<>();
    private List<String> asmInst = new ArrayList<>(List.of(".text"));

    private RegAlloc regMap = new RegAlloc();

    public void loadIR(List<Instruction> originInstructions) {
        // TODO: 读入前端提供的中间代码并生成所需要的信息
        for (var inst: originInstructions){
            // 遇到ret指令舍弃后面的指令
            if (inst.getKind().isReturn()) {
                processedInst.add(inst);
                break;
            }
            else if (inst.getKind().isUnary()) processedInst.add(inst);
            else {
                IRValue ls = inst.getLHS();
                IRValue rs = inst.getRHS();
                IRVariable res = inst.getResult();

                if (ls.isIRVariable() && rs.isIRVariable() ) processedInst.add(inst);
                else if (ls.isImmediate() && rs.isImmediate()) {
                    int tmp = 0;
                    if (inst.getKind() == InstructionKind.ADD) tmp = ((IRImmediate) ls).getValue() + ((IRImmediate) rs).getValue();
                    else if (inst.getKind() == InstructionKind.SUB) tmp = ((IRImmediate) ls).getValue() - ((IRImmediate) rs).getValue();
                    else if (inst.getKind() == InstructionKind.MUL) tmp = ((IRImmediate) ls).getValue() * ((IRImmediate) rs).getValue();
                    processedInst.add(Instruction.createMov(res, IRImmediate.of(tmp)));
                }
                else if (ls.isIRVariable() && rs.isImmediate()) {
                    if (inst.getKind() == InstructionKind.MUL) {
                        IRVariable tmp = IRVariable.temp();
                        processedInst.add(Instruction.createMov(tmp, rs));
                        processedInst.add(Instruction.createMul(res, tmp, ls));
                    }
                    else if (inst.getKind() == InstructionKind.SUB) {
                        IRImmediate tmp = IRImmediate.of(-((IRImmediate)rs).getValue());
                        processedInst.add(Instruction.createAdd(res, ls, tmp));
                    }
                    else processedInst.add(inst);
                }
                // ls是立即数
                else {
                    if (inst.getKind() == InstructionKind.ADD) processedInst.add(Instruction.createAdd(res,rs,ls));
                    else if (inst.getKind() == InstructionKind.SUB){
                        IRImmediate tmp = IRImmediate.of(-((IRImmediate) ls).getValue());
                        processedInst.add(Instruction.createAdd(res, rs, tmp));
                    }
                    else {
                        IRVariable tmp = IRVariable.temp();
                        processedInst.add(Instruction.createMov(tmp, ls));
                        processedInst.add(Instruction.createMul(res, tmp, rs));
                    }
                }
            }

        }
//        throw new NotImplementedException();
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
//        throw new NotImplementedException();
        for (int i=0;i<processedInst.size();i++) {
            Instruction inst = processedInst.get(i);
            String asm = "";
            switch (inst.getKind()) {
                case MOV -> {
                    IRValue val = inst.getFrom();
                    IRVariable res = inst.getResult();

                    if (val.isImmediate()) asm = String.format("\tli %s, %d", regMap.allocReg(res, processedInst, i).toString(), ((IRImmediate) val).getValue());
                    else asm = String.format("\tmv %s, %s", regMap.allocReg(res, processedInst, i).toString(), regMap.allocReg((IRVariable) val, processedInst, i).toString());
                }
                case RET -> {
                    IRValue retVal = inst.getReturnValue();
                    if (retVal.isImmediate())
                        asm = String.format("\tmv a0, %d", ((IRImmediate) retVal).getValue());
                    else
                        asm = String.format("\tmv a0, %s", regMap.allocReg((IRVariable) retVal, processedInst, i));
                }
                case ADD -> {
                    IRValue ls = inst.getLHS();
                    IRValue rs = inst.getRHS();
                    IRVariable res = inst.getResult();

                    Reg lsReg = regMap.allocReg((IRVariable) ls, processedInst, i);
                    Reg resReg = regMap.allocReg(res, processedInst, i);
                    if (rs.isImmediate()) asm = String.format("\taddi %s, %s, %d",resReg.toString(), lsReg.toString(), ((IRImmediate) rs).getValue());
                    else {
                        Reg rsReg = regMap.allocReg((IRVariable) rs, processedInst, i);
                        asm = String.format("\taddi %s, %s, %s",resReg.toString(), lsReg.toString(), rsReg.toString());
                    }
                }
                case SUB, MUL -> {
                    Reg lsReg = regMap.allocReg((IRVariable) inst.getLHS(), processedInst, i);
                    Reg resReg = regMap.allocReg(inst.getResult(), processedInst, i);
                    Reg rsReg = regMap.allocReg((IRVariable) inst.getRHS(), processedInst, i);
                    if (inst.getKind() == InstructionKind.SUB)
                        asm = String.format("\tsub %s, %s, %s", resReg.toString(), lsReg.toString(), rsReg.toString());
                    else
                        asm = String.format("\tmul %s, %s, %s", resReg.toString(), lsReg.toString(), rsReg.toString());
                }
            }
            asm += "\t\t# %s".formatted(inst.toString());
            asmInst.add(asm);
        }
    }


    /**
     * 输出汇编代码到文件
     *
     * @param path 输出文件路径
     */
    public void dump(String path) {
        // TODO: 输出汇编代码到文件
        FileUtils.writeLines(path, asmInst);
//        throw new NotImplementedException();
    }
}

