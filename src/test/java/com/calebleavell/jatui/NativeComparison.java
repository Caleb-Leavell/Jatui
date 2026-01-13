package com.calebleavell.jatui;

import com.calebleavell.jatui.modules.ApplicationModule;
import com.calebleavell.jatui.modules.ContainerModule;
import com.calebleavell.jatui.modules.ModuleFactory;
import com.calebleavell.jatui.modules.TextInputModule;
import com.calebleavell.jatui.templates.ConfirmationPrompt;
import com.calebleavell.jatui.templates.TextChain;

import java.util.Random;
import java.util.Scanner;

public class NativeComparison {
    public static void main(String[] args) {
        libraryApp();
    }

    static Random rand = new Random();

    public static int getRandomInt(int max) {
        return rand.nextInt(max) + 1;
    }

    public static void nativeApp() {
        Scanner scanner = new Scanner(System.in);

        while(true) {
            System.out.print("Maximum Number (or -1 to exit): ");
            int max;
            try {
                max = Integer.parseInt(scanner.nextLine());
            }
            catch(NumberFormatException e) {
                System.out.println("Invalid Number");
                continue;
            }

            if (max < 0) {
                System.out.print("Exit (y/n)? ");
                if (scanner.nextLine().equalsIgnoreCase("y")) break;
                else continue;
            }

            int n = getRandomInt(max);
            System.out.println("Generated Number: " + n);
        }

        System.out.println("Exiting...");
    }

    public static void libraryApp() {
        ApplicationModule app = ApplicationModule.builder("app").build();

        ConfirmationPrompt confirmExit = ConfirmationPrompt.builder("confirm-exit",
                        "Exit (y/n)? ") // also accepts yes/no as valid inputs by default
                .application(app)
                .addOnConfirm(app::terminate)
                .addOnDeny(app::restart);

        ContainerModule.Builder home = ContainerModule.builder("home")
                .addChildren(
                        TextInputModule.builder("max-input", "Maximum Number (or -1 to exit): ")
                                .addSafeHandler("random-number", input -> {
                                    int max = Integer.parseInt(input); // automatically reruns on RuntimeException
                                    if(max < 0) {
                                        app.navigateTo(confirmExit);
                                        return -1;
                                    }

                                    return getRandomInt(max);
                                }),
                        TextChain.builder("display-module-output")
                                .addText("Generated Number: ")
                                .addModuleOutput("random-number").newLine(),
                        ModuleFactory.restart("restart-app", app)
                );

        app.setHome(home);
        app.start();
    }
}
