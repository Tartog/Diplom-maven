package com.company;

import java.util.Arrays;

public class Cipher {

    private final byte[][] sBlock;

    public Cipher() {
        sBlock = CipherParams.block1;
    }

    public Cipher(byte[][] sBlock) {
        this.sBlock = sBlock;
    }

    public byte[] encrypt(byte[] input, byte[] keys) {
        byte[] output = Arrays.copyOf(input, input.length);

        for (int i = 0; i <= input.length - 8; i += 8) {
            for (int q = 0; q < 3; q++) {
                for (int w = 0; w < 8; w++) {
                    int key = keyWrap(w, keys);
                    step(i, output, key);
                }
            }
            for (int w = 7; w >= 0; w--) {
                int key = keyWrap(w, keys);
                step(i, output, key);
                if (w == 0) {
                    byte[] lastArray2 = new byte[8];
                    System.arraycopy(output, output.length - i - 8, lastArray2, 4, 4);
                    System.arraycopy(output, output.length - i - 4, lastArray2, 0, 4);
                    System.arraycopy(lastArray2, 0, output, output.length - i - 8, 8);
                }
            }
        }

        return output;
    }

    public byte[] decrypt(byte[] input, byte[] keys) {
        byte[] output = Arrays.copyOf(input, input.length);
        int w;

        for (int i = 0; i <= input.length - 8; i += 8) {
            for (w = 0; w < 8; w++) {
                int key = keyWrap(w, keys);
                step(i, output, key);
            }
            for (int q = 0; q < 3; q++) {
                for (w = 7; w >= 0; w--) {
                    int key = keyWrap(w, keys);
                    step(i, output, key);
                }
                if (w == -1 && q == 2) {
                    byte[] lastArray = new byte[8];
                    System.arraycopy(output, output.length - i - 8, lastArray, 4, 4);
                    System.arraycopy(output, output.length - i - 4, lastArray, 0, 4);
                    System.arraycopy(lastArray, 0, output, output.length - i - 8, 8);
                }
            }
        }

        return output;
    }

    private void step(int startIndex, byte[] input, int key) {
        int n2 = input[input.length - startIndex - 1] << 24 | (input[input.length - startIndex - 2] & 0xFF) << 16
                | (input[input.length - startIndex - 3] & 0xFF) << 8 | (input[input.length - startIndex - 4] & 0xFF);
        int n1 = input[input.length - startIndex - 5] << 24 | (input[input.length - startIndex - 6] & 0xFF) << 16
                | (input[input.length - startIndex - 7] & 0xFF) << 8 | (input[input.length - startIndex - 8] & 0xFF);
        int s = ((n1 + key) & 0xFFFFFFFF);

        s = replace(s);
        s = shiftLeft11(s);
        s = s ^ n2;
        byte[] n1s = join(n1, s);
        for (int j = 7; j >= 0; j--) {
            input[input.length - 1 - (startIndex + j)] = n1s[j];
        }
    }

    private byte[] join(int n1, int n2) {
        byte[] bytes = new byte[8];

        for (int j = 0; j < 4; j++) {
            bytes[j] = (byte) ((n1 >> 24 - (j * 8)) & 0xFF);
        }
        for (int j = 4; j < 8; j++) {
            bytes[j] = (byte) ((n2 >> 24 - (j * 8)) & 0xFF);
        }

        return bytes;
    }

    private int replace(int n) {
        int xTest = 0;

        for (int i = 0, j = 0; i <= 28; i += 4, j++) {
            xTest += (sBlock[j][(byte) ((n >> i) & 0xF)]) << (i);
        }

        return xTest;
    }

    private int shiftLeft11(int a) {
        int shift = 11;

        a = (a >>> (32 - shift)) | a << shift;

        return a;
    }

    private int keyWrap(int w, byte[] keys) {
        int key = keys[w * 4 + 3] << 24 | (keys[w * 4 + 2] & 0xFF) << 16 | (keys[w * 4 + 1] & 0xFF) << 8
                | (keys[w * 4] & 0xFF);

        return key;
    }
}