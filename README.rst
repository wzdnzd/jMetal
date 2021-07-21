jMetal project Web site
=======================
.. image:: https://travis-ci.org/jMetal/jMetal.svg?branch=master
    :alt: Build Status
    :target: https://travis-ci.org/jMetal/jMetal

.. image:: https://readthedocs.org/projects/jmetal/badge/?version=latest
   :alt: Documentation Status
   :target: https://jmetal.readthedocs.io/?badge=latest

jMetal is a Java-based framework for multi-objective optimization with metaheuristics. The current stable version is 5.10 (https://github.com/jMetal/jMetal/tree/jmetal-5.10), which is based on the description of jMetal 5 included in the paper "Redesigning the jMetal Multi-Objective Optimization Framework" (http://dx.doi.org/10.1145/2739482.2768462), presented at GECCO 2015.

The current development version (5.11-SNAPSHOT) is a Maven project structured in seven subprojects:


+---------------------+------------------------------------+
| Sub-project         |  Contents                          | 
+=====================+====================================+
| jmetal-core         |  Core classes                      |
+---------------------+------------------------------------+
| jmetal-solution     |  Solution encodings                |
+---------------------+------------------------------------+
| jmetal-algorithm    |  Algorithm implementations         |
+---------------------+------------------------------------+
| jmetal-problem      |  Benchmark problems                |
+---------------------+------------------------------------+
| jmetal-example      |  Examples                          |
+---------------------+------------------------------------+
| jmetal-lab          |  Experimentation and visualization |
+---------------------+------------------------------------+
| jmetal-experimental |  New features in development       |
+---------------------+------------------------------------+
| jmetal-parallel     |  Parallel extensions               |
+---------------------+------------------------------------+

The most recent documentation is hosted in https://jmetal.readthedocs.io  (the old documentation site is located in https://github.com/jMetal/jMetalDocumentation).

Comments and suggestions are very welcome.

Changelog
---------
* [2/19/2021] The `Solution <https://github.com/jMetal/jMetal/blob/master/jmetal-core/src/main/java/org/uma/jmetal/solution/Solution.java>`_ interface has been refactorized.

* [2/19/2021] New implementation of quality indicators to remove the dependence of jMetal classes. Now, all of them accept as a parameter a matrix containing objective values.

* [1/21/2021] Added the MicroFAME multi-objective genetic algorith, described in: Alejandro Santiago, Bernabé Dorronsoro, Héctor Fraire, Patricia Ruíz: Micro-Genetic algorithm with fuzzy selection of operators for multi-Objective optimization: microFAME. Swarm and Evolutionary Computation, V.61, March 2021. `DOI <https://doi.org/10.1016/j.swevo.2020.100818>`_. Contributed by Alejandro Santiago.

* [10/1/2020] Added the problems described in: Ryoji Tanabe and Hisao Ishibuchi: An Easy-to-use Real-world Multi-objective Optimization Problem Suite. Applied Soft Computing, V.89, April 2020. `DOI <https://doi.org/10.1016/j.asoc.2020.106078>`_.

* [9/14/2020] New ``jmetal-auto`` sub-module. It contains asynchronous versions of a genetic algorithm and NSGA-II, and a synchronous evaluator based on Apache Spark.

* [7/23/2020] The former ``jmetal-auto`` sub-project and the stuff related to using a component-based evolutionary template have been moved to a new sub-project called ``jmetal-experimental``, which is intended to explore new features that can be consolidated in the project in the future.

* [7/21/2020] jMetal 5.10 has been released.

* [7/15/2020] `Automatic generation of HTML pages <https://jmetal.readthedocs.io/en/latest/experimentation.html#generation-of-html-pages>_`. summarizing the results of experimental studies. Contributed by Javier Pérez Abad.

* [7/14/2020] New experiment component: `GenerateFriedmanHolmTestTables <https://github.com/jMetal/jMetal/blob/master/jmetal-lab/src/main/java/org/uma/jmetal/lab/experiment/component/impl/GenerateFriedmanHolmTestTables.java>`_. Contributed by Javier Pérez Abad.

* [3/19/2020] New quality indicator: `NormalizedHypervolume <https://github.com/jMetal/jMetal/blob/master/jmetal-core/src/main/java/org/uma/jmetal/qualityindicator/impl/NormalizedHypervolume.java>`_.

* [3/19/2020] The jMetal project adopts Java 11.

* [2/11/2020] All the files containing Pareto front approximations and weight vectors have been moved to the ``resources`` folder, located in root project directory.
