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

package com.calebleavell.jatui.modules;

/**
 * Provides abstractions for common TUI patterns.
 */
public class ModuleFactory {
    /**
     * Returns an empty {@link ContainerModule} - simply wraps <pre><code>ContainerModule.builder([name])</code></pre>
     * to allow for code that consistently uses ModuleFactory if desired.
     * @param name The name of the module
     * @return The empty {@link ContainerModule}
     */
    public static ContainerModule.Builder empty(String name) {
        return ContainerModule.builder(name);
    }

    /**
     * Returns a {@link FunctionModule} that calls another module's terminate method.
     *
     * @param name              The name of <i>this</i> module (the one that is returned).
     * @param moduleToTerminate The module to terminate (it will terminate when this module is run)
     * @return The {@link FunctionModule} that terminates the inputted module
     */
    public static FunctionModule.Builder terminate(String name, TUIModule moduleToTerminate) {
        return FunctionModule.builder(name, moduleToTerminate::terminate);
    }

    /**
     * Returns a {@link FunctionModule} that terminates the child of name {@code moduleToTerminate} that is a child of {@code parent}. <br>
     * Note: {@code moduleToTerminate} doesn't have to be a direct child of {@code parent}. It can be multiple layers deep.
     *
     * @param name The name of <i>this</i> module (the one that is returned).
     * @param moduleToTerminate The name of the module to terminate
     * @param parent The parent module that will terminate the module
     * @return The {@link FunctionModule} that terminates the module corresponding to {@code moduleToTerminate}
     */
    public static FunctionModule.Builder terminate(String name, String moduleToTerminate, TUIModule parent) {
        return FunctionModule.builder(name, () -> parent.terminateChild(moduleToTerminate));
    }

    /**
     * Returns a Function Module that finds the child of a parent by name then runs it.
     * Will do nothing if the parent's module tree does not have a child with the given name.
     * Nothing happens if {@code parentModule} isn't running when the returned {@link FunctionModule}
     * runs.
     * <br>
     * Note: {@code parentModule} is usually
     * the {@link ApplicationModule} for the program.
     *
     * @param name         The name of <i>this</i> module (the one that is returned).
     * @param parent The module that the <strong>module to run</strong> is the child of (directly or indirectly)
     * @param moduleToRun  The name of the module to run (it will run when this module is run)
     * @return The {@link FunctionModule} that calls another module's run method
     * @implNote Runs {@code moduleToRun} via {@link TUIModule#navigateTo(TUIModule.Builder)} to
     * ensure there is only one scheduler.
     */
    public static FunctionModule.Builder run(String name, TUIModule parent, String moduleToRun) {
        return FunctionModule.builder(name, () -> {
            if (parent == null || moduleToRun == null) return;
            TUIModule.Builder<?> toRun = parent.getChild(moduleToRun);
            if(toRun == null) return;
            parent.navigateTo(toRun);
        });
    }

    /**
     * Restarts {@code moduleToRestart} when the returned {@link FunctionModule} is run.
     * Nothing happens if {@code moduleToRestart} isn't running.
     *
     * @param name The name of <i>this</i> module (the one that is returned).
     * @param moduleToRestart The module that will be restarted.
     * @return The {@link FunctionModule} that will restart {@code moduleToRestart} when run.
     */
    public static FunctionModule.Builder restart(String name, TUIModule moduleToRestart) {
        return FunctionModule.builder(name, () -> {
            if(moduleToRestart != null) moduleToRestart.restart();
        });
    }

    /**
     * Restarts {@code moduleToRestart} when the returned {@link FunctionModule} is run.
     * Nothing happens if {@code moduleToRestart} isn't running.
     *
     * @param name The name of <i>this</i> module (the one that is returned).
     * @param parent The module that {@code moduleToRestart} is the child of (directly or indirectly)
     * @param moduleToRestart The module that will be restarted.
     * @return The {@link FunctionModule} that will restart {@code moduleToRestart} when run.
     * @implNote Calls {@link TUIModule#restartChild(String)} and thus is dependent
     * on the behavior of the implementation of that method.
     */
    public static FunctionModule.Builder restart(String name, TUIModule parent, String moduleToRestart) {
        return FunctionModule.builder(name, () -> {
            if(parent == null) return;
            parent.restartChild(moduleToRestart);
        });
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
        return FunctionModule.builder(name, () -> {
            Integer counter = app.getInput(name, Integer.class);
            if(counter != null)  return counter + step;
            else return begin;
        });
    }
}
