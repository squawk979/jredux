package net.jmeze.jredux;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class BaseStore<S> implements Store<S> {

    // all state objects (ie initialState and any state returned by the reducer) must be immutable
    // with immutable state objects we only need to synchronize access to the reference to the state (AtomicReference)

    // the reducers must be thread safe

    // access/updates to subscribers must be thread safe, so use CopyOnWriteArrayList
    // listeners are often cited as a good use for CopyOnWriteArrayList.  Note any modifications of CopyOnWriteArrayList
    // are protected by a ReentrantLock in the implementation, we do not need to do

    private final AtomicReference<S> currentState = new AtomicReference<>();
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
    public void dispatch(Action action) {
        // TODO: dispatch could be called by multiple threads and, for example, the current state could change
        // during the subscriber iteration.  this may be undesirable if 2 or more subscribers require a consistent
        // view of the state.  we could possibly synchronize this method, but we then run into a potential deadlock
        // issue if any dispatch results in a dispatch call.  perhaps we could store the new state in threadlocal
        // and issue to each subscriber?
        currentState.set(reducer.reduce(currentState.get(), action));
        for (Subscriber subscriber : subscribers) {
            subscriber.onStateChange(currentState.get());
        }
    }

    @Override
    public Subscription subscribe(final Subscriber<S> subscriber) {
        subscribers.add(subscriber);
        return () -> subscribers.remove(subscriber);
    }

}
