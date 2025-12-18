package com.calebleavell.jatui.event;

import com.calebleavell.jatui.tui.TUIModule;

import java.util.ArrayList;
import java.util.List;


public class Event<T> {
    private T data;
    private final List<TUIModule> subscribers = new ArrayList<>();

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void subscribe(TUIModule module) {
        this.subscribers.add(module);
    }

//    public void call(T data) {
//        for(TUIModule subscriber: subscribers) {
//            subscriber.run(this);
//        }
//    }

    public static class InputCollected extends Event<String> {}
    public static class ModuleIsRun extends Event<TUIModule> {}
}
