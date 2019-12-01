package com.example.dc;

import java.math.BigDecimal;
import java.util.Stack;

public class Calculator {

    public void performOperation(Stack<Character> stack) {
        switch(stack.pop()) {
            case '+':
                //TODO: Add
                break;
            case '-':
                //TODO: Subtract
                break;
            case '*':
                //TODO: Multiply
                break;
            case '/':
                //TODO: Divide
                break;
        }
    }

    public BigDecimal add(BigDecimal a, BigDecimal b) {
        return a.add(b);
    }

    public BigDecimal subtract(BigDecimal a, BigDecimal b) {
        return a.subtract(b);
    }

    public BigDecimal multiply(BigDecimal a, BigDecimal b) {
        return a.multiply(b);
    }

    public BigDecimal divide(BigDecimal a, BigDecimal b) {
        return a.divide(b);
    }

    public BigDecimal mod(BigDecimal a, BigDecimal b) {
        //I just woke up but i think this works??
        BigDecimal remainder = a.subtract(b);
        while(remainder.compareTo(BigDecimal.ZERO) > 0) {
            if(remainder.subtract(b).compareTo(BigDecimal.ZERO) < 0) {
                return remainder;
            } else {
                remainder.subtract(b);
            }
        }
        return remainder;
    }
}
