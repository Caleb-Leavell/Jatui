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

package com.calebleavell.jatui.modules;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * Provides abstractions for common TUI patterns.
 */
public class ModuleFactory {
    /**
     * Returns an empty ContainerModule - simply wraps <pre><code>new TUIContainerModule.Builder([name])</code></pre>
     * to allow for code that consistently uses TUIModuleFactory if desired.
     * @param name The name of the module
     * @return The empty ContainerModule
     */
    public static ContainerModule.Builder empty(String name) {

        return new ContainerModule.Builder(name);
    }

    /**
     * Returns a Function Module that calls another module's terminate method.
     *
     * @param name              The name of the module to be returned
     * @param moduleToTerminate The module to terminate (it will terminate when this module is run)
     * @return The Function Module that terminates the inputted module
     */
    public static FunctionModule.Builder terminate(String name, TUIModule moduleToTerminate) {
        return new FunctionModule.Builder(name, moduleToTerminate::terminate);
    }

    /**
     * Returns a Function Module that terminates the child of name {@code moduleToTerminate} that is a child of {@code parent}. <br>
     * Note: {@code moduleToTerminate} doesn't have to be a direct child of {@code parent}. It can be multiple layers deep.
     *
     * @param name The name of the module to be returned
     * @param moduleToTerminate The name of the module to terminate
     * @param parent The parent module that will terminate the module
     * @return The Function Module that terminates the module corresponding to {@code moduleToTerminate}
     */
    public static FunctionModule.Builder terminate(String name, String moduleToTerminate, TUIModule parent) {
        return new FunctionModule.Builder(name, () -> parent.terminateChild(moduleToTerminate));
    }

    /**
     * // TODO - update documentation
     * Returns a Function Module that finds the child of a parent by name then runs it.
     * Will do nothing if the parent's module tree does not have a child with the given name.
     *
     * @param name         The name of the module to be returned
     * @param parentModule The module that the <strong>module to run</strong> is the child of
     * @param moduleToRun  The name of the module to run (it will run when this module is run)
     * @return The Function Module that calls another module's run method
     */
    public static FunctionModule.Builder run(String name, TUIModule parentModule, String moduleToRun) {
        return new FunctionModule.Builder(name, () -> {
            if (parentModule == null || moduleToRun == null) return;
            TUIModule.Builder<?> toRun = parentModule.getChild(moduleToRun);
            parentModule.runModuleAsChild(toRun);
        });
    }

    // TODO - document
    public static FunctionModule.Builder restart(String name, TUIModule moduleToRestart) {
        return new FunctionModule.Builder(name, () -> {
            if(moduleToRestart != null) moduleToRestart.restart();
        });
    }

    public static FunctionModule.Builder restart(String name, TUIModule parent, String moduleToRestart) {
        return new FunctionModule.Builder(name, () -> parent.restartChild(moduleToRestart));
    }

    /**
     * Returns a Function Module that increments a counter every time it's run (starts at 1).
     * To access the counter, call <br>
     * {@code [app].getInput([name], Integer.class)}). <br>
     * Note that this will likely be null before this module is run.
     *
     * @param name The name of the module to be returned
     * @param app  The Application Module that this module will be the child of
     * @return The Function Module that increments a counter
     */
    public static FunctionModule.Builder counter(String name, ApplicationModule app) {
        return counter(name, app, 1, 1);
    }

    /**
     * Returns a Function Module that increments a counter every time it's run (starts at 1).
     * To access the counter, call <br>
     * {@code [app].getInput([name], Integer.class)}). <br>
     * Note that this will likely be null before this module is run.
     *
     * @param name  The name of the module to be returned
     * @param app   The Application Module that this module will be the child of
     * @param begin The number to begin at (e.g. begin = 5 -> 5, 6, 7, 8, ...)
     * @param step  The amount to increment each time (e.g. step = 5 -> 1, 6, 11, ...)
     * @return The Function Module that increments a counter
     */
    public static FunctionModule.Builder counter(String name, ApplicationModule app, int begin, int step) {
        return new FunctionModule.Builder(name, () -> {
            Integer counter = app.getInput(name, Integer.class);
            if(counter != null)  return counter + step;
            else return begin;
        });
    }
}
