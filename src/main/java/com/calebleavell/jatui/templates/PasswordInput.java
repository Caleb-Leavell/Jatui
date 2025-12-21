package com.calebleavell.jatui.templates;

import com.calebleavell.jatui.core.DirectedGraphNode;
import com.calebleavell.jatui.modules.TUIModule;
import com.calebleavell.jatui.modules.ContainerModule;
import com.calebleavell.jatui.modules.FunctionModule;

import java.io.Console;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class PasswordInput extends TUIModule.Template<PasswordInput> {
    String displayText;
    private Supplier<char[]> passwordSupplier;

    final List<FunctionModule.Builder> onValidPassword = new ArrayList<>();
    final List<FunctionModule.Builder> onInvalidPassword = new ArrayList<>();
    private boolean storeInput = false;
    private boolean storeMatch = false;

    public PasswordInput(String name, String displayText, Supplier<char[]> passwordSupplier) {
        super(PasswordInput.class, name);
        this.displayText = displayText;
        this.passwordSupplier = passwordSupplier;
        main.addChild(new FunctionModule.Builder(name+"-input", createPasswordInput()));
    }

    protected PasswordInput() {
        super(PasswordInput.class);
    }

    @Override
    protected PasswordInput createInstance() {return new PasswordInput();}

    /**
     * Cleans any memory that may have been stored from this password input
     * (including the input and whether the password was a match). <br>
     * Note: Memory is automatically cleaned unless you explicitly set this
     * module to store it via {@link PasswordInput#storeInput()}, {@link PasswordInput#storeIfMatched()},
     * or {@link PasswordInput#storeInputAndMatch()}.
     */
    public void cleanMemory() {
        char[] password = application.getInput(this.name+"-input", char[].class);
        if(password != null) Arrays.fill(password, ' ');
        password = null;

        application.forceUpdateInput(this.name+"-is-matched", null);
    }

    /**
     * Validates a password that's been stored in memory. <br>
     * Note: this will always return false if the behavior hasn't been set to store the password.
     * @param correct The actual password
     * @return Whether the inputted password matches the correct password, if it exists and was saved
     */
    public boolean validatePassword(char[] correct) {
        char[] inputtedPassword = application.getInput(this.name+"-input", char[].class);
        if (inputtedPassword == null || correct == null || correct.length != inputtedPassword.length) return false;

        for(int i = 0; i < inputtedPassword.length; i ++) {
            if(inputtedPassword[i] != correct[i]) return false;
        }

        return true;
    }

    public PasswordInput addOnValidPassword(Runnable onValidPassword) {
        this.onValidPassword.add(new FunctionModule.Builder("", () -> {
            onValidPassword.run();
            return null;
        }));
        return self();
    }

    public PasswordInput addOnValidPassword(String name, Supplier<?> onValidPassword) {
        this.onValidPassword.add(new FunctionModule.Builder(name, onValidPassword));
        return self();
    }

    public PasswordInput addOnInvalidPassword(Runnable onInvalidPassword) {
        this.onInvalidPassword.add(new FunctionModule.Builder("", () -> {
            onInvalidPassword.run();
            return null;
        }));
        return self();
    }

    public PasswordInput addOnInvalidPassword(String name, Supplier<?> onInvalidPassword) {
        this.onInvalidPassword.add(new FunctionModule.Builder(name, onInvalidPassword));
        return self();
    }

    public PasswordInput cleanImmediately() {
        this.storeInput = false;
        this.storeMatch = false;
        return self();
    }

    public PasswordInput storeIfMatched() {
        this.storeInput = false;
        this.storeMatch = true;
        return self();
    }

    public PasswordInput storeInput() {
        this.storeInput = true;
        this.storeMatch = false;
        return self();
    }

    public PasswordInput storeInputAndMatch() {
        this.storeInput = true;
        this.storeMatch = true;
        return self();
    }

    @Override
    public PasswordInput setName(String name) {
        if(this.name == null) return super.setName(name);
        FunctionModule.Builder input = main.getChild(this.name+"-input", FunctionModule.Builder.class);
        input.setName(name + "-input");
        return super.setName(name);
    }

    public PasswordInput setDisplayText(String displayText) {
        this.displayText = displayText;
        return self();
    }

    public String getDisplayText() {return displayText;}

    @Override
    public ContainerModule build() {
        // update the input function to reflect the most recent name, input, and application
        FunctionModule.Builder input = main.getChild(this.name+"-input", FunctionModule.Builder.class);
        input.setFunction(createPasswordInput());
        return super.build();
    }

    private Supplier<char[]> createPasswordInput() {
        return () -> {
            Console console = System.console();

            this.getPrintStream().print(this.displayText);

            boolean match = true;
            char[] input = new char[0];
            char[] correct = new char[0];
            try {
                // just reads the input normally if the Scanner was set to something custom or if Console is null
                // otherwise, read as a password from console
                if(this.getScanner() != TUIModule.DEFAULT_SCANNER || console == null) {
                    input = this.getScanner().nextLine().toCharArray();
                }
                else {
                    input = console.readPassword();
                }

                correct = this.passwordSupplier.get();

                if (input == null || correct == null || correct.length != input.length) match = false;
                else {
                    for(int i = 0; i < input.length; i ++) {
                        if (input[i] != correct[i]) {
                            match = false;
                            break;
                        }
                    }
                }
            }
            finally {
                if(input != null && !this.storeInput) Arrays.fill(input, ' ');
                if(correct != null) Arrays.fill(correct, ' ');
            }

            List<FunctionModule.Builder> functions = match ? onValidPassword : onInvalidPassword;
            for(FunctionModule.Builder func : functions) {
                func.setApplication(this.application); // application could be null but that's ok
                func.build().run();
            }

            if(this.application == null) {
                match = false; // helps avoid brute-forcing by removing match from memory (low severity threat)
                return null;
            }
            else {
                if(this.storeMatch) this.application.forceUpdateInput(this.name+"-is-matched", match);
                match = false;
                if(this.storeInput) return input != null ? input : new char[0];
                else return null;
            }
        };
    }

    /**
     * <p>Checks equality for properties given by the builder.</p>
     *
     * <p>For ConfirmationPrompt, this includes: </p>
     * <ul>
     *     <li><strong>displayText</strong></li>
     *     <li><strong>onValidPassword</strong></li>
     *     <li><strong>onInvalidPassword</strong></li>
     *     <li><strong>storeInput</strong></li>
     *     <li><strong>storeMatch</strong></li>
     *     <li>name</li>
     *     <li>application</li>
     *     <li>children</li>
     *     <li>ansi</li>
     *     <li>scanner</li>
     *     <li>printStream</li>
     *     <li>enableAnsi</li>
     * </ul>
     *
     * <p>Note: Runtime properties (e.g., currentRunningChild, terminated), are not considered. Children are also not considered here,
     *  but are considered in equals()
     * @param first The first NumberedList to compare
     * @param second The second NumberedList to compare
     * @return {@code true} if {@code first} and {@code second} are equal according to builder-provided properties
     *
     * @implNote
     * This is the {@code Function<TUIModule<?>, TUIModule.Builder<?>, Boolean>} that is passed into {@link DirectedGraphNode#structuralEquals(DirectedGraphNode)}
     */
    @Override
    public boolean shallowStructuralEquals(PasswordInput first, PasswordInput second) {
        if(first == second) return true;
        if(first.getClass() != second.getClass()) return false;

        // note: these fields are guaranteed to be non-null
        if(first.onValidPassword.size() != second.onValidPassword.size()) return false;
        if(first.onInvalidPassword.size() != second.onInvalidPassword.size()) return false;
        for(int i = 0; i < first.onInvalidPassword.size(); i ++) {
            if(!TUIModule.Builder.structuralEquals(first.onInvalidPassword.get(i), second.onInvalidPassword.get(i))) {
                return false;
            }
        }
        for(int i = 0; i < first.onInvalidPassword.size(); i ++) {
            if(!TUIModule.Builder.structuralEquals(first.onInvalidPassword.get(i), second.onInvalidPassword.get(i))) {
                return false;
            }
        }

        return  Objects.equals(first.displayText, second.displayText)
                && Objects.equals(first.storeInput, second.storeInput)
                && Objects.equals(first.storeMatch, second.storeMatch)
                && super.shallowStructuralEquals(first, second);
    }

    @Override
    public void shallowCopy(PasswordInput original) {
        this.displayText = original.displayText;
        this.onValidPassword.clear();
        this.onInvalidPassword.clear();
        for(FunctionModule.Builder module: original.onValidPassword) {
            this.onValidPassword.add(module.getCopy());
        }
        for(FunctionModule.Builder module: original.onInvalidPassword) {
            this.onInvalidPassword.add(module.getCopy());
        }
        this.storeInput = original.storeInput;
        this.storeMatch = original.storeMatch;
        super.shallowCopy(original);
    }
}
