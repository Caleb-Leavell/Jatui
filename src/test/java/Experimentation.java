import com.calebleavell.jatui.modules.*;

import java.io.*;
import java.util.*;

import static org.fusesource.jansi.Ansi.ansi;

public class Experimentation {
    public static void main(String[] args) throws IOException {
        TUIApplicationModule app = new TUIApplicationModule.Builder("app").build();
        var getPassword = new TUIModuleFactory.PasswordInput("get-password", "Your password: ");

        app.setHome(getPassword);
        app.run();

        System.out.println("before clearing: " + new String(app.getInput("get-password-input", char[].class)));
        getPassword.clearPassword();
        System.out.println("after clearing: " + new String(app.getInput("get-password-input", char[].class)));

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
