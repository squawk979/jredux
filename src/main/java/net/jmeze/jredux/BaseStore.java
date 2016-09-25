package net.jmeze.jredux;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class BaseStore<S> implements Store<S> {

    // all state objects (ie initialState and any state returned by the reducer) must be immutable
    // with immutable state objects we only need to synchronize access to the reference to the state

    // reducers must be pure functions - functions that return the exact same output for given inputs.
    // They should also be free of side-effects.

    // access/updates to subscribers must be thread safe, so use CopyOnWriteArrayList
    // listeners are often cited as a good use for CopyOnWriteArrayList.  Note any modifications of CopyOnWriteArrayList
    // are protected by a ReentrantLock in the implementation, we do not need to do

    private static final ThreadLocal<Boolean> isDispatching =
            new ThreadLocal<Boolean>() {
                @Override protected Boolean initialValue() {
                    return false;
                }
            };

    private final AtomicReference<S> currentState = new AtomicReference<S>();
    private final Reducer<S> reducer;
    private final List<Subscriber<S>> subscribers = new CopyOnWriteArrayList<Subscriber<S>>();

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

        // if actions arrive on multiple threads, these threads may block on the synchronized section.
        // the order in which they wake and eventually pass through the synchronized section cannot be determined
        // which may have intended consequences: say we have current state = 1 and an action "add 10" followed by
        // "multiply by 10" - if the action order is switched the resulting state is different :(

        // therefore dispatch can only guarantee the order of actions called from the same thread - you
        // almost certainly want to be calling from the same thread anyway otherwise you'll probably encounter race
        // conditions.

        // So when would multithreaded dispatch be useful? perhaps your state structure contains unrelated sections.
        // these could be safely updated by separate threads.

        if (isDispatching.get()) {
            throw new IllegalStateException("Reducers may not dispatch actions");
        }

        // dispatch could be called by multiple threads at the same time, the synchronized block ensures only a single
        // thread at a time updates the currentState
        synchronized (this) {
            isDispatching.set(true);
            currentState.set(reducer.reduce(currentState.get(), action));
            isDispatching.set(false);
        }

        // the notify subscribers loop is outside the synchronized block to allow for nested dispatch
        // note we pass the currentState which can potentially change (by other threads) as we go through the loop
        // in the earlier implementation I stored the state so the same state could be given to all subscribers,
        // but now realise that would create problems with nested dispatch calls: the deepest nested call would
        // report the latest state to subscribers, but as we popped up the stack and the earlier calls continued they
        // would report earlier state to the subscribers
        for (Subscriber subscriber : subscribers) {
            subscriber.onStateChange(currentState.get());
        }

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
