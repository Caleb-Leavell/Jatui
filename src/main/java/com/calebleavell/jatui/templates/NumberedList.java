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
import com.calebleavell.jatui.modules.ModuleTemplate;
import com.calebleavell.jatui.modules.TUIModule;

import java.util.Objects;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * Handles displaying a list of text as a numbered list.
 * <br><br>
 * Example usage:
 * <pre><code>
 *
 * </code></pre>
 */
public class NumberedList extends ModuleTemplate<NumberedList> {
    private int start = 1;
    private int step = 1;
    private int i = 0;

    public NumberedList(String name) {
        super(NumberedList.class, name);
    }

    protected NumberedList() {
        super(NumberedList.class);
    }

    /**
     * Gets a fresh instance of this type of Builder.
     *  Note, this is intended only for copying utility and may have unknown consequences if used in other ways.
     * @return A fresh, empty instance.
     */
    @Override
    protected NumberedList createInstance() {
        return new NumberedList();
    }

    @Override
    public void shallowCopy(NumberedList original) {
        this.start = original.start;
        this.step = original.step;
        this.i = original.i;
        super.shallowCopy(original);
    }

    public NumberedList addListText(String listText) {
        logger.trace("adding list text \"{}\" to {}", listText, getName());
        int currentNum = (i * step) + start;
        main.addChild(
                new LineBuilder(name + "-" + currentNum)
                        .addText("[" + currentNum + "] ", ansi().bold())
                        .addText(listText)
                        .newLine());
        i ++;
        return this;
    }

    public NumberedList addListText(String... listText) {
        for(String text : listText) this.addListText(text);
        return self();
    }

    public NumberedList setStart(int start) {
        logger.trace("adding start of {} to {}", getName(), start);
        this.start = start;
        return this;
    }

    public NumberedList setStep(int step) {
        logger.trace("adding step of {} to {}", getName(), step);
        this.step = step;
        return this;
    }

    /**
     * <p>Checks equality for properties given by the builder.</p>
     *
     * <p>For InputHandlers, this includes: </p>
     * <ul>
     *     <li><strong>start</strong>/li>
     *     <li><strong>step</strong>
     *     <li><strong>i</strong>
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
    public boolean shallowStructuralEquals(NumberedList first, NumberedList second) {
        if(first == second) return true;
        if(first == null || second == null) return false;


        return  Objects.equals(first.start, second.start) &&
                Objects.equals(first.step, second.step) &&
                Objects.equals(first.i, second.i) &&
                super.shallowStructuralEquals(first, second);
    }
}
