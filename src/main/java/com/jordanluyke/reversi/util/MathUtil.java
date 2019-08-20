package com.jordanluyke.reversi.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class MathUtil {
    private static final Logger logger = LogManager.getLogger(MathUtil.class);

    public final static MathContext context = new MathContext(25);

    public static BigDecimal[] normalize(int value, int min, int max) {
        return normalize(new BigDecimal(value), min, max);
    }

    public static BigDecimal[] normalize(BigDecimal value, int min, int max) {
        BigDecimal _min = new BigDecimal(min);
        BigDecimal _max = new BigDecimal(max);
        if(value.compareTo(_min) < 0 || value.compareTo(_max) > 0) {
            logger.error("Value out of bounds: {} {} {}", value, _min, _max);
            throw new RuntimeException("Value out of bounds");
        }
        BigDecimal radianFraction = new BigDecimal(Math.PI).multiply(new BigDecimal(2)).multiply(value.subtract(_min).divide(_max.subtract(_min), context));
        return new BigDecimal[]{new BigDecimal(Math.cos(radianFraction.doubleValue())), new BigDecimal(Math.sin(radianFraction.doubleValue()))};
    }

    public static BigDecimal denormalize(BigDecimal x, BigDecimal y, int min, int max) {
        BigDecimal _min = new BigDecimal(min);
        BigDecimal _max = new BigDecimal(max);
        BigDecimal radians = new BigDecimal(Math.atan2(y.doubleValue(), x.doubleValue()));
        BigDecimal twoPi = new BigDecimal(Math.PI).multiply(new BigDecimal(2));
        if(radians.signum() == -1)
            radians = radians.add(twoPi);
        return radians.divide(twoPi, context).multiply(_max.subtract(_min)).add(_min);
    }
}
