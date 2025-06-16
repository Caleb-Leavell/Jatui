package com.calebleavell.jatui.ansi;

/**
 * <p>Acknowledgement: Ansi Descriptions adapted from Christian Petersen (fnky) at: <br/>
 * <a href="https://gist.github.com/fnky/458719343aabd01cfb17a3a4f7296797">https://gist.github.com/fnky/458719343aabd01cfb17a3a4f7296797</a></p>
 */
public class AnsiCursor {
    private AnsiCursor() {}

    private static final char ESC = AnsiSequences.ESC.code;

    /**
     * <p>moves cursor to home position (0, 0) </p>
     * <p>"ESC[H"</p>
     */
    public static final String HOME = ESC+"[H";

    /**
     * <p>Moves cursor to line #, column # </p>
     * @param line The target line (1-based).
     * @param column The target column (1-based).
     * @return "ESC[{line};{column}H"
     */
    public static String toLineColumn(int line, int column) {
        if (line < 1 || column < 1) {
            throw new IllegalArgumentException("Line and column must be 1 or greater.");
        }
        // ESC[<line>;<column>H
        return String.format("%c[%d;%dH", ESC, line, column);
    }

    /**
     * <p>Moves cursor up # lines</p>
     * @param lines The number of lines to move up.
     * @return "ESC[{lines}A"
     */
    public static String moveUp(int lines) {
        if (lines < 0) {
            throw new IllegalArgumentException("Number of lines must be non-negative.");
        }
        // ESC[<lines>A
        return String.format("%c[%dA", ESC, lines);
    }

    /**
     * <p>Moves cursor down # lines</p>
     * @param lines The number of lines to move down.
     * @return "ESC[{lines}B"
     */
    public static String moveDown(int lines) {
        if (lines < 0) {
            throw new IllegalArgumentException("Number of lines must be non-negative.");
        }
        // ESC[<lines>B
        return String.format("%c[%dB", ESC, lines);
    }

    /**
     * <p>Moves cursor right # columns</p>
     * @param columns The number of columns to move right.
     * @return "ESC[{columns}C"
     */
    public static String moveRight(int columns) {
        if (columns < 0) {
            throw new IllegalArgumentException("Number of columns must be non-negative.");
        }
        // ESC[<columns>C
        return String.format("%c[%dC", ESC, columns);
    }

    /**
     * <p>Moves cursor left # columns</p>
     * @param columns The number of columns to move left.
     * @return "ESC[{columns}D"
     */
    public static String moveLeft(int columns) {
        if (columns < 0) {
            throw new IllegalArgumentException("Number of columns must be non-negative.");
        }
        // ESC[<columns>D
        return String.format("%c[%dD", ESC, columns);
    }

    /**
     * <p>Moves cursor to beginning of next line, # lines down</p>
     * @param lines The number of lines to move down.
     * @return "ESC[{lines}E"
     */
    public static String moveNextLine(int lines) {
        if (lines < 0) {
            throw new IllegalArgumentException("Number of lines must be non-negative.");
        }
        // ESC[<lines>E
        return String.format("%c[%dE", ESC, lines);
    }

    /**
     * <p>Moves cursor to beginning of previous line, # lines up</p>
     * @param lines The number of lines to move up.
     * @return "ESC[{lines}F"
     */
    public static String movePreviousLine(int lines) {
        if (lines < 0) {
            throw new IllegalArgumentException("Number of lines must be non-negative.");
        }
        // ESC[<lines>F
        return String.format("%c[%dF", ESC, lines);
    }

    /**
     * <p>Moves cursor to column #</p>
     * @param column The target column (1-based).
     * @return "ESC[{column}G"
     */
    public static String toColumn(int column) {
        if (column < 1) {
            throw new IllegalArgumentException("Column must be 1 or greater.");
        }
        // ESC[<column>G
        return String.format("%c[%dG", ESC, column);
    }

    /**
     * <p>Requests cursor position (reports as ESC[#;#R)</p>
     * @return "ESC[6n"
     */
    public static String requestCursorPosition() {
        // ESC[6n
        return String.format("%c[6n", ESC);
    }

    /**
     * <p>Moves cursor one line up, scrolling if needed (DEC)</p>
     * @return "ESC M"
     */
    public static String scrollUp() {
        // ESC M
        return String.format("%cM", ESC);
    }

    /**
     * <p>Saves cursor position (DEC)</p>
     * @return "ESC 7"
     */
    public static String savePositionDEC() {
        // ESC 7
        return String.format("%c7", ESC);
    }

    /**
     * <p>Restores the cursor to the last saved position (DEC)</p>
     * @return "ESC 8"
     */
    public static String restorePositionDEC() {
        // ESC 8
        return String.format("%c8", ESC);
    }

    /**
     * <p>Saves cursor position (SCO)</p>
     * @return "ESC[s"
     */
    public static String savePositionSCO() {
        // ESC[s
        return String.format("%c[s", ESC);
    }

    /**
     * <p>Restores the cursor to the last saved position (SCO)</p>
     * @return "ESC[u"
     */
    public static String restorePositionSCO() {
        // ESC[u
        return String.format("%c[u", ESC);
    }
}
