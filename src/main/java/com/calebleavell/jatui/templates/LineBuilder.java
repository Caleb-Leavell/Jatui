package com.calebleavell.jatui.templates;


import com.calebleavell.jatui.core.DirectedGraphNode;
import com.calebleavell.jatui.modules.TUIModule;
import com.calebleavell.jatui.modules.TextModule;
import org.fusesource.jansi.Ansi;

import java.util.Objects;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * <p>LineBuilder simplifies chaining text together that is meant to live on the same line.</p>
 * <p>Ansi is supported with method overloads.</p>
 * <p><strong>Usage:</strong>
 * <pre><code>
 * LineBuilder text = new LineBuilder("name")
 *     .addText("Regular text: ")
 *     // this will display what the inputted module outputs
 *     .addModuleOutput("This string is the name of another module")
 *     .newLine() // end of line 1
 *     .addText("Text on the next line.")
 *     .newLine(); // end of line 2
 * </code></pre>
 */
public class LineBuilder extends TUIModule.Template<LineBuilder> {
    private TextModule.Builder current;
    protected int iterator;

    public LineBuilder(String name) {
        super(LineBuilder.class, name);
    }

    protected LineBuilder() {
        super(LineBuilder.class);
    }

    /**
     * Gets a fresh instance of this type of Builder.
     *  Note, this is intended only for copying utility and may have unknown consequences if used in other ways.
     * @return A fresh, empty instance.
     */
    @Override
    protected LineBuilder createInstance() {
        return new LineBuilder();
    }


    @Override
    public void shallowCopy(LineBuilder original) {
        this.current = original.current.getCopy();
        this.iterator = original.iterator;
        super.shallowCopy(original);
    }

    public LineBuilder addText(TextModule.Builder text) {
        logger.trace("adding text to LineBuilder \"{}\" that displays \"{}\" (output type is \"{}\")", getName(), text.getText(), text.getOutputType());
        main.addChild(text);
        current = text;
        iterator ++;
        return self();
    }

    public LineBuilder addText(String text, Ansi ansi) {
        this.addText(new TextModule.Builder(main.getName() + "-" + iterator, text)
                .setAnsi(ansi)
                .printNewLine(false));
        return self();
    }

    public LineBuilder addText(String text) {
        return addText(text, ansi());
    }

    public LineBuilder addModuleOutput(String moduleName, Ansi ansi) {
        this.addText(new TextModule.Builder(main.getName() + "-" + iterator, moduleName)
                .setOutputType(TextModule.OutputType.DISPLAY_MODULE_OUTPUT)
                .printNewLine(false)
                .setAnsi(ansi));
        return self();
    }

    public LineBuilder addModuleOutput(String moduleName) {
        return this.addModuleOutput(moduleName, ansi());
    }

    public LineBuilder newLine() {
        logger.trace("adding newline to LineBuilder \"{}\"", getName());
        if(current != null) current.printNewLine(true);
        return self();
    }

    protected TextModule.Builder getCurrent() {
        return current;
    }

    /**
     * <p>Checks equality for properties given by the builder.</p>
     *
     * <p>For LineBuilder, this includes: </p>
     * <ul>
     *     <li><strong>current</strong> (the most recent text module added) </li>
     *     <li><strong>iterator</strong> (the number of text modules added so far) </li>
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
    public boolean shallowStructuralEquals(LineBuilder first, LineBuilder second) {
        if(first == second) return true;
        if(first == null || second == null) return false;

        if(!TUIModule.Builder.structuralEquals(first.current, second.current)) return false;

        return Objects.equals(first.iterator, second.iterator) &&
                super.shallowStructuralEquals(first, second);
    }
}