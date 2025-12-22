package com.calebleavell.jatui.templates;

import com.calebleavell.jatui.core.DirectedGraphNode;
import com.calebleavell.jatui.modules.ModuleTemplate;
import com.calebleavell.jatui.modules.TUIModule;
import com.calebleavell.jatui.modules.TextInputModule;

import java.util.*;
import java.util.function.Supplier;

public class ConfirmationPrompt extends ModuleTemplate<ConfirmationPrompt> {

    private final Set<String> confirm = new HashSet<>(List.of("yes", "y"));
    private final Set<String> deny = new HashSet<>(List.of("no", "n"));
    private int confirmIter = 1;
    private int denyIter = 2;


    public ConfirmationPrompt(String name, String displayText) {
        super(ConfirmationPrompt.class, name);
        main.addChild(
                new TextInputModule.Builder(name + "-input", displayText)
        );
    }

    protected ConfirmationPrompt() {
        super(ConfirmationPrompt.class);
    }

    @Override
    protected ConfirmationPrompt createInstance() {
        return new ConfirmationPrompt();
    }

    /**
     * Sets the strings that will count as a confirmation. Note, this clears the current default confirmation strings <br>
     * Note: spaces are stripped from both these strings and the input,
     * and both these strings and teh input are converted to lowercase.
     * @param confirmStrings The strings that count as confirmation
     * @return self
     */
    public ConfirmationPrompt setValidConfirm(String... confirmStrings) {
        confirm.clear();
        if(confirmStrings.length == 0) return self();
        confirm.addAll(Arrays.asList(confirmStrings));

        return self();
    }

    public Set<String> getValidConfirm() {
        return confirm;
    }

    /**
     * Sets the strings that will count as a denial. Note, this clears the current default denial strings <br>
     * Note: spaces are stripped from both these strings and the input,
     * and both these strings and teh input are converted to lowercase.
     * @param denyStrings The strings that count as denial
     * @return self
     */
    public ConfirmationPrompt setValidDeny(String... denyStrings) {
        deny.clear();
        if(denyStrings.length == 0) return self();
        deny.addAll(Arrays.asList(denyStrings));

        return self();
    }

    public Set<String> getValidDeny() {
        return deny;
    }

    public ConfirmationPrompt addOnConfirm(Runnable logic) {
        return this.addOnConfirm(this.name + "-onConfirm-" + confirmIter++, () -> {
            logic.run();
            return null;
        });
    }

    public ConfirmationPrompt addOnConfirm(String name, Supplier<?> logic) {
        TextInputModule.Builder input = this.getChild(this.name+"-input",
                TextInputModule.Builder.class);

        input.addSafeHandler(name, s -> {
            String in = s.toLowerCase().replace("\n", "").replace(String.format("%n"), "");
            if(confirm.contains(in)) {
                return logic.get();
            } else if(deny.contains(in))
                return null;
            else throw new RuntimeException(); // this will be caught by the safe handler
        });

        return self();
    }

    public ConfirmationPrompt addOnDeny(Runnable logic) {
        return this.addOnDeny(this.name + "-onDeny-" + denyIter++,() -> {
            logic.run();
            return null;
        });
    }


    public ConfirmationPrompt addOnDeny(String name, Supplier<?> logic) {
        TextInputModule.Builder input = this.getChild(this.name+"-input",
                TextInputModule.Builder.class);

        input.addSafeHandler(name, s -> {
            String in = s.toLowerCase().strip().replace("\n", "").replace(String.format("%n"), "");
            if(confirm.contains(in))
                return null;
            else if(deny.contains(in)) {
                return logic.get();
            } else throw new RuntimeException();
        });

        return self();
    }

    @Override
    public ConfirmationPrompt setName(String name) {
        if(this.name != null) this.getChild(this.name + "-input").setName(name + "-input");
        super.setName(name);
        return self();
    }

    /**
     * <p>Checks equality for properties given by the builder.</p>
     *
     * <p>For ConfirmationPrompt, this includes: </p>
     * <ul>
     *     <li><strong>confirm (set)</strong></li>
     *     <li><strong>deny (set)</strong></li>
     *     <li><strong>confirmIter</strong></li>
     *     <li><strong>denyIter</strong></li>
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
    public boolean shallowStructuralEquals(ConfirmationPrompt first, ConfirmationPrompt second) {
        if(first == second) return true;
        if(first == null || second == null) return false;

        return  Objects.equals(first.confirm, second.confirm) &&
                Objects.equals(first.deny, second.deny) &&
                Objects.equals(first.confirmIter, second.confirmIter) &&
                Objects.equals(first.denyIter, second.denyIter) &&
                super.shallowStructuralEquals(first, second);
    }

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