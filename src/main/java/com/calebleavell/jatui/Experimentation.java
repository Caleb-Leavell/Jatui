package com.calebleavell.jatui;

import com.calebleavell.jatui.modules.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.fusesource.jansi.Ansi.ansi;

public class Experimentation {
    public static void main(String[] args) throws IOException {
        Scanner scnr = new Scanner(System.in);
        Random rand = new Random();

        APP: while(true) {
            System.out.print("Maximum Number (or -1 to exit): ");
            String input = scnr.nextLine().trim();
            int max = -1;
            try {
                max = Integer.parseInt(input);
            }
            catch(NumberFormatException ex) {
                System.out.println("Error: Error: input integer (your input might be too large)");
                continue;
            }

            if(max == -1) break;
            if(max <= 0) {
                System.out.println("Error: input integer must be greater than 0");
                break;
            }

            int randomNum = rand.nextInt(max);

            System.out.println("Generated Number: " + ansi().bold().fgRgb(220, 180, 0).a(randomNum).reset());

            while(true) {
                System.out.println(ansi().bold().a("[1]").reset() + " Generate another number");
                System.out.println(ansi().bold().a("[2]").reset() + " Exit");
                System.out.print("Your choice: ");

                int choice = -1;

                try {
                    choice = Integer.parseInt(scnr.nextLine());
                }
                catch(NumberFormatException e) {
                    System.out.println("Error: input must be integer");
                    continue;
                }

                switch(choice) {
                    case 1: continue APP;
                    case 2: break APP;
                    default: System.out.println("Error: input 1 or 2");
                }
            }
        }

        System.out.println(ansi().fgRgb(200, 100, 100).a("Exiting...").reset());
    }

    public static TUIContainerModule.Builder LineWithDot(String name, int dotX) {
        String line = "   ".repeat(Math.max(0, dotX)) + "[##]";
        return new TUIContainerModule.Builder(name).addChildren(
                new TUITextModule.Builder(name+"dot", line),
                new TUIFunctionModule.Builder(name+"sleep", () -> {
                    try {
                        System.out.flush();
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }));
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
