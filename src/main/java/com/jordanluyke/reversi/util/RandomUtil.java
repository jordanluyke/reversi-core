package com.jordanluyke.reversi.util;

import java.util.Random;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class RandomUtil {

    private static String eligibleCharacters = "2346789ABCDEFGHJLMNPQRTUVWXYZ";
    private static final Random random = new Random();

    public static String generateRandom(int characters) {
        StringBuilder r = new StringBuilder();
        for(int i=0; i<characters; i++)
            r.append(eligibleCharacters.charAt(((int) (random.nextDouble() * eligibleCharacters.length()))));
        return r.toString();
    }
}
