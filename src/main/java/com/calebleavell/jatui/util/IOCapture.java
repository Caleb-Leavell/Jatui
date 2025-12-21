/*
 * Copyright (c) 2025 Caleb Leavell
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
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
