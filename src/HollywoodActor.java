/**
 * HollywoodActor is an interface for implementation to
 * answer questions pertaining to the Hollywood Number
 * of an actor or to compute distance between actors.
 */
public interface HollywoodActor {
    String name();
    Iterable<String> movies();
    double distanceAverage();
    double distanceMaximum();
    String actorMaximum();
    Iterable<String> actorPath(String name);
    double actorPathLength(String name);
    Iterable<String> moviePath(String name);
}
