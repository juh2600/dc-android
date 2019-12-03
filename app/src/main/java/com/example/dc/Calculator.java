package com.example.dc;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Stack;

public class Calculator {

    private int scale;
    private final RoundingMode roundingMode = RoundingMode.DOWN;

    public Calculator() {
        scale = 0;
    }

    public void setScale(int k) {
        scale = k;
    }

    public int getScale() {
        return scale;
    }

    public BigDecimal add(BigDecimal b, BigDecimal a) {
        return a.add(b);
    }

    /**
     * Subtracts one number from another.
     *
     * @param b subtrahend
     * @param a minuend
     * @return a - b
     */
    public BigDecimal subtract(BigDecimal b, BigDecimal a) {
        return a.subtract(b);
    }

    /**
     * Multiply two numbers, keeping the scale of the number whose scale is larger.
     */
    public BigDecimal multiply(BigDecimal b, BigDecimal a) {
        int precisionA = a.precision() - a.scale();
        int precisionB = b.precision() - b.scale();
        int precision = Math.max(precisionA, precisionB);
        int scaleA = a.scale();
        int scaleB = b.scale();
        int scale = Math.max(scaleA, scaleB);
        return a.multiply(b, new MathContext(scale + precision, roundingMode));
    }

    /**
     * Divide one number by another, using the current precision value as the scale of the quotient.
     *
     * @param b Divisor
     * @param a Dividend
     * @return a / b
     */
    public BigDecimal divide(BigDecimal b, BigDecimal a) {
        return a.divide(b, scale, roundingMode);
    }

    /**
     * According to the dc manual, the modulus is equivalent to the remainder
     * of the division that the regular division operator would perform.
     * If we are working in a precision greater than zero, this may produce
     * initially unexpected results. For example, 1%2 gives 1 when precision
     * is zero, but for any greater precision, 1/2 yields 0.5, so 1%2 gives 0.
     *
     * @param mod Modulus
     * @param a Dividend
     * @return a % b
     */
    public BigDecimal mod(BigDecimal mod, BigDecimal a) {
        return a.subtract(divide(mod, a).multiply(mod));
    }

    public BigDecimal pow(int pow, BigDecimal base) {
        return base.pow(pow);
    }

    public BigDecimal modexp(BigDecimal mod, int pow, BigDecimal base) {
        BigDecimal result = BigDecimal.ONE;
        for(int i = 0; i < pow; i++) {
            result = result.multiply(base);
            result = mod(mod, result).setScale(base.scale(), roundingMode);
        }
        return result.setScale(base.scale(), roundingMode);
    }

    /*
    public BigDecimal sqrt(BigDecimal a) {
        if(a.compareTo(BigDecimal.ZERO) < 0) throw new ArithmeticException();
        if(a.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        // haha no
    }
     */
}
