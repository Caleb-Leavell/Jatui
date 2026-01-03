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

import com.calebleavell.jatui.modules.ModuleTemplate;
import com.calebleavell.jatui.modules.TUIModule;
import com.calebleavell.jatui.modules.TextModule;
import org.fusesource.jansi.Ansi;

import java.io.PrintStream;
import java.util.Objects;

import static org.fusesource.jansi.Ansi.ansi;
/**
 * LineBuilder simplifies chaining text together that is meant to live on the same line.
 * Ansi is supported with method overloads (e.g., {@link TextChain#addText(String, Ansi)}).
 * <br><br>
 * <strong>Usage:</strong>
 * <pre><code>
 * LineBuilder text = LineBuilder.builder("name")
 *     .addText("Regular text: ")
 *     // this will display what the inputted module outputs
 *     .addModuleOutput("This string is the name of another module")
 *     .newLine() // end of line 1
 *     .addText("Text on the next line.")
 *     .newLine(); // end of line 2
 * </code></pre>
 */
public class TextChain extends ModuleTemplate<TextChain> {

    /**
     * A reference of the most recently added text is saved
     * to call {@link TextModule.Builder#printNewLine(boolean)} if
     * {@link TextChain#newLine()} is called.
     */
    private TextModule.Builder current;

    /**
     * Appended to the name of every new {@link TextModule} and incremented.
     * The name of every text module follows "[{@code LineBuilder name}]-main-[{@code iterator}]".
     */
    protected int iterator;

    public TextChain(String name) {
        super(TextChain.class, name);
    }

    /**
     * Constructs a new {@link TextChain} builder.
     *
     * @param name The name of the builder.
     * @return The new builder.
     */
    public static TextChain builder(String name) {
        return new TextChain(name);
    }

    protected TextChain() {
        super(TextChain.class);
    }

    /**
     * Gets a fresh instance of this type of Builder.
     *  Note, this is intended only for copying utility and may have unknown consequences if used in other ways.
     * @return A fresh, empty instance.
     */
    @Override
    protected TextChain createInstance() {
        return new TextChain();
    }

    /**
     * Copies the reference to the most recently added module and the naming
     * iterator,
     * and delegates to {@link TUIModule.Builder#shallowCopy(TUIModule.Builder)}.
     * @param original The builder to copy from.
     */
    @Override
    public void shallowCopy(TextChain original) {
        this.current = original.current.getCopy();
        this.iterator = original.iterator;
        super.shallowCopy(original);
    }

    /**
     * Adds {@code text} as a child of this module.
     * @param text The {@link TextModule} to add.
     * @return self
     * @implNote Adds {@code text} as a child of {@code main} rather than directly to this module.
     */
    public TextChain addText(TextModule.Builder text) {
        logger.trace("adding text to LineBuilder \"{}\" that displays \"{}\" (output type is \"{}\")", getName(), text.getText(), text.getOutputType());
        main.addChild(text);
        current = text;
        iterator ++;
        return self();
    }

    /**
     * Adds a new {@link TextModule} as a child of this module.
     * To print a new line after this text, call {@link TextChain#newLine()}.
     *
     * @param text The text to add.
     * @param ansi The {@link Ansi} that this text may use. Ansi is reset automatically after {@code text} is displayed.
     * @return self.
     */
    public TextChain addText(String text, Ansi ansi) {
        this.addText(TextModule.builder(main.getName() + "-" + iterator, text)
                .setAnsi(ansi)
                .printNewLine(false));
        return self();
    }

    /**
     * Adds a new {@link TextModule} as a child of this module.
     * To print a new line after this text, call {@link TextChain#newLine()}.
     *
     * @param text The text to add.
     * @return self.
     */
    public TextChain addText(String text) {
        return addText(text, ansi());
    }

    /**
     * Displays the output of the module with name {@code moduleName}.
     * More precisely, it displays the application state that is accessed
     * at {@link com.calebleavell.jatui.modules.ApplicationModule#getInput(String)}.
     *
     * @param moduleName The input identifier for the application state (likely the name of another module).
     * @param ansi The {@link Ansi} that this text may use. Ansi is reset automatically after text is displayed.
     * @return self
     * @implNote Wraps {@link TextModule.OutputType#DISPLAY_APP_STATE}.
     */
    public TextChain addModuleOutput(String moduleName, Ansi ansi) {
        this.addText(TextModule.builder(main.getName() + "-" + iterator, moduleName)
                .setOutputType(TextModule.OutputType.DISPLAY_APP_STATE)
                .printNewLine(false)
                .setAnsi(ansi));
        return self();
    }

    /**
     * Displays the output of the module with name {@code moduleName}.
     * More precisely, it displays the application state that is accessed
     * at {@link com.calebleavell.jatui.modules.ApplicationModule#getInput(String)}.
     *
     * @param moduleName The input identifier for the application state (likely the name of another module).
     * @return self
     * @implNote Wraps {@link TextModule.OutputType#DISPLAY_APP_STATE}.
     */
    public TextChain addModuleOutput(String moduleName) {
        return this.addModuleOutput(moduleName, ansi());
    }

    /**
     * Prints an os-defined newline.
     * <br><br>
     * Example:
     * <pre><code>
     * LineBuilder text = LineBuilder.builder("name")
     *     .newLine()
     *     .addText("Hello, ")
     *     .addText("World!")
     *     .newLine();
     *
     * text.build().run();
     *
     * // output: "\nHello, World!\n"
     * </code></pre>
     * @return self
     * @implNote If text has been added, it sets the most recently added {@link TextModule}
     * to print a newline. If not, it adds a new empty {@link TextModule} and sets
     * it to print a newline. {@link TextModule} uses {@link PrintStream#println()}.
     *
     */
    public TextChain newLine() {
        if (current == null) {
            this.addText("");
        }
        logger.trace("adding newline to LineBuilder \"{}\"", getName());
        current.printNewLine(true);
        return self();
    }

    /**
     * {@code current} is a reference of the most recently added text is saved
     * to call {@link TextModule.Builder#printNewLine(boolean)} if
     * {@link TextChain#newLine()} is called.
     * @return current;
     */
    protected TextModule.Builder getCurrent() {
        return current;
    }

    /**
     * Checks equality for properties given by the builder. For {@link TextChain}, this includes
     * {@code current} and {@code iterator},
     * as well as other requirements provided by {@link TUIModule.Builder#shallowStructuralEquals(TUIModule.Builder, TUIModule.Builder)}.
     */
    @Override
    public boolean shallowStructuralEquals(TextChain first, TextChain second) {
        if(first == second) return true;
        if(first == null || second == null) return false;

        if(!TUIModule.Builder.structuralEquals(first.current, second.current)) return false;

        return Objects.equals(first.iterator, second.iterator) &&
                super.shallowStructuralEquals(first, second);
    }
}