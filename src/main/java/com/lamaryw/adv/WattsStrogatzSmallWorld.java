package com.lamaryw.adv;

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Locale;
import java.util.NoSuchElementException;

import com.lamaryw.util.StdDraw;

/**
 * Simulation WattsStrogatzSmallWorld
 *
 */
public class WattsStrogatzSmallWorld {

	static void validateParameters(int n, int k, double step) {
		if (n < 20 || n > 8000) throw new IllegalArgumentException("Nodes must be between 20 and 8000.");
		if (k < 2 || k > 40 || k >= n || k % 2 != 0) throw new IllegalArgumentException("Neighbors must be even, between 2 and 40, and less than nodes.");
		if (!Double.isFinite(step) || step < 0.001 || step > 1.0) throw new IllegalArgumentException("Probability step must be between 0.001 and 1.0 (at most 1000 intervals).");
	}

	static double normalizeMetric(double value, double baseline) {
		if (!Double.isFinite(value) || !Double.isFinite(baseline) || baseline <= 0) return Double.NaN;
		double ratio = value / baseline;
		return Double.isFinite(ratio) ? ratio : Double.NaN;
	}

	public static void main(String[] args) {

			Scanner in = new Scanner(System.in).useLocale(Locale.ROOT);
			int n;
			int k;
			double step;
			try {

			System.out.println("Enter number of nodes(20~8000): ");
			n = in.nextInt();// number of nodes


			System.out.println("Enter even number of neighbors(2~40, less than nodes): ");
			k = in.nextInt();// number of neighbors


			System.out.println("Enter step for reconnecting probability (0.001~1.00): ");
			// int sampleNum = n / 10;
			step = in.nextDouble();//step for p
			validateParameters(n, k, step);
			} catch (NoSuchElementException | IllegalArgumentException error) {
				System.err.println("Invalid simulation input: " + error.getMessage());
				return;
			}
			double p = 0;// reconnecting probability


			int showTime = (int) (1000 * 0.5);// display and pause for showTime ms


			List<Point> p_apl_list = new LinkedList<Point>();//points of (p, normalized average path length)
			List<Point> p_cc_list = new LinkedList<Point>();//points of (p, normalized clustering coefficient)

			Graph G = GraphProcessor.createRegularGraph(n, k);

			/*
			double avgPathLen0 = GraphProcessor.avgPathLen(G);
			double cc0 = GraphProcessor.clusterCoeff(G);
			*/


			double avgPathLen0 = (n >= 5000 && k > 10) ? GraphProcessor.avgPathLenBig(G)
					: GraphProcessor.avgPathLen(G);
			double cc0 = (n >= 5000 && k > 10) ? GraphProcessor.clusterCoeffBig(G)
					: GraphProcessor.clusterCoeff(G);


			/*
			double avgPathLen0 = (n <= 100) ? GraphProcessor.avgPathLen(G)
					: GraphProcessor.avgPathLen(G, sampleNum);
			double cc0 = (n <= 100) ? GraphProcessor.clusterCoeff(G)
					: GraphProcessor.clusterCoeff(G, sampleNum);
			*/

			GraphDrawer graphDrawer = new GraphDrawer(G);
			double offSet = graphDrawer.getOffSet(); // get offSet of this graphDrawer;
			double xScale = graphDrawer.getxScale();
			double yScale = graphDrawer.getyScale();
			graphDrawer.drawGraph(G);
			graphDrawer.drawText(n, k, p, avgPathLen0, cc0, normalizeMetric(avgPathLen0, avgPathLen0), normalizeMetric(cc0, cc0));

			p_apl_list.add(new Point(p * (xScale - offSet),
					normalizeMetric(avgPathLen0, avgPathLen0) * (yScale - offSet)));//points of (p, normalized average path length)
			p_cc_list.add(new Point(p * (xScale - offSet),
					normalizeMetric(cc0, cc0) * (yScale - offSet)));//points of (p, normalized clustering coefficient)

			graphDrawer.drawNormalizedAPL_Plot(p_apl_list);
			graphDrawer.drawNormalizedCC_Plot(p_cc_list);


			StdDraw.show(showTime); // display and pause for showTime ms

			System.out.println("p = " + p);
			System.out.println("avgPathLen: " + avgPathLen0);
			System.out.println("clusterCoeff: " + cc0);
			System.out.println();


			System.out.println("p = " + p);
			System.out.println("normalized avgPathLen: " + normalizeMetric(avgPathLen0, avgPathLen0) );
			System.out.println("normalized clusterCoeff: " + normalizeMetric(cc0, cc0));
			System.out.println();


			for (int iteration = 1; (p = iteration * step) < 1; iteration++) {
				StdDraw.clear();
				GraphProcessor.reconnectGraph(G, p, k);
				graphDrawer = new GraphDrawer(G);
				graphDrawer.drawGraph(G);

				/*
				double avgPL = GraphProcessor.avgPathLen(G);
				double cc = GraphProcessor.clusterCoeff(G);
				*/

				double avgPL = (n >= 5000 && k > 10) ? GraphProcessor.avgPathLenBig(G)
						: GraphProcessor.avgPathLen(G);
				double cc = (n >= 5000 && k > 10) ? GraphProcessor.clusterCoeffBig(G)
						: GraphProcessor.clusterCoeff(G);

				/*
				double avgPL = (n <= 100) ? GraphProcessor.avgPathLen(G)
						: GraphProcessor.avgPathLen(G, sampleNum);
				double cc = (n <= 100) ? GraphProcessor.clusterCoeff(G)
						: GraphProcessor.clusterCoeff(G, sampleNum);
				*/

				graphDrawer.drawText(n, k, p, avgPL, cc, normalizeMetric(avgPL, avgPathLen0), normalizeMetric(cc, cc0) );

				p_apl_list.add(new Point(p * (xScale - offSet),
						normalizeMetric(avgPL, avgPathLen0) * (yScale - offSet)));//points of (p, normalized average path length)
				p_cc_list.add(new Point(p * (xScale - offSet),
						normalizeMetric(cc, cc0) * (yScale - offSet)));//points of (p, normalized clustering coefficient)

				graphDrawer.drawNormalizedAPL_Plot(p_apl_list);
				graphDrawer.drawNormalizedCC_Plot(p_cc_list);

				StdDraw.show(showTime);

				System.out.println("p = " + p);
				System.out.println("avgPathLen: " + avgPL);
				System.out.println("clusterCoeff: " + cc);
				System.out.println();

				System.out.println("p = " + p);
				System.out.println("normalized avgPathLen: " + normalizeMetric(avgPL, avgPathLen0));
				System.out.println("normalized clusterCoeff: " + normalizeMetric(cc, cc0));
				System.out.println();


				G = GraphProcessor.createRegularGraph(n, k);

			}

			p = 1;
			StdDraw.clear();
			GraphProcessor.reconnectGraph(G, p, k);
			graphDrawer = new GraphDrawer(G);
			graphDrawer.drawGraph(G);

			/*
			double avgPL = GraphProcessor.avgPathLen(G);
			double cc = GraphProcessor.clusterCoeff(G);
			*/

			double avgPL = (n >= 5000 && k > 10) ? GraphProcessor.avgPathLenBig(G)
					: GraphProcessor.avgPathLen(G);
			double cc = (n >= 5000 && k > 10) ? GraphProcessor.clusterCoeffBig(G)
					: GraphProcessor.clusterCoeff(G);

			/*
			double avgPL = (n <= 100) ? GraphProcessor.avgPathLen(G)
					: GraphProcessor.avgPathLen(G, sampleNum);
			double cc = (n <= 100) ? GraphProcessor.clusterCoeff(G)
					: GraphProcessor.clusterCoeff(G, sampleNum);
			*/

			graphDrawer.drawText(n, k, p, avgPL, cc, normalizeMetric(avgPL, avgPathLen0), normalizeMetric(cc, cc0) );

			p_apl_list.add(new Point(p * (xScale - offSet),
					normalizeMetric(avgPL, avgPathLen0) * (yScale - offSet)));
			p_cc_list.add(new Point(p * (xScale - offSet),
					normalizeMetric(cc, cc0) * (yScale - offSet)));
			//p_apl_list.add(new Point(p, normalizeMetric(avgPL, avgPathLen0) ));
			//p_cc_list.add(new Point(p, normalizeMetric(cc, cc0) ));
			graphDrawer.drawNormalizedAPL_Plot(p_apl_list);
			graphDrawer.drawNormalizedCC_Plot(p_cc_list);

			StdDraw.show(showTime);

			System.out.println("p = " + p);
			System.out.println("avgPathLen: " + avgPL);
			System.out.println("clusterCoeff: " + cc);
			System.out.println();

			System.out.println("p = " + p);
			System.out.println("normalized avgPathLen: " + normalizeMetric(avgPL, avgPathLen0));
			System.out.println("normalized clusterCoeff: " + normalizeMetric(cc, cc0));
			System.out.println();



		}
}
