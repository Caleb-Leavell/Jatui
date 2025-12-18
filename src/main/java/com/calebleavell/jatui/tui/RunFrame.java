package com.calebleavell.jatui.tui;

import java.util.Deque;

public class RunFrame {
    public final TUIModule module;
    public final TUIModule parent;
    public final State state;
    public final TUIModule displacedChild;

    public enum State {
        BEGIN,
        END
    }

    public RunFrame(TUIModule module, TUIModule parent, State state) {
        this(module, parent, state, null);
    }

    public RunFrame(TUIModule module, TUIModule parent, State state, TUIModule displacedChild) {
        this.module = module;
        this.parent = parent;
        this.state = state;
        this.displacedChild = displacedChild;
    }

    @Override
    public String toString() {
        return String.format("(toRun: \"%s\" | parent: \"%s\" | state: %s)", module, parent, state == State.BEGIN ? "begin" : "end");
    }
}
