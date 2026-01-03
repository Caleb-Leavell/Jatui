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
 *        NumberedList list = new NumberedList("list")
 *                 .setStart(4)
 *                 .setStep(2)
 *                 .addListText("item 1", "item 2", "item 3");
 *
 *         list.build().run();
 * </code></pre>
 * <br>
 * Outputs:
 * <pre>
 * <b>[4]</b> item 1
 * <b>[6]</b> item 2
 * <b>[8]</b> item 3
 * </pre>
 */
public class NumberedList extends ModuleTemplate<NumberedList> {

    /** The value of the identifier of the first list item (e.g., "[5] item"). **/
    private int start = 1;

    /** How much to increment {@link NumberedList#i} after every list item **/
    private int step = 1;

    /** The incrementer that defines the number for every list item. (e.g., "[5] item") **/
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

    /**
     * Copies {@code start}, {@code step}, and {@code i},
     * and delegates to {@link TUIModule.Builder#shallowCopy(TUIModule.Builder)}.
     * @param original The builder to copy from.
     */
    @Override
    public void shallowCopy(NumberedList original) {
        this.start = original.start;
        this.step = original.step;
        this.i = original.i;
        super.shallowCopy(original);
    }

    /**
     * Adds a single list item. Adheres to the start and step set by
     * {@link NumberedList#setStart(int)} and {@link NumberedList#setStep(int)}.
     *
     * @param listText The item to display (e.g., "[5] {@code listText}").
     * @return self
     */
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

    /**
     * Adds zero or more list items. Adheres to the start and step set by
     * {@link NumberedList#setStart(int)} and {@link NumberedList#setStep(int)}.
     * <br>
     * Example:
     * <pre><code>
     *        NumberedList list = new NumberedList("list")
     *                 .addListText("item 1", "item 2", "item 3");
     *
     *         list.build().run();
     * </code></pre>
     * <br>
     * Outputs:
     * <pre>
     * <b>[1]</b> item 1
     * <b>[2]</b> item 2
     * <b>[3]</b> item 3
     * </pre>
     *
     * @param listText The items to display.
     * @return self
     */
    public NumberedList addListText(String... listText) {
        for(String text : listText) this.addListText(text);
        return self();
    }

    /**
     * {@code start} is the value of the identifier of the first list item (e.g., "[5] item").
     * @param start The starting number for the list.
     * @return self
     **/
    public NumberedList setStart(int start) {
        logger.trace("adding start of {} to {}", getName(), start);
        this.start = start;
        return this;
    }

    /**
     * {@code step} determines how much to increment the list number after every list item.
     * @param step The amount to increment every list number.
     * @return self
     **/
    public NumberedList setStep(int step) {
        logger.trace("adding step of {} to {}", getName(), step);
        this.step = step;
        return this;
    }

    /**
     * Checks equality for properties given by the builder. For {@link NumberedList}, this includes
     * {@code start}, {@code step}, and {@code i},
     * as well as other requirements provided by {@link TUIModule.Builder#shallowStructuralEquals(TUIModule.Builder, TUIModule.Builder)}.
     */
    public boolean shallowStructuralEquals(NumberedList first, NumberedList second) {
        if(first == second) return true;
        if(first == null || second == null) return false;


        return  Objects.equals(first.start, second.start) &&
                Objects.equals(first.step, second.step) &&
                first.i == second.i &&
                super.shallowStructuralEquals(first, second);
    }
}
