package com.calebleavell.jatui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class IOCapture implements AutoCloseable {
    ByteArrayInputStream input;
    Scanner scnr;
    ByteArrayOutputStream output;
    PrintStream strm;

    public IOCapture(String input)  {
        this.input = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        this.scnr = new Scanner(this.input);
        this.output = new ByteArrayOutputStream();
        this.strm = new PrintStream(output);
    }

    public IOCapture() {
        this("");
    }

    public Scanner getScanner() {
        return scnr;
    }

    public PrintStream getPrintStream() {
        return strm;
    }

    public String getOutput() {
        return output.toString(StandardCharsets.UTF_8);
    }


    @Override
    public void close() {
        scnr.close();
        strm.close();
    }
}
