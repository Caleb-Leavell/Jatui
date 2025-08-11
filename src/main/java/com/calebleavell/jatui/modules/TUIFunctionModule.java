package com.calebleavell.jatui.modules;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class TUIFunctionModule extends TUIModule {
    private Supplier<Object> function;


    @Override
    public void run() {
        this.terminated = false;
        Object output = function.get();
        if(getApplication() != null) getApplication().updateInput(this, output);
        super.run();
    }

    public Supplier<Object> getFunction() {
        return function;
    }

    public void setFunction(Supplier<Object> function) {
        this.function = function;
    }

    /**
     * <p>Checks equality for properties given by the builder.</p>
     *
     * <p>For TUIFunctionModule, this includes: </p>
     * <ul>
     *     <li><strong>function</strong> <i>(Note: this checks reference equality, not function equality.)</i></li>
     *     <li>name</li>
     *     <li>application</li>
     *     <li>children</li>
     *     <li>ansi</li>
     *     <li>scanner</li>
     *     <li>printStream</li>
     *     <li>enableAnsi</li>
     * </ul>
     *
     * <p>For this method to return true, the same function reference must be used for both modules.</p>
     * <strong>Examples: </strong>
     * <pre><code>
     // this will be false, since you're technically passing in 2 different lambdas
     boolean one = new TUIFunctionModule.Builder("one", () -> System.out.println("Hello, World!")).build()
     .equals(new TUIFunctionModule.Builder("one", () -> System.out.println("Hello, World!")).build()); // returns false

     // this will also be false, since Java constructs a different method reference each time
     boolean two = new TUIFunctionModule.Builder("two", Experimentation::myMethod).build()
     .equals(new TUIFunctionModule.Builder("two", Experimentation::myMethod).build()); // returns false

     Runnable run = () -> System.out.println("Hello, World!");

     // this will be false since TUIFunctionModule.Builder converts Runnable to Supplier under the hood
     boolean three = new TUIFunctionModule.Builder("three", run).build()
     .equals(new TUIFunctionModule.Builder("three", run).build()); // returns false

     Supplier<Object> sup = () -> "Hello, World!";

     // this will be true since the same lambda is being referenced
     boolean four = new TUIFunctionModule.Builder("four", sup).build()
     .equals(new TUIFunctionModule.Builder("four", sup).build()); // returns true
     * </code></pre>
     * <p>Note: Runtime properties (e.g., inputMap, currentRunningChild, terminated), are not considered.</p>
     * @param other The non-null TUIFunctionModule to compare
     * @return true if this module equals {@code other} according to builder-provided properties
     * @implNote This method intentionally does not override {@link Object#equals(Object)} so that things like HashMaps still check by method reference.
     *  This method is merely for checking structural equality, which is generally only necessary for manual testing.
     */
    public boolean equals(TUIFunctionModule other) {
        if(this == other) return true;
        if(other == null) return false;


        return Objects.equals(function, other.function) && super.equals(other);
    }


    public TUIFunctionModule(Builder builder) {
        super(builder);
        this.function = builder.function;
    }

    public static class Builder extends TUIModule.Builder<Builder> {
        Supplier<Object> function;

        public Builder(String name, Supplier<Object> function) {
            super(Builder.class, name);
            this.function = function;
        }

        public Builder(String name, Runnable function) {
            super(Builder.class, name);
            this.function(function);
        }

        public Builder function(Supplier<Object> function) {
            this.function = function;
            return self();
        }

        public Builder function(Runnable function) {
            if(function == null) this.function = null;
            else {
                this.function = () -> {
                    function.run();
                    return null;
                };
            }

            return self();
        }

        protected Builder(Builder original) {
            super(original);

            this.function = original.function;
        }

        /**
         * <p>Checks equality for properties given by the builder.</p>
         *
         * <p>For TUIFunctionModule, this includes: </p>
         * <ul>
         *     <li><strong>function</strong> <i>(Note: this checks reference equality, not function equality.)</i></li>
         *     <li>name</li>
         *     <li>application</li>
         *     <li>children</li>
         *     <li>ansi</li>
         *     <li>scanner</li>
         *     <li>printStream</li>
         *     <li>enableAnsi</li>
         * </ul>
         *
         * <p>For this method to return true, the same function reference must be used for both modules.</p>
         * <strong>Examples: </strong>
         * <pre><code>
         // this will be false, since you're technically passing in 2 different lambdas
         boolean one = TUIFunctionModule.Builder.equalTo(
            new TUIFunctionModule.Builder("one", () -> System.out.println("Hello, World!")),
            new TUIFunctionModule.Builder("one", () -> System.out.println("Hello, World!"))); // returns false

         // this will also be false, since Java constructs a different method reference each time
         boolean two = TUIFunctionModule.Builder.equalTo(
            new TUIFunctionModule.Builder("two", Experimentation::myMethod),
            new TUIFunctionModule.Builder("two", Experimentation::myMethod)); // returns false

         Runnable run = () -> System.out.println("Hello, World!");

         // this will be false since TUIFunctionModule.Builder converts Runnable to Supplier under the hood
         boolean three = TUIFunctionModule.Builder.equalTo(
            new TUIFunctionModule.Builder("three", run),
            new TUIFunctionModule.Builder("three", run)); // returns false

         Supplier<Object> sup = () -> "Hello, World!";

         // this will be true since the same lambda is being referenced
         boolean four = TUIFunctionModule.Builder.equalTo(
            new TUIFunctionModule.Builder("four", sup),
            new TUIFunctionModule.Builder("four", sup)); // returns true
         * </code></pre>
         * <p>Note: Runtime properties (e.g., currentRunningChild, terminated), are not considered. Children are also not considered here,
         *  but are considered in {@link TUIModule.Builder#equals(TUIModule.Builder)}.
         * @param first The first TUIModule to compare
         * @param second The second TUIModule to compare
         * @return {@code true} if {@code first} and {@code second} are equal according to builder-provided properties
         * @implNote This is the {@code Function<TUIModule<?>, TUIModule.Builder<?>, Boolean>} that is passed into {@link DirectedGraphNode#equals(DirectedGraphNode, BiFunction)}
         */
        public static boolean equalTo(TUIFunctionModule.Builder first, TUIFunctionModule.Builder second) {
            if(first == second) return true;
            if(first == null || second == null) return false;

            return Objects.equals(first.function, second.function) && TUIModule.Builder.equalTo(first, second);
        }

        public boolean equals(TUIFunctionModule.Builder other) {
            return equals(other, TUIFunctionModule.Builder::equalTo);
        }

        @Override
        public Builder getCopy() {
            return new Builder(this);
        }

        @Override
        public TUIFunctionModule build() {
            return new TUIFunctionModule(self());
        }
    }

}
