# jredux

A very simple Java take on Redux.

Consider this library experimental.  Originally ripped from https://github.com/brianegan/bansa.

Thread safe use of this library requires immutable* state objects and also requires pure reducers.  The order of action
dispatch can only be guaranteed if called from the same thread.  Do not dispatch actions that update the
same part of the state structure from different threads as the order in which the actions are applied cannot be guaranteed.
If you do call dispatch from different threads ensure that these actions (and any listener actions they may trigger) do
not update the same part of the state.  You're almost certainly better off just having a one thread call dispatch.

Redux basics: http://redux.js.org/docs/basics/

The Java Immutables library https://immutables.github.io/ is recommended.

Utils.combineReducers is used to combine multiple reducers into a single reducer that can be passed to constructor.

We did consider using https://github.com/MutabilityDetector/MutabilityDetector to runtime check the immutability
of the state object, but decided against the overhead.  We do however recommend annotating immutable classes and
using the associated findbugs plugin.  See https://stackoverflow.com/questions/37087809/how-to-find-out-if-a-class-is-immutable

* by using immutable objects we avoid the need to deep copy (clone) state objects
