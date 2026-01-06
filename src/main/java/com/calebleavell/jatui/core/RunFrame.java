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

package com.calebleavell.jatui.core;

import com.calebleavell.jatui.modules.TUIModule;

/**
 *  Holds information required for a single scheduling unit of the application.
 *  In principle, the application runs module-by-module, so the primary data stored is
 *  {@link RunFrame#module}. Everything else (e.g., {@link RunFrame#parent}) is auxiliary
 *  information to help manage the run stack (created in {@link TUIModule#start()}).
 */
public class RunFrame {
    /**
     * The {@link TUIModule} that will be running.
     */
    public final TUIModule module;

    /**
     * The {@link TUIModule} that is the parent of the module that will be running.
     */
    public final TUIModule parent;

    /**
     * The {@link RunFrame.State} state of the module; could be {@link RunFrame.State#BEGIN} or {@link RunFrame.State#END}.
     */
    public final State state;

    /**
     * This field signals that the running module is replacing the {@code currentRunningChild} of a {@link TUIModule}
     * (provided by {@link TUIModule#getCurrentRunningChild()}) in order to be able to set it back.
     * This replacement happens when {@link TUIModule#navigateTo(TUIModule.Builder)} is called.
     */
    public final TUIModule displacedChild;

    /**
     * Signals to the scheduler how to manage this frame. See {@link TUIModule#start()}
     * for implementation details on frame management.
     */
    public enum State {
        BEGIN,
        END
    }

    /**
     * Constructs a new {@link RunFrame} to store information for a scheduling unit.
     *
     * @param module The {@link TUIModule} that will be running.
     * @param parent The {@link TUIModule} that is the parent of the module that will be running.
     * @param state The {@link RunFrame.State} state of the module; could be {@link RunFrame.State#BEGIN} or {@link RunFrame.State#END}.
     */
    public RunFrame(TUIModule module, TUIModule parent, State state) {
        this(module, parent, state, null);
    }

    /**
     * Constructs a new {@link RunFrame} to store information for a scheduling unit.
     *
     * @param module The {@link TUIModule} that will be running.
     * @param parent The {@link TUIModule} that is the parent of the module that will be running.
     * @param state The {@link RunFrame.State} state of the module; could be {@link RunFrame.State#BEGIN} or {@link RunFrame.State#END}.
     * @param displacedChild The {@code currentRunningChild} of {@link RunFrame#parent} if {@code module} is temporarily replacing it.
     */
    public RunFrame(TUIModule module, TUIModule parent, State state, TUIModule displacedChild) {
        this.module = module;
        this.parent = parent;
        this.state = state;
        this.displacedChild = displacedChild;
    }

    /**
     * Format:
     * "(toRun: {@code <module>} | parent: {@code <parent>} |
     * state: {@code <state>} | displacedChild: {@code <displacedChild>})".
     *
     * @return the formatted string
     */
    @Override
    public String toString() {
        return String.format(
                "(toRun: %s | parent: %s | state: %s | displacedChild: %s)",
                module, parent, state, displacedChild
        );
    }

}
