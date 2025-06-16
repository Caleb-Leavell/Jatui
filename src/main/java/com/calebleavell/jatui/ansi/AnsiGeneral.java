package com.calebleavell.jatui.ansi;

/**
 * <p>Acknowledgement: Ansi Descriptions adapted from Christian Petersen (fnky) at: <br/>
 * <a href="https://gist.github.com/fnky/458719343aabd01cfb17a3a4f7296797">https://gist.github.com/fnky/458719343aabd01cfb17a3a4f7296797</a></p>
 */
public enum AnsiGeneral {
    /** Terminal Bell */
    BEL((char) 0x07),

    /** Backspace */
    BS((char) 0x08),

    /** Horizontal Tab */
    HT((char) 0x09),

    /** Linefeed */
    LF((char) 0x0a),

    /** Vertical Tab */
    VT((char) 0x0b),

    /** Formfeed (or New Page [NP]) */
    FF((char) 0x0c),

    /** Carriage Return */
    CR((char) 0x0d),

    /** Delete Character */
    DEL((char) 0x7f);

    public final char code;

    AnsiGeneral(char code) {
        this.code = code;
    }

}
