package com.calebleavell.jatui;

/**
 * <p>Acknowledgement: Ansi Descriptions adapted from Christian Petersen (fnky) at: <br/>
 * <a href="https://gist.github.com/fnky/458719343aabd01cfb17a3a4f7296797">https://gist.github.com/fnky/458719343aabd01cfb17a3a4f7296797</a></p>
 */
public abstract class Ansi {

    public enum Sequences {
        /** Escape */
        ESC(0x1b),

        /** Control Sequence Inducer*/
        CSI(0x9b),

        /** Device Control String */
        DCS(0x90),

        /** Operating System Command */
        OSC(0x9d);

        public final int code;

        Sequences(int code) {
            this.code = code;
        }
    }

    public enum General {
        /** Terminal Bell */
        BEL(0x07),

        /** Backspace */
        BS(0x08),

        /** Horizontal Tab */
        HT(0x09),

        /** Linefeed */
        LF(0x0a),

        /** Vertical Tab */
        VT(0x0b),

        /** Formfeed (or New Page [NP]) */
        FF(0x0c),

        /** Carriage Return */
        CR(0x0d),

        /** Delete Character */
        DEL(0x7f);

        public final int code;

        General(int code) {
            this.code = code;
        }
    }

    // Cursor Controls

    /** moves cursor to home position (0, 0) */
    public static final String CURSOR_HOME = Sequences.ESC.code+"[H";

    /** moves cursor to line #, column # */
    public static String cursorToLineColumn(int line, int column) {
        return String.format("%d[%d;%dH", Sequences.ESC.code, line, column);
    }


    public String getString() {
        return string;
    }


    public static class StringBuilder {
        private String string = "";

        public StringBuilder addText(String text) {
            string += text;
            return this;
        }

        public String build() {

            return "UNIMPLEMENTED";
        }

    }
}
