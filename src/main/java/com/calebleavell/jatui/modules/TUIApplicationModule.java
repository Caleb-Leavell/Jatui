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

import java.util.*;

/**
 * The root TUIModule of an application. This class handles:
 * <ul>
 *     <li>Arbitrary Input storage</li>
 *     <li>Entering/Exiting the TUI (via Home and onExit)</li>
 *     <li>Name collision enforcement (logging)</li>
 * </ul>
 */
public class TUIApplicationModule extends TUIModule {

    /** The module that runs by default when the application finishes running **/
    public static final TUIModule.Builder<?> DEFAULT_EXIT = new TUITextModule.Builder("exit", "Exiting...")
            .setAnsi(ansi().fgRgb(125, 100, 100));

    /**
     * Where input collected during the application's lifecycle is stored. <br>
     * It maps a String to an Object, where the String is generally the
     * name of the module that supplied the input (it may also just be the
     * provided name of the input if {@link TUIApplicationModule#forceUpdateInput(String, Object)}
     * is used), and the Object is the input itself.
     */
    private final Map<String, Object> inputMap; // maps module names to the input object

    /**
     * The module that runs when the application finishes running. <br>
     * Displays "Exiting..." in gray by default.
     */
    private TUIModule.Builder<?> onExit;

    /**
     * The frequency of names of all children of this application. <br>
     * This is used to support {@link TUIApplicationModule#checkForNameDuplicates()}.
     * An error is logged if there are name collisions.
     */
    private final Map<String, Integer> nameFrequencyMap = new HashMap<>();

    /**
     * Overrides {@link TUIModule#run}. <br>
     * Checks and logs name duplicates, runs children (where "home" is the first child),
     * and then runs {@link TUIApplicationModule#onExit} if not disabled.
     */
    @Override
    public void run() {
        boolean doExit = !this.restart; // copy restart into local variable because super.run will set it to false
        checkForNameDuplicates();

        logger.info("Running TUIApplicationModule \"{}\"", getName());
        super.run();

        if(doExit) onExit.build().run();
    }

    /**
     * Checks every child attached to this application and logs an error
     * for every name collision found.
     */
    private void checkForNameDuplicates() {
        nameFrequencyMap.clear();

        for(TUIModule.Builder<?> child : getChildren()) {
            child.forEach(c -> {
                nameFrequencyMap.computeIfAbsent(c.getName(), _ -> 0);
                nameFrequencyMap.put(c.getName(), nameFrequencyMap.get(c.getName()) + 1);
            });
        }
        for(Map.Entry<String, Integer> entry : nameFrequencyMap.entrySet()) {
            if(entry.getValue() >= 2)
                logger.error("Duplicate name detected: \"{}\" appears in {} modules", entry.getKey(), entry.getValue());
        }
    }

    /**
     * Clears the hashmap of inputs for reuse.
     * Fills all char arrays with spaces for security.
     */
    public void resetMemory() {
        // zero out all char arrays as they are most likely to be sensitive information (like a Password)
        for(Map.Entry<String, Object> obj : inputMap.entrySet()) {
            Object val = obj.getValue();
            if(val instanceof char[]) {
                Arrays.fill((char[]) val, ' ');
            }
        }

        inputMap.clear();
    }

    /**
     * Return the input corresponding to a given name.
     * @param inputName The name that corresponds to the requested input. In general, this is the
     *  name of the module that supplied the input (it may also just be the
     *  provided name of the input if {@link TUIApplicationModule#forceUpdateInput(String, Object)}
     *  is used.
     * @return The input corresponding to {@code inputName}, or null if it doesn't exist.
     */
    public Object getInput(String inputName) {
        return inputMap.get(inputName);
    }

    /**
     * Returns the input for the given module only if 1. it exists 2. it is of the correct type.
     * @param inputName The name that corresponds to the requested input. In general, this is the
     *      *  name of the module that supplied the input (it may also just be the
     *      *  provided name of the input if {@link TUIApplicationModule#forceUpdateInput(String, Object)}
     *      *  is used.
     * @param type The class type of the input
     * @return The input, which will be the same type as the "type" parameter, or null if it
     *  either doesn't exist in general or doesn't exist of the provided type.
     * @param <T> The type to safely cast to.
     *
     */
    public <T> T getInput(String inputName, Class<T> type) {
        if(inputMap.get(inputName) == null) return null;
        if(inputMap.get(inputName).getClass().equals(type)) return type.cast(inputMap.get(inputName));
        else return null;
    }

    /**
     * Returns the input for the given module only if 1. it exists 2. it is of the correct type.
     * @param inputName The name that corresponds to the requested input. In general, this is the
     *      *  name of the module that supplied the input (it may also just be the
     *      *  provided name of the input if {@link TUIApplicationModule#forceUpdateInput(String, Object)}
     *      *  is used.
     * @param type The class type of the input
     * @param defaultValue The value to return if the requested value doesn't exist.
     * @return The input, which will be the same type as the "type" parameter, or {@code defaultValue} if it
     *  either doesn't exist in general or doesn't exist of the provided type.
     * @param <T> The type to safely cast to.
     */
    public <T> T getInputOrDefault(String inputName, Class<T> type, T defaultValue) {
        if(inputMap.get(inputName) == null) return defaultValue;
        if(inputMap.get(inputName).getClass().equals(type)) return type.cast(inputMap.get(inputName));
        else return defaultValue;
    }

