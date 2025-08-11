package com.calebleavell.jatui;

import com.calebleavell.jatui.modules.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.Supplier;

public class Experimentation {
    public static void main(String[] args) throws IOException {

        List<String> list = new ArrayList<>(List.of("a", "b", "c"));
        list.remove(null);
        System.out.println(list);

//        // this will be false, since you're technically passing in 2 different lambdas
//        boolean one = TUIFunctionModule.Builder.equalTo(
//                new TUIFunctionModule.Builder("one", () -> System.out.println("Hello, World!")),
//                new TUIFunctionModule.Builder("one", () -> System.out.println("Hello, World!"))); // returns false
//
//        // this will also be false, since Java constructs a different method reference each time
//        boolean two = TUIFunctionModule.Builder.equalTo(
//                new TUIFunctionModule.Builder("two", Experimentation::myMethod),
//                new TUIFunctionModule.Builder("two", Experimentation::myMethod)); // returns false
//
//        Runnable run = () -> System.out.println("Hello, World!");
//
//        // this will be false since TUIFunctionModule.Builder converts Runnable to Supplier under the hood
//        boolean three = TUIFunctionModule.Builder.equalTo(
//                new TUIFunctionModule.Builder("three", run),
//                new TUIFunctionModule.Builder("three", run)); // returns false
//
//        Supplier<Object> sup = () -> "Hello, World!";
//
//        // this will be true since the same lambda is being referenced
//        boolean four = TUIFunctionModule.Builder.equalTo(
//                new TUIFunctionModule.Builder("four", sup),
//                new TUIFunctionModule.Builder("four", sup)); // returns true
//
//        List.of(one, two, three, four).forEach(System.out::println);

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

    public static Object myMethod() {
        return "Hello, World!";
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
