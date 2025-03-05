package com.github.xpenatan.gdx.backends.teavm.utils;

public class KeyCodes {
    public static final int KEY_A = 65;
    public static final int KEY_B = 66;
    public static final int KEY_C = 67;
    public static final int KEY_D = 68;
    public static final int KEY_E = 69;
    public static final int KEY_F = 70;
    public static final int KEY_G = 71;
    public static final int KEY_H = 72;
    public static final int KEY_I = 73;
    public static final int KEY_J = 74;
    public static final int KEY_K = 75;
    public static final int KEY_L = 76;
    public static final int KEY_M = 77;
    public static final int KEY_N = 78;
    public static final int KEY_O = 79;
    public static final int KEY_P = 80;
    public static final int KEY_Q = 81;
    public static final int KEY_R = 82;
    public static final int KEY_S = 83;
    public static final int KEY_T = 84;
    public static final int KEY_U = 85;
    public static final int KEY_V = 86;
    public static final int KEY_W = 87;
    public static final int KEY_X = 88;
    public static final int KEY_Y = 89;
    public static final int KEY_Z = 90;
    public static final int KEY_ZERO = 48;
    public static final int KEY_ONE = 49;
    public static final int KEY_TWO = 50;
    public static final int KEY_THREE = 51;
    public static final int KEY_FOUR = 52;
    public static final int KEY_FIVE = 53;
    public static final int KEY_SIX = 54;
    public static final int KEY_SEVEN = 55;
    public static final int KEY_EIGHT = 56;
    public static final int KEY_NINE = 57;
    public static final int KEY_NUM_ZERO = 96;
    public static final int KEY_NUM_ONE = 97;
    public static final int KEY_NUM_TWO = 98;
    public static final int KEY_NUM_THREE = 99;
    public static final int KEY_NUM_FOUR = 100;
    public static final int KEY_NUM_FIVE = 101;
    public static final int KEY_NUM_SIX = 102;
    public static final int KEY_NUM_SEVEN = 103;
    public static final int KEY_NUM_EIGHT = 104;
    public static final int KEY_NUM_NINE = 105;
    public static final int KEY_NUM_MULTIPLY = 106;
    public static final int KEY_NUM_PLUS = 107;
    public static final int KEY_NUM_MINUS = 109;
    public static final int KEY_NUM_PERIOD = 110;
    public static final int KEY_NUM_DIVISION = 111;
    public static final int KEY_ALT = 18;
    public static final int KEY_BACKSPACE = 8;
    public static final int KEY_CTRL = 17;
    public static final int KEY_DELETE = 46;
    public static final int KEY_DOWN = 40;
    public static final int KEY_END = 35;
    public static final int KEY_ENTER = 13;
    public static final int KEY_ESCAPE = 27;
    public static final int KEY_HOME = 36;
    public static final int KEY_LEFT = 37;
    public static final int KEY_PAGEDOWN = 34;
    public static final int KEY_PAGEUP = 33;
    public static final int KEY_RIGHT = 39;
    public static final int KEY_SHIFT = 16;
    public static final int KEY_TAB = 9;
    public static final int KEY_UP = 38;
    public static final int KEY_F1 = 112;
    public static final int KEY_F2 = 113;
    public static final int KEY_F3 = 114;
    public static final int KEY_F4 = 115;
    public static final int KEY_F5 = 116;
    public static final int KEY_F6 = 117;
    public static final int KEY_F7 = 118;
    public static final int KEY_F8 = 119;
    public static final int KEY_F9 = 120;
    public static final int KEY_F10 = 121;
    public static final int KEY_F11 = 122;
    public static final int KEY_F12 = 123;
    public static final int KEY_WIN_KEY_FF_LINUX = 0;
    public static final int KEY_MAC_ENTER = 3;
    public static final int KEY_PAUSE = 19;
    public static final int KEY_CAPS_LOCK = 20;
    public static final int KEY_SPACE = 32;
    public static final int KEY_PRINT_SCREEN = 44;
    public static final int KEY_INSERT = 45;
    public static final int KEY_NUM_CENTER = 12;
    public static final int KEY_WIN_KEY = 224;
    public static final int KEY_WIN_KEY_LEFT_META = 91;
    public static final int KEY_WIN_KEY_RIGHT = 92;
    public static final int KEY_CONTEXT_MENU = 93;
    public static final int KEY_MAC_FF_META = 224;
    public static final int KEY_NUMLOCK = 144;
    public static final int KEY_SCROLL_LOCK = 145;
    public static final int KEY_FIRST_MEDIA_KEY = 166;
    public static final int KEY_LAST_MEDIA_KEY = 183;
    public static final int KEY_WIN_IME = 229;
    public static final int KEY_OPEN_BRACKET = 219;
    public static final int KEY_CLOSE_BRACKET = 221;

    public static boolean isArrowKey(int code) {
        switch (code) {
            case 37:
            case 38:
            case 39:
            case 40:
                return true;
            default:
                return false;
        }
    }

    public static int maybeSwapArrowKeysForRtl(int code, boolean isRtl) {
        if (isRtl) {
            if (code == 39) {
                code = 37;
            } else if (code == 37) {
                code = 39;
            }
        }

        return code;
    }

    private KeyCodes() {
    }
}