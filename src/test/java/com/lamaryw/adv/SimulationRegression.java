package com.lamaryw.adv;

public class SimulationRegression {
    private static void rejects(Runnable action) {
        try { action.run(); } catch (IllegalArgumentException expected) { return; }
        throw new AssertionError("Invalid input was accepted");
    }

    private static void close(double actual, double expected, String label) {
        if (Double.isNaN(expected) ? !Double.isNaN(actual) :
                !Double.isFinite(actual) || Math.abs(actual - expected) > 1e-12) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }

    private static void exactMetrics(Graph graph, double path, double clustering, String label) {
        close(GraphProcessor.avgPathLen(graph), path, label + " APL");
        close(GraphProcessor.avgPathLenBig(graph), path, label + " big APL");
        close(GraphProcessor.clusterCoeff(graph), clustering, label + " CC");
        close(GraphProcessor.clusterCoeffBig(graph), clustering, label + " big CC");
        // Sampling every source (or requesting more than n) must become exact.
        for (int samples : new int[]{Math.max(1, graph.V()), graph.V() + 2}) {
            close(GraphProcessor.avgPathLen(graph, samples), path, label + " sampled APL");
            close(GraphProcessor.clusterCoeff(graph, samples), clustering, label + " sampled CC");
        }
    }

    private static void metricRegressions() {
        Graph cycle = GraphProcessor.createRegularGraph(4, 2);
        exactMetrics(cycle, 4.0 / 3, 0, "cycle4");
        // Every cycle source has the same distance mean: m=1 catches the old /0 denominator.
        close(GraphProcessor.avgPathLen(cycle, 1), 4.0 / 3, "cycle4 one source");
        close(GraphProcessor.avgPathLen(cycle, 2), 4.0 / 3, "cycle4 two sources");
        close(GraphProcessor.clusterCoeff(cycle, 1), 0, "cycle4 one local CC");

        Graph clique = GraphProcessor.createRegularGraph(5, 4);
        exactMetrics(clique, 1, 1, "clique5");
        close(GraphProcessor.avgPathLen(clique, 1), 1, "clique5 one source");
        close(GraphProcessor.clusterCoeff(clique, 1), 1, "clique5 one local CC");

        Graph triangleLeaf = new Graph();
        triangleLeaf.addEdge(10, 20);
        triangleLeaf.addEdge(20, 30);
        triangleLeaf.addEdge(30, 10);
        triangleLeaf.addEdge(10, 40);
        // Local values are 1/3, 1, 1, 0. Their mean differs from transitivity 3/5.
        exactMetrics(triangleLeaf, 4.0 / 3, 7.0 / 12, "triangle with leaf");

        Graph triangleIsolate = new Graph();
        triangleIsolate.addEdge(0, 1);
        triangleIsolate.addEdge(1, 2);
        triangleIsolate.addEdge(2, 0);
        triangleIsolate.addNode(3);
        exactMetrics(triangleIsolate, Double.NaN, .75, "triangle with isolate");
        close(GraphProcessor.avgPathLen(triangleIsolate, 1), Double.NaN, "disconnected sampled source");

        Graph disconnected = new Graph();
        disconnected.addEdge(0, 1);
        disconnected.addEdge(2, 3);
        exactMetrics(disconnected, Double.NaN, 0, "two disconnected edges");
        close(GraphProcessor.avgPathLen(disconnected, 1), Double.NaN, "disconnected one source");

        Graph empty = new Graph();
        exactMetrics(empty, Double.NaN, Double.NaN, "empty graph");
        Graph singleton = new Graph();
        singleton.addNode(42);
        exactMetrics(singleton, 0, 0, "single vertex");
        clique.addEdge(0, 0); // Self-loops do not create triangles or new shortest paths.
        exactMetrics(clique, 1, 1, "clique with self-loop");

        rejects(() -> GraphProcessor.avgPathLen(cycle, 0));
        rejects(() -> GraphProcessor.avgPathLen(cycle, -1));
        rejects(() -> GraphProcessor.clusterCoeff(cycle, 0));
        rejects(() -> GraphProcessor.clusterCoeff(cycle, -1));
        rejects(() -> GraphProcessor.avgPathLen(null));
        rejects(() -> GraphProcessor.clusterCoeff(null));

        close(WattsStrogatzSmallWorld.normalizeMetric(4, 2), 2, "finite normalization");
        close(WattsStrogatzSmallWorld.normalizeMetric(0, 2), 0, "zero value normalization");
        close(WattsStrogatzSmallWorld.normalizeMetric(0, 0), Double.NaN, "k2 zero baseline");
        close(WattsStrogatzSmallWorld.normalizeMetric(1, 0), Double.NaN, "nonzero over zero baseline");
        close(WattsStrogatzSmallWorld.normalizeMetric(Double.NaN, 2), Double.NaN, "undefined metric");
        close(WattsStrogatzSmallWorld.normalizeMetric(1, Double.POSITIVE_INFINITY), Double.NaN, "infinite baseline");
        close(WattsStrogatzSmallWorld.normalizeMetric(Double.MAX_VALUE, Double.MIN_VALUE), Double.NaN, "overflowing ratio");
        if (!"undefined".equals(GraphDrawer.formatMetric(Double.NaN)) ||
                !"undefined".equals(GraphDrawer.formatMetric(Double.POSITIVE_INFINITY))) {
            throw new AssertionError("Nonfinite display values must be labelled undefined");
        }
        if (GraphDrawer.isPlottable(null) || GraphDrawer.isPlottable(new Point(0, Double.NaN)) ||
                GraphDrawer.isPlottable(new Point(Double.POSITIVE_INFINITY, 1)) ||
                GraphDrawer.isPlottable(new Point(0, Double.NEGATIVE_INFINITY)) ||
                !GraphDrawer.isPlottable(new Point(.5, 0))) {
            throw new AssertionError("Invalid metric points must be skipped without discarding finite zeros");
        }
    }

