package com.calebleavell.jatui.ansi;

/**
 * <p>Acknowledgement: Ansi Descriptions adapted from Christian Petersen (fnky) at: <br/>
 * <a href="https://gist.github.com/fnky/458719343aabd01cfb17a3a4f7296797">https://gist.github.com/fnky/458719343aabd01cfb17a3a4f7296797</a></p>
 */
public enum AnsiSequences {
    /** Escape */
    ESC((char) 0x1b),

    /** Control Sequence Inducer*/
    CSI((char) 0x9b),

    /** Device Control String */
    DCS((char) 0x90),

    /** Operating System Command */
    OSC((char) 0x9d);

    public final char code;

    AnsiSequences(char code) {
        this.code = code;
    }
}
