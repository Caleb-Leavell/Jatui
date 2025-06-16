package com.calebleavell.jatui.ansi;

public enum AnsiErase {

    /** Erase in display (same as ESC[0J), clears from cursor to end of screen. */
    DISPLAY_TO_END(String.format("%c[J", (char) AnsiSequences.ESC.code)),

    /** Erase from cursor until end of screen. */
    DISPLAY_TO_END_0(String.format("%c[0J", AnsiSequences.ESC.code)),

    /** Erase from cursor to beginning of screen. */
    DISPLAY_TO_BEGINNING(String.format("%c[1J", AnsiSequences.ESC.code)),

    /** Erase entire screen. */
    DISPLAY_ENTIRE_SCREEN(String.format("%c[2J", AnsiSequences.ESC.code)),

    /** Erase saved lines (clears scrollback buffer). */
    DISPLAY_SAVED_LINES(String.format("%c[3J", AnsiSequences.ESC.code)),

    /** Erase in line (same as ESC[0K), clears from cursor to end of line. */
    LINE_TO_END(String.format("%c[K", AnsiSequences.ESC.code)),

    /** Erase from cursor to end of line. */
    LINE_TO_END_0(String.format("%c[0K", AnsiSequences.ESC.code)),

    /** Erase start of line to the cursor. */
    LINE_TO_BEGINNING(String.format("%c[1K", AnsiSequences.ESC.code)),

    /** Erase the entire line. */
    LINE_ENTIRE(String.format("%c[2K", AnsiSequences.ESC.code));

    private final String sequence;

    AnsiErase(String sequence) {
        this.sequence = sequence;
    }

    public String getSequence() {
        return sequence;
    }
}