    public static void main(String[] args) {
        metricRegressions();
        WattsStrogatzSmallWorld.validateParameters(100, 4, .005);
        WattsStrogatzSmallWorld.validateParameters(20, 18, 1);
        for (double step : new double[]{0, -.1, Double.MIN_VALUE, Double.NaN, Double.POSITIVE_INFINITY, 1.1}) {
            rejects(() -> WattsStrogatzSmallWorld.validateParameters(100, 4, step));
        }
        rejects(() -> WattsStrogatzSmallWorld.validateParameters(19, 4, .1));
        rejects(() -> WattsStrogatzSmallWorld.validateParameters(20, 3, .1));
        rejects(() -> WattsStrogatzSmallWorld.validateParameters(20, 20, .1));
        rejects(() -> GraphProcessor.createRegularGraph(10, 3));

        Graph complete = GraphProcessor.createRegularGraph(5, 4);
        if (GraphProcessor.chooseRewireTarget(complete, 0) != -1) throw new AssertionError("Complete graph must skip rewiring");
        GraphProcessor.reconnectGraph(complete, 1, 4); // Previously never terminated.
        if (complete.E() != 10) throw new AssertionError("Complete graph edges changed");

        Graph dense = new Graph();
        for (int vertex = 0; vertex < 100; vertex++) dense.addNode(vertex);
        for (int vertex = 1; vertex < 99; vertex++) dense.addEdge(0, vertex);
        for (int attempt = 0; attempt < 100; attempt++) {
            if (GraphProcessor.chooseRewireTarget(dense, 0) != 99) throw new AssertionError("Missed sole valid target");
        }

        for (int attempt = 0; attempt < 100; attempt++) {
            Graph graph = GraphProcessor.createRegularGraph(21, 18);
            int edges = graph.E();
            GraphProcessor.reconnectGraph(graph, 1, 18);
            if (graph.E() != edges) throw new AssertionError("Rewiring changed edge count");
            for (int vertex = 0; vertex < graph.V(); vertex++) {
                if (graph.hasEdge(vertex, vertex)) throw new AssertionError("Self-loop introduced");
                for (int neighbor : graph.adjacentTo(vertex)) {
                    if (!graph.hasEdge(neighbor, vertex)) throw new AssertionError("Undirected symmetry lost");
                }
            }
        }
        rejects(() -> GraphProcessor.reconnectGraph(complete, Double.NaN, 4));
        System.out.println("PASS: analytic full/big/sample metrics, undefined-value display guards, input guards, dense rewiring and graph invariants.");
    }
}
