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

package com.calebleavell.jatui.templates;

import com.calebleavell.jatui.core.DirectedGraphNode;
import com.calebleavell.jatui.modules.*;

import java.io.Console;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Handles collecting, validating, and cleaning passwords from the user. Zeroes out the password
 * char array after collection unless explicitly set to do otherwise via {@link PasswordInput#storeInput()}
 * (<b>not recommended</b>).
 * <br><br>
 * Example usage:
 * <pre><code>
 * ApplicationModule app = ApplicationModule.builder("app").build();
 *
 * TextModule.Builder homePage = TextModule.builder("home", "Home Page");
 *
 * Supplier&lt;char[]&gt; supplyPassword = "password"::toCharArray; // would likely reference some database in practice
 *
 * PasswordInput myInput = PasswordInput.builder("pw-input", "Password: ", supplyPassword)
 *   .addOnValidPassword(() -> app.navigateTo(homePage))
 *   .addOnInvalidPassword(app::restart);
 *
 * app.setHome(myInput);
 * app.run();
 * </code></pre>
 *
 * Output:
 * <pre>
 * Password: incorrect
 * Password: password
 * Home Page
 * Exiting...
 * </pre>
 */
public class PasswordInput extends ModuleTemplate<PasswordInput> {
    /**
     * The text to display before getting input (e.g., "Password: ")
     */
    private String displayText;

    /**
     * The supplier of the correct password for input validation.
     * It is up to the user of the class what this supplier references
     * (e.g., a database, some hardcoded value, etc.).
     */
    private Supplier<char[]> passwordSupplier;

    /**
     * The logic to perform if the user's input is the correct password.
     */
    final List<FunctionModule.Builder> onValidPassword = new ArrayList<>();

    /**
     * The logic to perform if the user's input is <i>not</i> the correct password.
     */
    final List<FunctionModule.Builder> onInvalidPassword = new ArrayList<>();
    private boolean storeInput = false;
    private boolean storeMatch = false;

    /**
     * Constructs a new {@link PasswordInput} builder.
     *
     * @param name The name of the module.
     * @param displayText The text to display before getting input (e.g., "Password: ").
     * @param passwordSupplier The supplier of the correct password for input validation.
     *                         There is no restriction on what it may reference
     *                         (e.g., a database, some hardcoded value, etc.).
     *                         The supplier should return a fresh copy of the array
     *                         per invocation, as the array is zeroed out after use.
     */
    protected PasswordInput(String name, String displayText, Supplier<char[]> passwordSupplier) {
        super(PasswordInput.class, name);
        this.displayText = displayText;
        this.passwordSupplier = passwordSupplier;
        main.addChild(FunctionModule.builder(name+"-input", this::createPasswordInput));
    }

    /**
     * Constructs a new {@link PasswordInput} builder.
     *
     * @param name The name of the module.
     * @param displayText The text to display before getting input (e.g., "Password: ").
     * @param passwordSupplier The supplier of the correct password for input validation.
     *                         There is no restriction on what it may reference
     *                         (e.g., a database, some hardcoded value, etc.).
     */
    public static PasswordInput builder(String name, String displayText, Supplier<char[]> passwordSupplier) {
        return new PasswordInput(name, displayText, passwordSupplier);
    }

    protected PasswordInput() {
        super(PasswordInput.class);
    }

    /**
     * Gets a fresh instance of this type of Builder.
     *  Note, this is intended only for copying utility and may have unknown consequences if used in other ways.
     * @return A fresh, empty instance.
     */
    @Override
    protected PasswordInput createInstance() {
        return new PasswordInput();}

    /**
     * Cleans any memory that may have been stored from this password input
     * (including the input and whether the password was a match).
     * <br><br>
     * <b>Note:</b> Memory is automatically cleaned unless you explicitly set this
     * module to store it via {@link PasswordInput#storeInput()}, {@link PasswordInput#storeIfMatched()},
     * or {@link PasswordInput#storeInputAndMatch()}.
     */
    public void cleanMemory() {
        if(application == null) return;
        char[] password = application.getInput(this.name+"-input", char[].class);
        if(password != null) Arrays.fill(password, ' ');
        password = null;

        application.forceUpdateInput(this.name+"-is-matched", null);
    }

    /**
     * Validates a password that's been stored in memory.
     * <br><br>
     * <b>Note:</b> this will always return false if the behavior hasn't been set to store the password
     * via {@link PasswordInput#storeInput()}.
     * @param correct The actual password.
     * @return Whether the inputted password matches the correct password, if it exists and was saved.
     */
    public boolean validatePassword(char[] correct) {
        if(this.application == null) return false;
        char[] inputtedPassword = application.getInput(this.name+"-input", char[].class);

        return matches(inputtedPassword, correct);
    }

    /**
     * Specifies behavior for when the user inputs the correct password.
     * Input is validated automatically, but input is not re-collected
     * automatically on an invalid password.
     *
     * @param onValidPassword Logic to execute when the user inputs the correct password.
     * @return self
     */
    public PasswordInput addOnValidPassword(Runnable onValidPassword) {
        this.onValidPassword.add(FunctionModule.builder("", () -> {
            onValidPassword.run();
            return null;
        }));
        return self();
    }

    /**
     * Specifies behavior for when the user inputs the correct password.
     * Input is validated automatically, but input is not re-collected
     * automatically on an invalid password.
     *
     * @param name The name of the {@link FunctionModule} being built internally, as well as the
     *             input identifier for the return value of {@code onValidPassword} that
     *             can be retrieved at {@link ApplicationModule#getInput(String)}.
     * @param onValidPassword Logic to execute when the user inputs the correct password.
     *                        The return value is stored in the {@link ApplicationModule}
     *                        and can be retrieved via {@code name}.
     * @return self
     */
    public PasswordInput addOnValidPassword(String name, Supplier<?> onValidPassword) {
        this.onValidPassword.add(FunctionModule.builder(name, onValidPassword));
        return self();
    }

    /**
     * Specifies behavior for when the user inputs an incorrect password.
     * Input is validated automatically, but input is not re-collected
     * automatically on an invalid password.
     *
     * @param onInvalidPassword Logic to execute when the user inputs an incorrect password.
     * @return self
     */
    public PasswordInput addOnInvalidPassword(Runnable onInvalidPassword) {
        this.onInvalidPassword.add(FunctionModule.builder("", () -> {
            onInvalidPassword.run();
            return null;
        }));
        return self();
    }


    /**
     * Specifies behavior for when the user inputs an incorrect password.
     * Input is validated automatically, but input is not re-collected
     * automatically on an invalid password.
     *
     * @param name The name of the {@link FunctionModule} being built internally, as well as the
     *             input identifier for the return value of {@code onValidPassword} that
     *             can be retrieved at {@link ApplicationModule#getInput(String)}.
     * @param onInvalidPassword Logic to execute when the user inputs an incorrect password.
     *                        The return value is stored in the {@link ApplicationModule}
     *                        and can be retrieved via {@code name}.
     * @return self
     */
    public PasswordInput addOnInvalidPassword(String name, Supplier<?> onInvalidPassword) {
        this.onInvalidPassword.add(FunctionModule.builder(name, onInvalidPassword));
        return self();
    }

    /**
     * Configures the module to immediately clean the input
     * and validation result after running. This is the default
     * setting. <br>
     * The other settings are {@link PasswordInput#storeIfMatched()},
     * {@link PasswordInput#storeInput()}, and {@link PasswordInput#storeInputAndMatch()},
     * but these are generally not recommended.
     * @return self
     */
    public PasswordInput cleanImmediately() {
        this.storeInput = false;
        this.storeMatch = false;
        return self();
    }

    /**
     * Configures the module immediately clean the input
     * but store whether the input was a match. This is generally
     * not required.<br>
     * Nothing will be stored if this module isn't tied to
     * an application (e.g., via {@link TUIModule.Builder#application(ApplicationModule)}). <br>
     * The other settings are {@link PasswordInput#cleanImmediately()},
     * {@link PasswordInput#storeInput()}, and {@link PasswordInput#storeInputAndMatch()}.
     * @return self
     */
    public PasswordInput storeIfMatched() {
        this.storeInput = false;
        this.storeMatch = true;
        return self();
    }

    /**
     * Configures the module to immediately clean whether
     * the input was a match but store the input itself in the
     * {@link ApplicationModule} state (unencrypted).
     * This is generally not recommended.<br>
     * The application state (included stored passwords) can be fully cleaned via
     * {@link ApplicationModule#resetMemory()}. <br>
     * Nothing will be stored if this module isn't tied to
     * an application (e.g., via {@link TUIModule.Builder#application(ApplicationModule)}). <br>
     * The other settings are {@link PasswordInput#cleanImmediately()},
     * {@link PasswordInput#storeIfMatched()}, and {@link PasswordInput#storeInputAndMatch()}.
     * @return self
     */
    public PasswordInput storeInput() {
        this.storeInput = true;
        this.storeMatch = false;
        return self();
    }

    /**
     * Configures the module store the input, as well
     * as whether the input was a match, in the
     * {@link ApplicationModule} state (unencrypted).
     * This is generally not recommended.<br>
     * The application state (included stored passwords) can be fully cleaned via
     * {@link ApplicationModule#resetMemory()}. <br>
     * Nothing will be stored if this module isn't tied to
     * an application (e.g., via {@link TUIModule.Builder#application(ApplicationModule)}). <br>
     * The other settings are {@link PasswordInput#cleanImmediately()},
     * {@link PasswordInput#storeIfMatched()}, and {@link PasswordInput#storeInput()}.
     * @return self
     */
    public PasswordInput storeInputAndMatch() {
        this.storeInput = true;
        this.storeMatch = true;
        return self();
    }

    /**
     * Sets the name of this module.
     *
     * @param name The unique name of this module.
     * @return self
     * @implNote sets the names of the {@link FunctionModule} that collects input to stay
     * consistent with the new name.
     */
    @Override
    public PasswordInput name(String name) {
        if(this.name == null) return super.name(name);
        FunctionModule.Builder input = main.getChild(this.name+"-input", FunctionModule.Builder.class);
        input.name(name + "-input");
        return super.name(name);
    }

    /**
     * Set the text to display before getting input (e.g., "Password: ").
     * @param displayText The new text to display.
     * @return self
     */
    public PasswordInput setDisplayText(String displayText) {
        this.displayText = displayText;
        return self();
    }

    /**
     * {@code displayText} is the text to display before getting input (e.g., "Password: ").
     * @return {@code displayText}
     */
    public String getDisplayText() {return displayText;}

    /**
     * Builds a new {@link ContainerModule} with the configuration from this builder.
     * Ensures the {@link FunctionModule} that collects input is up-to-date
     * with the most recent configuration.
     *
     * @return self
     */
    @Override
    public ContainerModule build() {
        // update the input function to reflect the most recent name, input, and application
        FunctionModule.Builder input = main.getChild(this.name+"-input", FunctionModule.Builder.class);
        input.function(this::createPasswordInput);
        return super.build();
    }

    /**
     * Handles, collecting, validating, and cleaning the inputted password.
     *
     * @return null, or the inputted password if configured.
     */
    private char[] createPasswordInput() {
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

            match = this.matches(input, correct);
        }
        finally {
            if(input != null && !this.storeInput) Arrays.fill(input, ' ');
            if(correct != null) Arrays.fill(correct, ' ');
        }

        executeHandlers(match);
        return saveStateIfConfigured(input, match);
    }

    /**
     * Checks if two char arrays are equivalent without exiting early
     * (helps avoid timing attacks but can't provide cryptographic guarantees).
     *
     * @return Whether the two arrays are equivalent.
     */
    private boolean matches(char[] input, char[] correct) {
        boolean match = true;

        if (input == null || correct == null || correct.length != input.length) match = false;
        else {
            for(int i = 0; i < input.length; i ++) {
                match &= (input[i] == correct[i]);
            }
        }

        return match;
    }

    private void executeHandlers(boolean match) {
        List<FunctionModule.Builder> functions = match ? onValidPassword : onInvalidPassword;
        for(FunctionModule.Builder func : functions) {
            func.application(this.application); // application could be null but that's ok
            func.build().start();
        }
    }

    private char[] saveStateIfConfigured(char[] input, boolean match) {
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
    }

    /**
     * Checks equality for properties given by the builder. For {@link PasswordInput}, this includes
     * {@code onValidPassword}, {@code onInvalidPassword}, {@code displayText},
     * {@code storeInput}, and {@code storeMatch},
     * as well as other requirements provided by {@link TUIModule.Builder#shallowStructuralEquals(TUIModule.Builder, TUIModule.Builder)}.
     */
    @Override
    public boolean shallowStructuralEquals(PasswordInput first, PasswordInput second) {
        if(first == second) return true;
        if(first.getClass() != second.getClass()) return false;

        // note: these fields are guaranteed to be non-null
        if(first.onValidPassword.size() != second.onValidPassword.size()) return false;
        if(first.onInvalidPassword.size() != second.onInvalidPassword.size()) return false;
        for(int i = 0; i < first.onValidPassword.size(); i ++) {
            if(!TUIModule.Builder.structuralEquals(first.onValidPassword.get(i), second.onValidPassword.get(i))) {
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

    /**
     * Copies {@code displayText}, {@code onValidPassword}, {@code onInvalidPassword},
     * {@code storeInput}, and {@code storeMatch},
     * and delegates to {@link TUIModule.Builder#shallowCopy(TUIModule.Builder)}.
     * @param original The builder to copy from.
     */
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