    /**
     *
     * @param module
     * @param input
     */
    public void updateInput(TUIModule module, Object input) {
        logInput(module.getName(), input);
        inputMap.put(module.getName(), input);
    }

    public void updateInput(String moduleName, Object input) {
        logInput(moduleName, input);
        TUIModule.Builder<?> child = getChild(moduleName);
        if(child != null) updateInput(child.build(), input);
        else logger.debug("no child found of name \"{}\", so no input was updated", moduleName);
    }

    public void forceUpdateInput(String identifier, Object input) {
        inputMap.put(identifier, input);
    }

    private void logInput(String moduleName, Object input) {
        logger.info("updating input in app \"{}\" for module \"{}\" to \"{}\"", getName(), moduleName, (input == null) ? "null" : input.toString());
    }

    public void setHome(TUIModule.Builder<?> home) {
        logger.info("setting home of application \"{}\" to module \"{}\"", getName(), home.getName());
        this.getChildren().set(0, home);

        for(TUIModule.Builder<?> child : getChildren()) {
            child.setApplication(this);
            child.setPrintStream(this.getPrintStream());
            child.setScanner(this.getScanner());
            child.enableAnsi(this.getAnsiEnabled());
        }
    }

    public TUIModule.Builder<?> getHome() {
        if(this.getChildren().isEmpty()) return null;
        return this.getChildren().getFirst();
    }

    public void setOnExit(TUIModule.Builder<?> onExit) {
        logger.debug("setting onExit for application \"{}\" to module \"{}\"", getName(), onExit.getName());
        this.onExit = onExit;
        onExit.setApplication(this);
        onExit.setPrintStream(this.getPrintStream());
        onExit.setScanner(this.getScanner());
        onExit.enableAnsi(this.getAnsiEnabled());
    }

    public TUIModule.Builder<?> getOnExit() {
        return onExit;
    }

    /**
     * <p>Checks equality for properties given by the builder.</p>
     *
     * <p>For TUIApplicationModule, this includes: </p>
     * <ul>
     *     <li><strong>onExit</strong> (Note: checks structural equality, not reference equality)</li>
     *     <li>name</li>
     *     <li>application</li>
     *     <li>ansi</li>
     *     <li>scanner</li>
     *     <li>printStream</li>
     *     <li>enableAnsi</li>
     * </ul>
     * <p>Note: Runtime properties (e.g., inputMap, currentRunningChild, terminated), are not considered.</p>
     * @param other The TUIApplicationModule to compare
     * @return true if this module equals {@code other} according to builder-provided properties
     * @implNote This method intentionally does not override {@link Object#equals(Object)} so that things like HashMaps still check by method reference.
     *  This method is merely for checking structural equality, which is generally only necessary for manual testing.
     *  Also, There is no need for an equalTo method that overrides {@link TUIModule.Builder#equalTo(TUIModule.Builder, TUIModule.Builder)} in {@link TUIApplicationModule.Builder} due to the fact that onExit is a
     * child within the Builder, but not in the built module. This ensures property propagation is applied to onExit before building, but
     * after building it is run last.
     */
    public boolean equals(TUIApplicationModule other) {
        if(this == other) return true;
        if(other == null) return false;

        return TUIModule.Builder.equals(onExit, other.onExit) && super.equals(other);
    }

    public TUIApplicationModule(Builder builder) {
        super(builder);
        this.inputMap = builder.inputMap;
        this.onExit = builder.onExit;

        for(TUIModule.Builder<?> child : getChildren()) {
            if(this.getApplication() == null) child.setApplication(this);
            else child.setApplication(this.getApplication());

            child.setPrintStream(this.getPrintStream());
            child.setScanner(this.getScanner());
            child.enableAnsi(this.getAnsiEnabled());
        }

        this.getChildren().remove(onExit);
    }

    public static class Builder extends TUIModule.Builder<Builder> {
        private final Map<String, Object> inputMap = new HashMap<>();
        private TUIModule.Builder<?> onExit = DEFAULT_EXIT.getCopy();

        public Builder(String name) {
            super(Builder.class, name);
            this.children.add(TUIModuleFactory.empty("home"));
            this.children.add(onExit);
        }

        protected Builder() {
            super(Builder.class);
        }

        /**
         * Gets a fresh instance of this type of Builder.
         *  Note, this is intended only for copying utility and may have unknown consequences if used in other ways.
         * @return A fresh, empty instance.
         */
        @Override
        protected Builder createInstance() {
            return new TUIApplicationModule.Builder();
        }

        public TUIModule.Builder<?> getHome() {
            return this.children.getFirst();
        }

        public TUIModule.Builder<?> getOnExit() {
            return this.onExit;
        }

        public Builder setHome(TUIModule.Builder<?> home) {
            logger.debug("setting home for application builder \"{}\" to module \"{}\"", getName(), (home == null) ? "null" : home.getName());
            this.children.set(0, home);
            return self();
        }

        public Builder setOnExit(TUIModule.Builder<?> onExit) {
            logger.debug("setting onExit for application builder \"{}\" to \"{}\"", getName(), (onExit == null) ? "null" : onExit.getName());
            this.children.remove(this.onExit);
            this.onExit = onExit;
            this.children.add(onExit);
            return self();
        }

        @Override
        public TUIApplicationModule build() {
            logger.trace("Building TUIApplicationModule \"{}\"", getName());
            return new TUIApplicationModule(self());
        }
    }
}
