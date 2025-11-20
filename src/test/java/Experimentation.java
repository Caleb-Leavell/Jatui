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
import java.util.function.Supplier;

public class Experimentation {
    public static void main(String[] args) throws IOException {
        TUIApplicationModule app = new TUIApplicationModule.Builder("app").build();
        final char[] correct = {'a', 'e', 'i', 'o', 'u'};
        Supplier<char[]> supplyCorrect = () -> correct;
        var getPassword = new TUIModuleFactory.PasswordInput("get-password", "Your password: ", supplyCorrect)
                .addOnValidPassword(() -> System.out.println("You were correct."))
                .addOnInvalidPassword(() -> System.out.println("You were incorrect."))
                .storeInputAndMatch();

        app.setHome(getPassword);
        app.run();

        getPassword.cleanMemory();

        System.out.println(app.getInput("get-password-is-matched"));
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
