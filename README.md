# Titubate

v. 1. (obsolete) To stagger. _from [Wiktionary][def]_

Titubate is a simple framework for running "randomwalk" tests. A randomwalk
test is a test whose control flow is defined as a directed graph of test
actions, with weighted edges connecting each action with possible following
actions. A transition from one node to the next is taken on a random edge,
selected in accordance with the edge weights.

This project's code is refactored from [Apache Accumulo][accumulo].

## In Progress

This project is still a work in progress, so don't start relying too heavily
on it just yet. In particular, the Java package for the code will probably
change, based on feedback I get on what's appropriate.

## Modules

A randomwalk test graph is called a *module*. The easiest way to define a
module is using XML. Here is a module for testing a class that finds prime
numbers in a sequence. (The module is also in the set of examples for this
project, along with implementation code.)

```
<module>
  <package prefix="ex" value="com.cloudera.titubate.example"/>
  <fixture id="ex.PrimeFinderFixture" />
  <init id="ex.NextPrimeSetupAction" />
  <node id="ex.NextPrimeSetupAction">
    <property key="countPrimes" value="true" />
    <edge id="ex.NextPrimeAction" weight="1" />
  </node>
  <node id="ex.NextPrimeAction">
    <edge id="ex.CheckPrimeAction" weight="200" />
    <edge id="ex.NextPrimeAction" weight="100" />
    <edge id="ex.NextPrimeCompleteAction" weight="1" />
  </node>
  <node id="ex.CheckPrimeAction">
    <edge id="ex.NextPrimeAction" weight="1" />
  </node>
  <node id="ex.NextPrimeCompleteAction">
    <edge id="END" weight="1" />
  </node>
</module>
```

This module has four nodes. The setup node, referred to by the init element,
is the starting node for the test. It has a single edge to a node that finds
the next prime in the sequence.

That node leads to three possible nodes for the next step in the test: a node
that checks if a number is prime, back to the same node again to generate a new
prime, and a completing node. The edge weights make it so that it is highly
unlikely in each pass that the test will decide to complete; however, eventually
it is expected that it will.

The check node always leads back to the node that generates the next prime.
The completing node always leads to the special "END" node, which terminates the
test.

All of the node IDs in the graph refer to Java classes under the package
"com.cloudera.titubate.example". The package element allows for abbreviating
the package to just "ex", to make the XML less noisy.

The test has a fixture class which can perform overall test setup and teardown.
A fixture can define starting data values and create objects needed for the
test. It can also house common code required throughout the test.

## Types of Nodes

A node in a randomwalk test is a Java object that extends `Node`. Titubate
includes four node implementations, and you are free to add your own.

* `DummyNode` does nothing, but logs its ID if desired. A dummy node is useful
  as a point where many test paths diverge from a common, no-op start, and
  likewise where many test paths can converge back together.
* `AliasNode` is a pseudo-node that cannot be run (or "visited"). Instead, it
  points to another node. When a randomwalk test encounters an alias node, it
  dereferences it to run its target instead.
* `Module` represents a test graph. Since a module is a node, it is possible to
  nest tests inside tests.
* `CallableNode` runs a `CallableAction`, which is only a Java `Callable` that
  has access to test data. The example above is composed of `CallableNodes`;
  the node IDs are the class names of the corresponding `CallableAction`s
  to run.

## Environment, State, and Properties

Data that is needed for running a test can be retrieved and maintained in a
few different ways.

The *environment* is a read-only map of string key/value pairs. It is filled
up at the beginning of a test by reading the "randomwalk.conf" file in the
specified configuration directory. (The file may be empty.)

The *state* is a read-write map of objects keyed by strings. It starts out
empty at the beginning of a test, and data can be written to and read from it
by any test components.

*Node properties* are Java properties that are specific to nodes. Each node
can have its own properties, and a node may only read its own properties.

The environment and state are initialized when the test starts and are passed
to the fixture, if one is defined. Afterward, they are available to every node
in the test. In particular, data in state can be passed from node to node.
A node's properties are also kept across all visits to the node.

## How a Test is Run

A randomwalk test is run by specifying a module and environment
(randomwalk.conf). The environment is loaded, and the module is constructed
from its definition (which includes its nodes and their properties). A blank
state is created. If the test has a fixture, its `setUp` method is called.
Then, the initial node defined in the module is visited, or run.

After each node is visited, the framework selects a new random node to visit
next, based on the weights of the outgoing edges of the visited node. The
process continues until the test ends.

A test can end in several ways.

* The next node chosen is "END". This indicates a normal completion.
* A node throws an exception, indicating a failure or error in the test.
* The maximum number of hops across the graph is crossed. The "maxHops" property
  of a module (node) specifies that limit. By default, there is no limit.
* The maximum number of seconds has elapsed in the run. The "maxSec" property
  of a module (node) specifies that limit. By default, there is no limit.
* The user forcibly kills the test.

In all of these cases except the last, the fixture's `tearDown` method is called
to clean up test resources.

To run a randomwalk test, call the `main` method of the `Framework` class. The
only required options specify the configuration directory and the name of the
file defining the module to run. The module file is expected to be in a
"modules" directory under the configuration directory.

```
java -classpath titubate-0.1.0-SNAPSHOT.jar:lib/* \
  com.cloudera.titubate.Framework --config-dir example --graph primefinder.xml
```

[def]: http://en.wiktionary.org/wiki/titubate
[accumulo]: http://accumulo.apache.org/
