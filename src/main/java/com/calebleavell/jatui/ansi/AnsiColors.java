package com.calebleavell.jatui.ansi;

public class AnsiColors {
    public static final String BLACK = AnsiSequences.ESC.code + "[30m";
    public static final String RED = AnsiSequences.ESC.code + "[31";
    public static final String GREEN = AnsiSequences.ESC.code + "[32";
    public static final String YELLOW = AnsiSequences.ESC.code + "[33m";
    public static final String BLUE = AnsiSequences.ESC.code + "[34";
    public static final String MAGENTA = AnsiSequences.ESC.code + "[35m";
    public static final String CYAN = AnsiSequences.ESC.code + "[36m";
    public static final String WHITE = AnsiSequences.ESC.code + "[37m";
    public static final String RESET = AnsiSequences.ESC.code + "[39m";

}
