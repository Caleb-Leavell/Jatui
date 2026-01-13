/*
    Copyright (c) 2026 Caleb Leavell

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
 */

package com.calebleavell.jatui.templates;

import com.calebleavell.jatui.modules.ModuleTemplate;
import com.calebleavell.jatui.modules.TUIModule;
import com.calebleavell.jatui.modules.TextInputModule;

import java.util.*;
import java.util.function.Supplier;

/**
 * Handles getting confirmation from the user. Includes specifying allowed responses
 * (y/yes/n/no) by default, as well on what to do when the user confirms or denies.
 * <br><br>
 * Example Usage:
 * <pre><code>
 *             ConfirmationPrompt confirmExit = ConfirmationPrompt.builder("confirm-exit",
 *                 "Are you sure you want to exit (y/n)? ")
 *                 .setApplication(app)
 *                 .addOnConfirm(app::terminate)
 *                 .addOnDeny(app::restart);
 * </code></pre>
 */
public class ConfirmationPrompt extends ModuleTemplate<ConfirmationPrompt> {

    /** The inputs that are considered confirmation from the user (yes/y) by default. **/
    private final Set<String> confirm = new HashSet<>(List.of("yes", "y"));

    /** The inputs that are considered denial from the user (no/n) by default. **/
    private final Set<String> deny = new HashSet<>(List.of("no", "n"));

    /** Increments the name of the confirmation handlers to ensure they each have a unique name. **/
    private int confirmIter = 0;

    /** Increments the name of the denial handlers to ensure they each have a unique name. **/
    private int denyIter = 0;

    protected ConfirmationPrompt(String name, String displayText) {
        super(ConfirmationPrompt.class, name);
        main.addChild(
                TextInputModule.builder(name + "-input", displayText)
        );
    }

    /**
     * Constructs a new {@link ConfirmationPrompt} builder.
     *
     * @param name The name of the builder.
     * @param displayText The text that displays before getting input (e.g., "Are you sure? ").
     * @return The new builder.
     */
    public static ConfirmationPrompt builder(String name, String displayText) {
        return new ConfirmationPrompt(name, displayText);
    }

    protected ConfirmationPrompt() {
        super(ConfirmationPrompt.class);
    }

    /**
     * Gets a fresh instance of this type of Builder.
     *  Note, this is intended only for copying utility and may have unknown consequences if used in other ways.
     * @return A fresh, empty instance.
     */
    @Override
    protected ConfirmationPrompt createInstance() {
        return new ConfirmationPrompt();
    }

    /**
     * Sets the strings that will count as a confirmation. Note, this clears the current confirmation strings. <br>
     * Note: spaces are stripped from both these strings and the input,
     * and both these strings and the input are converted to lowercase.
     * @param confirmStrings The strings that count as confirmation
     * @return self
     */
    public ConfirmationPrompt validConfirm(String... confirmStrings) {
        confirm.clear();
        for(String str : confirmStrings) {
            confirm.add(str.toLowerCase().strip().replace("\n", ""));
        }
        return self();
    }

    /**
     * Gets the inputs that are considered confirmation from the user (yes/y) by default.
     * @return The valid confirmation strings.
     **/
    public Set<String> getValidConfirm() {
        return confirm;
    }

    /**
     * Sets the strings that will count as a denial. Note, this clears the current denial strings <br>
     * Note: spaces are stripped from both these strings and the input,
     * and both these strings and the input are converted to lowercase.
     * @param denyStrings The strings that count as denial
     * @return self
     */
    public ConfirmationPrompt validDeny(String... denyStrings) {
        deny.clear();
        for(String str : denyStrings) {
            deny.add(str.toLowerCase().strip().replace("\n", ""));
        }
        return self();
    }

    /**
     * Gets the inputs that are considered denial from the user (no/n) by default.
     * @return The valid denial strings.
     **/
    public Set<String> getValidDeny() {
        return deny;
    }

