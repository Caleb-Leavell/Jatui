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

import com.calebleavell.jatui.modules.*;

import java.io.*;
import java.util.*;

public class Experimentation {
    public static void main(String[] args) throws IOException {

        var app = new ApplicationModule.Builder("app").build();
        var text = new FunctionModule.Builder("func", () -> System.out.println(app.getCurrentRunningBranch()));
        var home = new ContainerModule.Builder("home");

        home.addChildren(text, ModuleFactory.restart("restart-home", app, "home"));

        app.setHome(home);
        app.run();

//        TUIModule a = new TextModule.Builder("a", "b").build();
//
//        a.run();

    }

    public static ContainerModule.Builder LineWithDot(String name, int dotX) {
        String line = "   ".repeat(Math.max(0, dotX)) + "[##]";
        return new ContainerModule.Builder(name).addChildren(
                new TextModule.Builder(name+"dot", line),
                new FunctionModule.Builder(name+"sleep", () -> {
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
