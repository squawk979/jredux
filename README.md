# jredux

A very simple Java (8+) take on Redux.

Thread safe use of this library requires immutable objects to be used for state and also requires thread safe reducers.

The Java Immutables library https://immutables.github.io/ is a recommended starting point.

Utils.combineReducers is used to combine multiple reducers into a single reducer that can be passed to constructor.

If immutable state objects were not used we would have to deep copy (clone) state objects - something we do not wish to do.

We did consider using https://github.com/MutabilityDetector/MutabilityDetector to runtime check the immutability
of the state object, but decided against the overhead.  We do however recommend annotating immutable classes and
using the associated findbugs plugin.  See https://stackoverflow.com/questions/37087809/how-to-find-out-if-a-class-is-immutable

Consider this library experimental.  The interfaces have been ripped from https://github.com/brianegan/bansa.  The
implementation has been simplified and thread safety improved.
