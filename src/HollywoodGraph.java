import java.util.Stack;

/**
 * HollywoodGraph loads movie and actor data from a file, generates
 * a graph out of it, and then allows questions to be asked about
 * the data, such as:
 * - How many connected components exist?
 * - How many actors are connected to a particular actor?
 * - What is the average distance between all actors connected
 *   to a particular actor?
 * - What is the length of the shortest path between two actors?
 */
public class HollywoodGraph {

    private class Actor implements HollywoodActor {
        String name;
        int actorVertex;
        double maxDist = 0.0;
        String furthestActor;
        double totalDist;
        int numConnectedActors = 1;
        Stack<String> movies;
        Stack<String> actors;
        Stack<String> moviePath;

        //bfs data structures
        boolean[] marked;
        int[] edgeTo;
        int[] distTo;

        public Actor(Graph graph, String newName){
            name = newName;
            actorVertex = actorIndex.get(name);
            marked = new boolean[graph.V()];
            edgeTo = new int[graph.V()];
            distTo = new int[graph.V()];

            // perform bfs from given actor
            Queue<Integer> q = new Queue<>();
            q.enqueue(actorVertex);
            marked[actorVertex] = true;
            distTo[actorVertex] = 0;

            while(!q.isEmpty()) {
                int v = q.dequeue();
                for(int w : graph.adj(v)) {
                    if(!marked[w]) {
                        // found new vertex w via edge v-w
                        q.enqueue(w);
                        marked[w] = true;
                        edgeTo[w] = v;
                        distTo[w] = distTo[v] + 1;
                        if (distTo[w] > maxDist){
                            maxDist = distTo[w];
                            furthestActor = indexActor.get(w);
                        }
                        if (indexActor.contains(w)){
                            numConnectedActors++;
                            totalDist = totalDist + distTo[w];
                        }
                    }
                }
            }

        }

        public String name(){
            return name;
        }

        public Iterable<String> movies(){
            movies = new Stack<>();
            int v = actorIndex.get(name);
            for (int movie: g.adj(v)){
                movies.push(indexMovie.get(movie));
            }
            return movies;
        }
        public double distanceAverage(){
            return (totalDist / 2.0) / numConnectedActors;
        }
        public double distanceMaximum(){
            return maxDist / 2;
        }
        public String actorMaximum(){
            return furthestActor;
        }
        public Iterable<String> actorPath(String name){
            if (!bfs.connected(actorIndex.get(name()), actorIndex.get(name))) return null; // if the two actors aren't even connected

            actors = new Stack<>();

            int root = actorIndex.get(name());
            int z = actorIndex.get(name);
            actors.push(indexActor.get(z));
            while (z != root){
                z = edgeTo[z];
                z = edgeTo[z];
                actors.push(indexActor.get(z));
            }

            return actors;
        }

        public double actorPathLength(String name){
            if (bfs.id(actorIndex.get(name)) != bfs.id(actorIndex.get(this.name))){
                return Double.POSITIVE_INFINITY;
            } else {
                return actors.size();
            }
        }

        private int getNumConnectedActors(){
            return numConnectedActors;
        }

        public Iterable<String> moviePath(String name){
            if (!bfs.connected(actorIndex.get(name()), actorIndex.get(name))) return null; // if the two actors aren't even connected

            moviePath = new Stack<>();

            int root = actorIndex.get(name());
            int z = actorIndex.get(name);
            moviePath.push(indexActor.get(z));
            while (z != root){
                z = edgeTo[z];
                if (indexActor.get(z) != null){
                    moviePath.push(indexActor.get(z));
                } else {
                    moviePath.push(indexMovie.get(z));
                }
            }

            return moviePath;
        }
    }


    // member variables
    int movieCount = 0;
    int actorCount = 0;
    RedBlackBST<String, Integer> movieIndex = new RedBlackBST<>();
    RedBlackBST<Integer, String> indexMovie = new RedBlackBST<>(); // used for EC portion
    RedBlackBST<String, Integer> actorIndex = new RedBlackBST<>();
    RedBlackBST<Integer, String> indexActor = new RedBlackBST<>(); // used for EC portion
    Graph g;
    CCBFS bfs;
    String[] representativeActors;


    public HollywoodGraph(){
        String[] lines = (new In("movies.txt")).readAllLines();

        actorCount = lines.length;
        for(String line : lines) {
            String[] parts = line.split("/");
            String movie = null;
            for(String part : parts) {
                if(movie == null) {
                    movie = part;
                    // put movie in movieIndex and indexMovie
                    movieIndex.put(movie, movieCount);
                    indexMovie.put(movieCount, movie);
                    movieCount++;
                } else {
                    String actor = part;
                    // put actor in actorIndex and indexActor if not already in there
                    if (actorIndex.get(actor) == null){
                        actorIndex.put(actor, actorCount);
                        indexActor.put(actorCount, actor);
                        actorCount++;
                    }
                }
            }
        }

        g = new Graph(actorCount);

        // add edges between movies and actors
        movieCount = 0;
        actorCount = lines.length;
        for(String line : lines) {
            String[] parts = line.split("/");
            String movie = null;
            for(String part : parts) {
                if(movie == null) {
                    movie = part;
                } else {
                    String actor = part;
                    g.addEdge(movieIndex.get(movie), actorIndex.get(actor));
                }
            }
        }
        bfs = new CCBFS(g);

        // create representative actor array
        movieCount = 0;
        actorCount = lines.length;
        int index;
        representativeActors = new String[connectedComponentsCount()];
        for(String line : lines) {
            String[] parts = line.split("/");
            String movie = null;
            for(String part : parts) {
                if(movie == null) {
                    movie = part;
                } else {
                    String actor = part;
                    index = bfs.id(actorIndex.get(actor));
                    if (representativeActors[index] == null){
                        representativeActors[index] = actor;
                    }
                }
            }
        }
    }

    public HollywoodActor getActorDetails(String name) {
        Actor actor = new Actor(g, name);
        return actor;
    }

    public Iterable<String> connectedComponents() {
        Stack<String> actorStack = new Stack<>();
        for (String actor: representativeActors){
            if (actor != null){
                actorStack.push(actor);
            }
        }

        return actorStack;
    }

    public int connectedComponentsCount() {
        return bfs.count();
    }

    public int connectedActorsCount(String name) {
        Actor actor = new Actor(g, name);
        return actor.getNumConnectedActors();
    }

    public double hollywoodNumber(String name) {
        Actor actor = new Actor(g, name);
        return actor.distanceAverage();
    }

    private Iterable<String> getActors(){
        Stack<String> actorStack = new Stack<>();
        for (String actor: actorIndex.keys()){
            actorStack.push(actor);
        }

        return actorStack;
    }

    static public void main(String[] args) {
        /* put code here to answer readme questions */
//        HollywoodGraph test = new HollywoodGraph();
//        test.connectedComponents();
//        test.getActorDetails("Fisher, Carrie");
//        test.connectedActorsCount("Bacon, Kevin");

        HollywoodGraph speedRun = new HollywoodGraph();
        Iterable<String> actors = speedRun.getActors();
        int numCalls = 0;
        Stopwatch stopwatch = new Stopwatch();
                for (String actor: actors){
                    if (stopwatch.elapsedTime() < 60){
                        speedRun.getActorDetails(actor);
                        numCalls++;
                    }
                }
        StdOut.print("Number of calls in one minute: " + numCalls + "\n");
    }
}
