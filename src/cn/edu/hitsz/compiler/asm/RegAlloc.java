package cn.edu.hitsz.compiler.asm;

import cn.edu.hitsz.compiler.ir.IRValue;
import cn.edu.hitsz.compiler.ir.IRVariable;
import cn.edu.hitsz.compiler.ir.Instruction;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;


class BMap<K, V> {
    private final Map<K, V> KVmap = new HashMap<>();
    private final Map<V, K> VKmap = new HashMap<>();

    public void removeByKey(K key) {
        VKmap.remove(KVmap.remove(key));
    }

    public void removeByValue(V value) {
        KVmap.remove(VKmap.remove(value));

    }

    public boolean containsKey(K key) {
        return KVmap.containsKey(key);
    }

    public boolean containsValue(V value) {
        return VKmap.containsKey(value);
    }

    public void replace(K key, V value) {
        // 对于双射关系, 将会删除交叉项
        removeByKey(key);
        removeByValue(value);
        KVmap.put(key, value);
        VKmap.put(value, key);
    }

    public V getByKey(K key) {
        return KVmap.get(key);
    }

    public K getByValue(V value) {
        return VKmap.get(value);
    }
}

public class RegAlloc {
    private BMap<IRVariable, Reg> allocMap = new BMap<>();
    public Reg varToReg(IRVariable var) {return allocMap.getByKey(var);}
    public IRVariable regToVar(Reg reg) {return allocMap.getByValue(reg);}
    public Reg allocReg(IRVariable var, List<Instruction> insts, int cur) {
        if (allocMap.containsKey(var)) return varToReg(var);

        // 寻找空闲寄存器
        for (var reg:Reg.values()) {
            if (!allocMap.containsValue(reg)) {
                allocMap.replace(var, reg);
                return varToReg(var);
            }
        }

        // 没有空闲寄存器
        var victimRegList = Arrays.stream(Reg.values()).collect(Collectors.toSet());
        for (;cur<insts.size();cur++) {
            for (var operands:insts.get(cur).getOperands()) {
                if (operands.isImmediate()) continue;
                victimRegList.remove(allocMap.getByKey((IRVariable) operands));
            }
        }

        if (!victimRegList.isEmpty()) {
            allocMap.replace(var, victimRegList.iterator().next());
            return varToReg(var);
        }
        throw new RuntimeException("No enough registers");
    }
}
