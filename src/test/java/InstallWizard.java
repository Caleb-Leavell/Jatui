import com.calebleavell.jatui.modules.ApplicationModule;
import com.calebleavell.jatui.modules.ContainerModule;
import com.calebleavell.jatui.modules.FunctionModule;
import com.calebleavell.jatui.modules.TextModule;
import com.calebleavell.jatui.templates.ConfirmationPrompt;

public class InstallWizard {
    public static void main(String[] args) {
        // basic installation wizard that gets confirmation from the user and
        // handles success/failure of installation

        // the core application
        ApplicationModule app = ApplicationModule.builder("app").build();

        // defining the messages for success/failure
        String SUCCESS_MESSAGE = "Package installed successfully!";
        String FAILURE_MESSAGE = "Package installation failed.";

        // performs installation, checks whether it was successful,
        // then displays the corresponding result.
        ContainerModule.Builder install = ContainerModule.builder("install")
                .addChildren(
                        TextModule.builder("display-installing", "installing..."),
                        FunctionModule.builder("do-install", InstallWizard::installPackage),
                        FunctionModule.builder("install-result-handler", () -> {
                            // retrieves the success state that was returned from the "installPackage"
                            // method. Handles the potential null-case.
                            Boolean input = app.getInput("do-install", Boolean.class);

                            // returns the corresponding string depending on whether the installation was successful
                            return (input != null && input) ? SUCCESS_MESSAGE : FAILURE_MESSAGE;
                        }),

                        // displays the value returned by the "install-result-handler" module,
                        // which was either SUCCESS_MESSAGE or FAILURE message
                        TextModule.builder("display-result", "install-result-handler")
                                .outputType(TextModule.OutputType.DISPLAY_APP_STATE)
                )
                .application(app);
        // notice that we don't build the modules (other than "app")
        // modules are lazily built at runtime by the scheduler

        // gets confirmation from the user then delegates to the "install" module
        ContainerModule.Builder installWizard = ContainerModule.builder("install-wizard")
                .addChildren(
                        ConfirmationPrompt.builder("confirm-install",
                                "Are you sure you want to install the package (y/n)? ")
                                .addOnConfirm(() -> app.navigateTo(install))
                                .addOnDeny(app::terminate)
                );

        app.setHome(installWizard);
        app.start();
    }

    public static boolean installPackage() {
        // install logic
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        return true; // signals "successful" installation
    }
}
