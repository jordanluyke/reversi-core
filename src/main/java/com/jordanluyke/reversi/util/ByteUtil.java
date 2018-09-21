package com.jordanluyke.reversi.util;

import io.netty.buffer.ByteBuf;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class ByteUtil {

    public static byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    public static byte[] getBytes(ByteBuf buf) {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        buf.release();
        return bytes;
    }
}
