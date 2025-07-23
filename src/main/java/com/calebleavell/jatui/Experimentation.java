package com.calebleavell.jatui;

import com.calebleavell.jatui.modules.*;

import java.io.*;
import java.util.Scanner;

public class Experimentation {
    public static void main(String[] args) throws IOException {
        try(IOCapture io = new IOCapture("Bob");) {
            TUIApplicationModule app = new TUIApplicationModule.Builder("test-app")
                    .addChildren(
                            new TUITextInputModule.Builder("input", "What is your name? "),
                            new TUIModuleFactory.LineBuilder("output")
                                    .addText("Hello, ").addModuleOutput("input").addText("!").newLine())
                    .enableAnsi(false)
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .build();

            app.run();
            System.out.println(app);
            System.out.println(io.getOutput());
        }

//        byte[] bytes = "Hello, World!".getBytes();
//        InputStream input = new FileInputStream("test.txt");
//        Scanner scnr = new Scanner(input);
//
//        MyClass test;
//
//        try(PrintStream output = new PrintStream("test2.txt");) {
//            test = new MyClass(output);
//        }
//
//        test.output();

    }

    static class MyClass {
        PrintStream stream;

        public MyClass(PrintStream strm) {
            stream = strm;
        }

        public void output() {
            stream.println("test");
        }
    }

    public static void inputOutputTest(Scanner input, PrintStream output) {
        output.println(input.next());
    }
}
