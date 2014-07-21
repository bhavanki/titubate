/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudera.titubate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A module is a directed graph of nodes. It is itself a node, so a module
 * may nest other modules.
 */
public class Module extends Node {
  private static final Logger LOG = LoggerFactory.getLogger(Module.class);

  public static final String INIT_NODE_ID = "_init";
  public static final String PROPERTY_MAX_HOPS = "maxHops";
  public static final String PROPERTY_MAX_SECONDS = "maxSec";
  public static final String PROPERTY_TEARDOWN = "teardown";

  private static class Edge {
    Edge(String nodeId, int weight) {
      if (weight <= 0) {
        throw new IllegalArgumentException("Node " + nodeId +
                                           " has non-positive weight " +
                                           weight);
      }
      this.nodeId = nodeId;
      this.weight = weight;
    }
    String nodeId;
    int weight;
  }

  static class AdjList {
    private Random random = new Random();
    private List<Edge> edges = new ArrayList<Edge>();
    private int totalWeight = 0;

    int size() {
      return edges.size();
    }
    int weight(String nodeId) {
      for (Edge e : edges) {
        if (e.nodeId.equals(nodeId)) {
          return e.weight;
        }
      }
      return -1;
    }

    /**
     * Adds an edge to this list.
     */
    void addEdge(String nodeId, int weight) {
      totalWeight += weight;
      edges.add(new Edge(nodeId, weight));
    }

    /**
     * Chooses a random neighbor node.
     *
     * @return neighbor node ID
     * @throws IllegalStateException if there are no edges in the list
     */
    String randomNeighbor() {
      if (totalWeight == 0) {
        throw new IllegalStateException("No edges in adjacency list");
      }
      int randNum = random.nextInt(totalWeight) + 1;
      String nodeId = null;
      int sum = 0;
      for (Edge e : edges) {
        nodeId = e.nodeId;
        sum += e.weight;
        if (randNum <= sum) {
          break;
        }
      }
      return nodeId;
    }
  }

  private final String source;
  private Map<String,AdjList> adjMap;
  private Map<String, Properties> nodeProps;
  private String initNodeId;
  private Fixture fixture = null;
  private final NodeKeeper nodeKeeper;
  
  Module(String source, Map<String, AdjList> adjMap, Map<String, Properties> nodeProps,
         String initNodeId, Fixture fixture, NodeKeeper nodeKeeper) {
    this.source = source;
    this.adjMap = adjMap;
    this.nodeProps = nodeProps;
    this.initNodeId = initNodeId;
    this.fixture = fixture;
    this.nodeKeeper = nodeKeeper;
  }

  Map<String, AdjList> getAdjacencyMap() {
    return adjMap;
  }
  Fixture getFixture() {
    return fixture;
  }
  NodeKeeper getNodeKeeper() {
    return nodeKeeper;
  }
  String getInitNodeId() {
    return initNodeId;
  }

