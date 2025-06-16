package com.calebleavell.jatui.ansi;

public class AnsiString {
    private String string = "";

    public AnsiString addText(String text) {
        string += text;
        return this;
    }

    public String getString() {

        return string;
    }
}
