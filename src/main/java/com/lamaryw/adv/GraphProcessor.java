package com.lamaryw.adv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


/**
 * GraphProcessor contains methods to process a graph
 * according to WattsStrogatz Small World model.
 * It also contains methods to compute average path length
 * and clustering coefficient.
 *
 *
 */
public class GraphProcessor {

	/**
	 * Greate a regular graph with n nodes and every node has k neighbors
	 *
	 * @param n number of node
	 * @param k number of neighbors
	 * @return Graph G
	 */
	public static Graph createRegularGraph(int n, int k) {
		if (n < 3 || k < 2 || k >= n || k % 2 != 0) {
			throw new IllegalArgumentException("A ring lattice requires n >= 3 and an even k with 2 <= k < n.");
		}

		Graph G = new Graph();
		//double step = (2 * PI) / n;
		//double x = 0;

		for (int i = 0; i < n; i++) {
			//StdDraw.point(cos(x), sin(x));//point ID is i
			G.addNode(i);
			//x += step;
		}

		// Add the circle links.

		int kk = k / 2;

		for (int i = 0; i < n; i++) {
			for (int j = 1; j <= kk; j++) {
				int jj = (i + j) % n;
				G.addEdge(i, jj);
				//addEdge(edgeId(i, jj), nodeId(i), nodeId(jj));
			}
		}

		return G;
	}

	/**
	 * Reconnect graph G with probability p
	 * @param G
	 * @param p: reconnecting probability
	 * @param k: number of neighbors
	 */
	public static void reconnectGraph(Graph G, double p, int k) {
		if (G == null || !Double.isFinite(p) || p < 0 || p > 1 || k < 2 || k >= G.V() || k % 2 != 0) {
			throw new IllegalArgumentException("Invalid graph, probability, or neighbor count.");
		}
		/*
		//first implementation, not correct
		for (Integer v : G.getNodes()) {
			for (Integer w : G.getNodes()) {
				if (G.hasEdge(v, w)) {
					if (Math.random() <= p) {
						//G.deleteEdge(v, w);
						int randomVertex = v;
						while (randomVertex == v) {//choose a random vertex other than v
							randomVertex = 0 + (int)(Math.random() * ((G.V() - 1 - 0) + 1));// vertex id is from 0 to G.V() - 1
						}
						G.addEdge(v, randomVertex);
					}
				}
			}
		}
		*/

		/*
		 * reconnecting algorithm:
		 * 		For each node i and each edge (i, j) with i < j
		 * 			with probability p, replace (i,j) with (i, k) where k is
		 * 			chosen uniformly from vertices not equal to or adjacent to i
		 */
		for (int i = 0; i < G.V(); i++) {
			for (int j = i + 1; j <= i + k / 2; j++) {//consider its k/2 neighbors on its larger id side
				int jj = j >= G.V() ? j % G.V() : j;//round up node id; if jj >= G.V(), restart jj from 0
				if (G.hasEdge(i, jj)) {
					if (Math.random() <= p) {//reconnecting with probability p
						int randomVertex = chooseRewireTarget(G, i);
						if (randomVertex < 0) continue; // A saturated vertex has no eligible target.
						G.addEdge(i, randomVertex);
						G.deleteEdge(i, jj);
					}
				}
			}
		}
	}

	// Ring-lattice vertices are numbered 0..V-1. Rejection sampling is fast for
	// sparse graphs; the finite fallback remains uniform for dense graphs.
	static int chooseRewireTarget(Graph graph, int vertex) {
		int available = graph.V() - 1 - graph.degree(vertex);
		if (available == 0) return -1;
		ThreadLocalRandom random = ThreadLocalRandom.current();
		for (int attempt = 0; attempt < 32; attempt++) {
			int candidate = random.nextInt(graph.V());
			if (candidate != vertex && !graph.hasEdge(vertex, candidate)) return candidate;
		}
		int rank = random.nextInt(available);
		for (int candidate = 0; candidate < graph.V(); candidate++) {
			if (candidate != vertex && !graph.hasEdge(vertex, candidate) && rank-- == 0) return candidate;
		}
		throw new IllegalStateException("Graph vertices must be numbered 0..V-1.");
	}


