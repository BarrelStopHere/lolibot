package com.kryang.lolibot.util;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Randoms {
    static final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    static final char[] NORMAL_CANDIDATES = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            .toCharArray();
    static final char[] SPECIAL_CANDIDATES = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789~!@#$%^&*()_+-={}[]"
            .toCharArray();
    static final char[] INT_CANDIDATES = "0123456789".toCharArray();
    private static final Random RAND = ThreadLocalRandom.current();

    /**
     * 生成指定长度的随机字符串（不包含特殊字符）
     *
     * @param length 字符串长度
     * @return
     */
    public static String random(int length) {
        return random(length, false);
    }

    /**
     * 生成指定长度的随机字符串
     *
     * @param length   字符串长度
     * @param specials true - 包含特殊字符, false - 不包含特殊字符
     * @return
     */
    public static String random(int length, boolean specials) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int j = RAND.nextInt(100000) % (specials ? SPECIAL_CANDIDATES.length : NORMAL_CANDIDATES.length);
            buf.append(specials ? SPECIAL_CANDIDATES[j] : NORMAL_CANDIDATES[j]);
        }
        return buf.toString();
    }

    /**
     * 生成指定长度的随机数字字符串
     *
     * @param length 字符串长度
     * @return
     */
    public static String randomInt(int length) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int j = RAND.nextInt(10000000) % INT_CANDIDATES.length;
            buf.append(INT_CANDIDATES[j]);
        }
        return buf.toString();
    }

    /**
     * 生成指定长度的随机字母字符串
     *
     * @param length 字符串长度
     * @return
     */
    public static String randomAlphabet(int length) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int j = RAND.nextInt(10000000) % ALPHABET.length;
            buf.append(ALPHABET[j]);
        }

        return buf.toString();
    }

    /**
     * 返回一个区间数据
     *
     * @param min 最小值
     * @param max 最大值
     * @return
     */
    public static int random(int min, int max) {
        if (max < min) return min;

        return RAND.nextInt(max - min) + min;
    }

    /**
     * 返回一个区间数据
     *
     * @param min 最小值（含）
     * @param max 最大值（含）
     * @return
     */
    public static int randomIncludeBoundary(int min, int max) {
        if (max < min) return min;

        return RAND.nextInt(9900000) % (max - min + 1) + min;
    }

    /**
     * 当前满足几分之一的概率
     */
    public static boolean oneOfPartValue(int val) {
        return val <= 1 || randomIncludeBoundary(1, val) % val == 0;
    }

    /**
     * 随机返回数组的索引下标
     */
    public static int indexOfListSize(int size) {
        return size <= 1 ? 0 : randomIncludeBoundary(1, size) % size;
    }

    /**
     * 返回true或者false
     */
    public static boolean random() {
        return random(0, 10000) % 2 == 0;
    }

    /**
     * 根据权重返回数组下标
     *
     * @param weights 数组中的值≥1
     */
    public static int weight(int[] weights) {
        if (weights == null || weights.length < 2) return 0;

        int w = 0;
        for (int x : weights) w += x;
        int v = random(0, w);
        w = 0;
        for (int i = 0; i < weights.length; i++) {
            w += weights[i];
            if (v <= w) return i;
        }

        return random() ? 0 : (weights.length - 1);
    }

}
