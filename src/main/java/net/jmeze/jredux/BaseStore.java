package net.jmeze.jredux;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class BaseStore<S> implements Store<S> {

    // all state objects (ie initialState and any state returned by the reducer) must be immutable
    // with immutable state objects we only need to synchronize access to the reference to the state (AtomicReference)

    // the reducers must be thread safe

    // we must use make access/updates to subscribers thread safe, we use CopyOnWriteArrayList for this.
    // listeners are often cited as a good use for CopyOnWriteArrayList.  Note any modifications of CopyOnWriteArrayList
    // are protected by a ReentrantLock in the implementation, we do not need to do

    private AtomicReference<S> currentState = new AtomicReference<>();
    private final Reducer<S> reducer;
    private final List<Subscriber<S>> subscribers = new CopyOnWriteArrayList<>();

    public BaseStore(S initialState, Reducer<S> reducer) {
        currentState.set(initialState);
        this.reducer = reducer;
    }

    @Override
    public S getState() {
        return currentState.get();
    }

    @Override
    public S dispatch(Action action) {
        currentState.set(reducer.reduce(currentState.get(), action));
        for (Subscriber subscriber : subscribers) {
            subscriber.onStateChange(currentState.get());
        }
        return currentState.get();
    }

    @Override
    public Subscription subscribe(final Subscriber<S> subscriber) {
        subscribers.add(subscriber);
        return new Subscription() {
            @Override
            public void unsubscribe() {
                subscribers.remove(subscriber);
            }
        };
    }

}