    /**
     * Specifies what to do when the user confirms.
     *
     * @param logic The logic to run on the input.
     * @return self
     */
    public ConfirmationPrompt addOnConfirm(Runnable logic) {
        return this.addOnConfirm(this.name + "-onConfirm-" + confirmIter++, () -> {
            logic.run();
            return null;
        });
    }

    /**
     * Specifies what to do when the user confirms.
     *
     * @param name The input identifier to access what {@code logic} returns ({@link com.calebleavell.jatui.modules.ApplicationModule#getInput(String)}).
     * @param logic The logic to run on the input.
     * @return self
     */
    public ConfirmationPrompt addOnConfirm(String name, Supplier<?> logic) {
        TextInputModule.Builder input = this.getChild(this.name+"-input",
                TextInputModule.Builder.class);

        input.addSafeHandler(name, s -> {
            String in = s.toLowerCase().replace("\n", "").replace(String.format("%n"), "");
            if(confirm.contains(in)) {
                return logic.get();
            } else if(deny.contains(in))
                return null;
            else throw new IllegalArgumentException("Invalid confirmation input: " + s); // this will be caught by the safe handler
        });

        return self();
    }

    /**
     * Specifies what to do when the user denies.
     *
     * @param name The input identifier to access what {@code logic} returns ({@link com.calebleavell.jatui.modules.ApplicationModule#getInput(String)}).
     * @param logic The logic to run on the input.
     * @return self
     */
    public ConfirmationPrompt addOnDeny(String name, Supplier<?> logic) {
        TextInputModule.Builder input = this.getChild(this.name+"-input",
                TextInputModule.Builder.class);

        input.addSafeHandler(name, s -> {
            String in = s.toLowerCase().strip().replace("\n", "").replace(String.format("%n"), "");
            if(confirm.contains(in))
                return null;
            else if(deny.contains(in)) {
                return logic.get();
            } else throw new IllegalArgumentException("Invalid confirmation input: " + s);
        });

        return self();
    }

    /**
     * Specifies what to do when the user denies.
     *
     * @param logic The logic to run on the input.
     * @return self
     */
    public ConfirmationPrompt addOnDeny(Runnable logic) {
        return this.addOnDeny(this.name + "-onDeny-" + denyIter++,() -> {
            logic.run();
            return null;
        });
    }


    /**
     * Sets the name as provided by {@link TUIModule.Builder#name(String)}. Also updates the name of the input module to
     * ensure correct input handling.
     *
     * @param name The unique name of this module.
     * @return self
     */
    @Override
    public ConfirmationPrompt name(String name) {
        if(this.name != null) this.getChild(this.name + "-input").name(name + "-input");
        super.name(name);
        return self();
    }

    /**
     * Checks equality for properties given by the builder. For {@link ConfirmationPrompt}, this includes
     * {@code confirm}, {@code deny}, {@code confirmIter}, and {@code denyIter},
     * as well as other requirements provided by {@link TUIModule.Builder#shallowStructuralEquals(TUIModule.Builder, TUIModule.Builder)}.
     */
    @Override
    public boolean shallowStructuralEquals(ConfirmationPrompt first, ConfirmationPrompt second) {
        if(first == second) return true;
        if(first == null || second == null) return false;

        return  Objects.equals(first.confirm, second.confirm) &&
                Objects.equals(first.deny, second.deny) &&
                Objects.equals(first.confirmIter, second.confirmIter) &&
                Objects.equals(first.denyIter, second.denyIter) &&
                super.shallowStructuralEquals(first, second);
    }

    /**
     * Copies valid confirmation and denial inputs, the current {@code iters}
     * for naming handlers,
     * and delegates to {@link TUIModule.Builder#shallowCopy(TUIModule.Builder)}.
     * @param original The builder to copy from.
     */
    @Override
    public void shallowCopy(ConfirmationPrompt original) {
        this.confirm.clear();
        this.confirm.addAll(original.confirm);
        this.deny.clear();
        this.deny.addAll(original.deny);
        this.confirmIter = original.confirmIter;
        this.denyIter = original.denyIter;
        super.shallowCopy(original);
    }
}