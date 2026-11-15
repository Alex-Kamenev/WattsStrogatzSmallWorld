# WattsStrogatzSmallWorld

## Maintenance in Alex-Kamenev's fork

This remains a historical simulation forked from
[mtjwy/WattsStrogatzSmallWorld](https://github.com/mtjwy/WattsStrogatzSmallWorld),
not original research authored by Alex. The original documentation/report and source credits are retained below.
The maintenance branch fixes input and rewiring termination, corrects inherited
metric formulas, and supplies a reproducible executable JAR build. It does not
establish new scientific results or reproduce the historical report's plots.

Install JDK 17+ and Python 3, with `JAVA_HOME` or the JDK tools on `PATH`:

```sh
python build.py --test
java -jar target/SmallWorld.jar
```

On Windows, build first and then use `run.bat`. The tracked Java utility sources
are included in the build; a separately downloaded `stdlib.jar` is not needed.
The historical Maven/Eclipse files remain as original metadata; `build.py` is the
verified build path. Generated classes/JARs stay under ignored `target/`.

The interactive simulation requires a graphical desktop. Enter 20–8000 nodes,
an even neighbor count from 2–40 smaller than the node count, and a finite
probability step from 0.001–1.0. The step bound limits the run to at most 1000
intervals. Dense or complete vertices with no eligible rewiring target are
skipped; they no longer hang in a random-selection loop. The endpoint p=1 is
computed once. Invalid input is reported before any graphics window is created.

`python build.py --test` compiles all original sources, runs headless input/dense
graph regressions, analytic metric checks, and edge-count/self-loop/symmetry
invariants over repeated rewiring. Verified with Microsoft OpenJDK 21. Historical
utility deprecation/unchecked warnings remain. The full GUI, performance timings,
and scientific simulation plots were not rerun.

## Corrected metric definitions

Results change from the original implementation. Clustering is the mean of local
coefficients across all vertices: `c_v = 2T_v / (d_v * (d_v - 1))`, with zero for
degree below two and self-loops ignored. Zero coefficients are included in the
mean. It is not a ratio of global triangle/wedge totals (transitivity). A four-node
cycle has clustering zero; a clique has clustering one; a triangle with one leaf
has mean local clustering `7/12` (its transitivity is `3/5`).

Average shortest path length averages distances over all distinct ordered vertex
pairs, dividing by `n * (n - 1)`. A disconnected or empty graph returns `NaN` to
mark the global result undefined; a single vertex returns zero. Missing paths
never contribute a misleading zero distance. The historical `*Big` APIs use the
same definitions and precision as the full APIs instead of a separate rounded
formula. Products are converted to double before multiplication to avoid integer
overflow in denominators.

The sampled path API chooses `m = min(sampleNum, n)` distinct source vertices
uniformly without replacement, sums each source's distances to all other vertices,
and divides by `m * (n - 1)`. It estimates the same full-graph mean, becomes exact
when all sources are used, and detects disconnection even with one source. Sampled
clustering averages the selected local coefficients, including zeros. A positive
sample count is required. Analytic tests cover full, big and sampled APIs; no
Monte Carlo accuracy or performance study was performed.

Normalization requires a finite value and positive finite baseline. In particular,
`k=2` ring lattices have zero baseline clustering, so normalized clustering is
undefined rather than forced to one or divided by zero. Nonfinite metric values
are labelled `undefined` in the GUI, and their points are omitted from the plot;
finite zero values remain valid. Pure label/filter guards are tested headlessly;
no full interactive GUI test was performed.

Definitions follow NetworkX's primary documentation:
[local clustering](https://networkx.org/documentation/stable/reference/algorithms/generated/networkx.algorithms.cluster.clustering.html),
[mean local clustering](https://networkx.org/documentation/stable/reference/algorithms/generated/networkx.algorithms.cluster.average_clustering.html),
and [average shortest path length](https://networkx.org/documentation/stable/reference/algorithms/generated/networkx.algorithms.shortest_paths.generic.average_shortest_path_length.html).
The original report below remains historical and has not been silently updated to
claim the corrected numerical results.

## Original project documentation

##For project detail, please read 'Watts-Strogatz Small World Simulation Project Report.pdf'

This project is to simulate a small-world graph according to Watts-Strogatz Model. The smallworld graph consists of a ring lattice with 5000 nodes (N = 5000) and each node has 20 short range neighbors (k = 20). The goal is to understand the properties of small-world graphs through simulation.

In this project, average path lengths, clustering coefficient of small-world
graphs are computed and analyzed. Through this project, we gain thorough
understanding of the Watts-Strogatz model, which explores the formation of networks that result in the "small world" phenomenon.

This project is developed in Windows 7 using java in Eclipse. 

Files included are
	 6 source code files:
			Graph.java
			GraphDrawer.java
			GraphProcessor.java
			PathExplorer.java
			Point.java
			WattsStrogatzSmallWorld.java

	 a jar that for create User Interface:
	 		stdlib.jar

	 the exported excutable jar from Eclipse of our project:
	 		SmallWorld.jar

	 the file to run our project:
	 		run.bat
	
	a user guide:
		UserGuide.pdf

To run the project:

	In Windows 7, just double click the run.bat file.
		Input example 1:
			num of nodes = 100
			num of neighbors = 4
			probability = 0.005

		Input example 2:
			num of nodes = 100
			num of neighbors = 4
			probability = 0.1

	In Linux, use command line: java -jar SmallWorld.jar