  @Override
  public void visit(Environment env, State state, Properties props) throws NodeException {
    int maxHops = Integer.MAX_VALUE;
    int maxSec = Integer.MAX_VALUE;
    boolean teardown = true;

    // Properties for a module are those passed in + any in the _init node
    Properties initProps = getProps(INIT_NODE_ID);
    initProps.putAll(props);

    // Get the max hops, max seconds, and teardown flag.
    String prop;
    prop = initProps.getProperty(PROPERTY_MAX_HOPS, "");
    if (!("".equals(prop))) {
      maxHops = Integer.parseInt(prop);
      if (maxHops <= 0) {
        maxHops = Integer.MAX_VALUE;
      }
    }
    prop = initProps.getProperty(PROPERTY_MAX_SECONDS, "");
    if (!("".equals(prop))) {
      maxSec = Integer.parseInt(prop);
      if (maxSec <= 0) {
        maxSec = Integer.MAX_VALUE;
      }
    }
    prop = initProps.getProperty(PROPERTY_TEARDOWN, "true");
    if (!("".equals(prop))) {
      teardown = Boolean.parseBoolean(prop);
    }

    // If this module has a fixture, call its setUp method now.
    if (fixture != null) {
      try {
        LOG.debug("Setting up module");
        fixture.setUp(env, state);
      } catch (FixtureException e) {
        throw new NodeException("Exception in fixture setup", e);
      }
    }

    // Visit the initialization node.
    doVisit(initNodeId, env, state);

    int numHops = 0;
    long maxMs = maxSec * 1000L;
    long startTime = System.currentTimeMillis();

    String curNodeId;
    for (curNodeId = initNodeId; !curNodeId.equalsIgnoreCase("END"); ) {

      // check if maxSec was reached
      long curTime = System.currentTimeMillis();
      if (curTime - startTime > maxMs) {
        LOG.debug("Reached maxSec = " + maxSec);
        break;
      }
      // check if maxHops was reached
      if (numHops >= maxHops) {
        LOG.debug("Reached maxHops = " + maxHops);
        break;
      }
      numHops++;

      // Find the next node
      if (!adjMap.containsKey(curNodeId) && !curNodeId.startsWith("alias.")) {
        throw new NodeException("Reached node " + curNodeId + " without outgoing edges in module " + this);
      }
      AdjList adj = adjMap.get(curNodeId);
      String nextNodeId = adj.randomNeighbor();

      // If an alias node, slide to its target
      Node nextNode = getNode(nextNodeId);
      if (nextNode instanceof AliasNode) {
        nextNodeId = ((AliasNode) nextNode).getTargetId();
      }

      // Visit the node.
      try {
        doVisit(nextNodeId, env, state);
      } catch (Exception e) {
        LOG.debug("Exception occurred at: " + System.currentTimeMillis());
        LOG.debug("Properties for node: " + nextNodeId);
        LOG.debug(getProps(nextNodeId).toString());
        LOG.debug("Environment");
        LOG.debug(env.dump());
        LOG.debug("State information");
        LOG.debug(state.dump());
        throw new NodeException("Error running node " + nextNodeId, e);
      }

      // Move position to the next node.
      curNodeId = nextNodeId;
    }

    // If this module has a fixture, call its tearDown method now.
    if (teardown && fixture != null) {
      LOG.debug("Tearing down module");
      try {
        fixture.tearDown(env, state);
      } catch (FixtureException e) {
        throw new NodeException("Exception in fixture teardown", e);
      }
    }

    timerES.shutdown();
  }

  void doVisit(String nodeId, Environment env, State state) throws NodeException {
    Node n = getNode(nodeId);
    boolean timed = n instanceof Timed;
    if (timed) {
      startTimer(n);
    }
    try {
      n.visit(env, state, getProps(nodeId));
    } finally {
      if (timed) {
        stopTimer(n);
      }
    }
  }

  private static final long LONG_TIME = 5 * 60 * 1000L;  // 5 minutes
  AtomicBoolean runningLong = new AtomicBoolean(false);
  long timerStart;
  ExecutorService timerES = Executors.newSingleThreadExecutor();
  Future<?> runningTaskFuture;

  class TimerTask implements Runnable {
    private Node n;  // node being timed
    TimerTask(Node n) {
      this.n = n;
    }
    @Override
    public void run() {
        try {
          timerStart = System.currentTimeMillis();
          Thread.sleep(LONG_TIME);
        } catch (InterruptedException ie) {
          return;
        }
        long timeSinceLastProgress = System.currentTimeMillis() - n.getProgress();
        if (timeSinceLastProgress > LONG_TIME) {
          LOG.warn("Node " + n + " has been running for " + timeSinceLastProgress / 1000 + " seconds. You may want to look into it.");
          runningLong.set(true);
        }
      }
  }

  private void startTimer(Node n) {
    runningLong.set(false);
    n.progress();
    runningTaskFuture = timerES.submit(new TimerTask(n));
  }

  private void stopTimer(Node n) {
    if (runningTaskFuture != null) {
      runningTaskFuture.cancel(true);
    }
    if (runningLong.get()) {
      LOG.warn("Node " + n + ", which was running long, has now completed after " + (System.currentTimeMillis() - timerStart) / 1000.0 + " seconds");
    }
  }
  
  @Override
  public String toString() {
    return source;
  }

  Node getNode(String id) {
    return nodeKeeper.getNode(id);
  }

  Properties getProps(String nodeId) {
    if (nodeProps == null) {
      nodeProps = new HashMap<String, Properties>();
    }
    if (!nodeProps.containsKey(nodeId)) {
      nodeProps.put(nodeId, new Properties());
    }
    return nodeProps.get(nodeId);
  }
}
