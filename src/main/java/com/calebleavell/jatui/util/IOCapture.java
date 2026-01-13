/*
    Copyright (c) 2026 Caleb Leavell

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
 */

package com.calebleavell.jatui.util;

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