    /**
     * Mean shortest path length over distinct ordered vertex pairs.
     * A disconnected or empty graph has no global finite average: return NaN.
     * A single vertex has average path length zero.
     */
    public static double avgPathLen(Graph graph) {
        List<Integer> vertices = metricVertices(graph);
        return averagePathLength(graph, vertices, vertices.size());
    }

    /** Historical API retained; uses the same definition and precision. */
    public static double avgPathLenBig(Graph graph) {
        return avgPathLen(graph);
    }

    /**
     * Estimate the global mean using uniformly sampled distinct source vertices.
     * Each source contributes distances to every other vertex, so the denominator
     * is m * (n - 1), not sampleNum * (sampleNum - 1). At least n samples is exact.
     * Even one sampled source detects a disconnected undirected graph.
     */
    public static double avgPathLen(Graph graph, int sampleNum) {
        List<Integer> vertices = metricVertices(graph);
        int sources = selectSample(vertices, sampleNum);
        return averagePathLength(graph, vertices, sources);
    }

    private static double averagePathLength(Graph graph, List<Integer> vertices, int sources) {
        int n = vertices.size();
        if (n == 0) return Double.NaN;
        if (n == 1) return 0.0;
        double total = 0.0;
        for (int index = 0; index < sources; index++) {
            int source = vertices.get(index);
            PathExplorer explorer = new PathExplorer(graph, source);
            for (int target : vertices) {
                if (source == target) continue;
                if (!explorer.hasPathTo(target)) return Double.NaN;
                total += explorer.pathLenTo(target);
            }
        }
        return total / ((double) sources * (n - 1));
    }

    /**
     * Mean local clustering over all vertices, including degree-zero/one zeros.
     * Local c_v = 2T_v / (d_v * (d_v - 1)); self-loops are ignored.
     * This is not the ratio of global triangle and wedge totals (transitivity).
     */
    public static double clusterCoeff(Graph graph) {
        List<Integer> vertices = metricVertices(graph);
        return averageClustering(graph, vertices, vertices.size());
    }

    /** Historical API retained; no separate rounded or overflow-prone formula. */
    public static double clusterCoeffBig(Graph graph) {
        return clusterCoeff(graph);
    }

    /** Mean local clustering on a uniform sample without replacement. */
    public static double clusterCoeff(Graph graph, int sampleNum) {
        List<Integer> vertices = metricVertices(graph);
        int sources = selectSample(vertices, sampleNum);
        return averageClustering(graph, vertices, sources);
    }

    private static double averageClustering(Graph graph, List<Integer> vertices, int sources) {
        if (sources == 0) return Double.NaN;
        double total = 0.0;
        for (int index = 0; index < sources; index++) {
            total += localClustering(graph, vertices.get(index));
        }
        return total / sources;
    }

    private static double localClustering(Graph graph, int vertex) {
        List<Integer> neighbors = new ArrayList<Integer>();
        for (int neighbor : graph.adjacentTo(vertex)) {
            if (neighbor != vertex) neighbors.add(neighbor);
        }
        int degree = neighbors.size();
        if (degree < 2) return 0.0;
        long triangles = 0;
        for (int first = 0; first < degree; first++) {
            for (int second = first + 1; second < degree; second++) {
                if (graph.hasEdge(neighbors.get(first), neighbors.get(second))) triangles++;
            }
        }
        return 2.0 * triangles / ((double) degree * (degree - 1));
    }

    private static List<Integer> metricVertices(Graph graph) {
        if (graph == null) throw new IllegalArgumentException("Graph is required.");
        List<Integer> vertices = new ArrayList<Integer>();
        for (int vertex : graph.getNodes()) vertices.add(vertex);
        return vertices;
    }

    private static int selectSample(List<Integer> vertices, int sampleNum) {
        if (sampleNum <= 0) throw new IllegalArgumentException("Sample count must be positive.");
        int count = Math.min(sampleNum, vertices.size());
        if (count == vertices.size()) return count;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        // A partial Fisher-Yates shuffle gives each size-m source subset equal chance.
        for (int index = 0; index < count; index++) {
            Collections.swap(vertices, index, index + random.nextInt(vertices.size() - index));
        }
        return count;
    }
}
