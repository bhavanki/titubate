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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;

public class Framework {
  private static final Logger LOG = LoggerFactory.getLogger(Framework.class);

  private static final String MODULE_DIR = "modules";

  private final NodeKeeper nodeKeeper;
  private String configDir = null;

  public Framework(NodeKeeper nodeKeeper) {
    this.nodeKeeper = nodeKeeper;
  }

  public Framework setConfigDir(String configDir) {
    this.configDir = configDir;
    return this;
  }

  public void run(String graph, Environment env) {
    run(graph, env, new State());
  }
  /**
   * Runs a random walk test.
   * 
   * @param graph name of graph to run
   * @param env test environment
   * @param state initial/current test state
   */
  public void run(String graph, Environment env, State state) {
    try {
      Node node = nodeKeeper.getNode(graph);
      node.visit(env, state, new Properties());  // wait what?
    } catch (NodeException e) {
      LOG.error("Error during random walk", e);
    }
  }

  static class Opts {
    @Parameter(names={"-h", "-?", "--help", "-help"}, help=true)
    boolean help = false;
    @Parameter(names="--config-dir", required=true,
               description="test configuration directory")
    String configDir;
    @Parameter(names="--config-file-name", required=false,
               description="test configuration file")
    String configFileName = "randomwalk.conf";
    @Parameter(names="--test-id", required=false,
               description="a unique test identifier (like a hostname, or pid)")
    String testId = null;
    @Parameter(names="--graph", required=true,
               description="the name of the test graph to run")
    String graph;

    public boolean parseArgs(String programName, String[] args, Object ... others) {
      JCommander commander = new JCommander();
      commander.addObject(this);
      for (Object other : others)
        commander.addObject(other);
        commander.setProgramName(programName);
      try {
        commander.parse(args);
      } catch (ParameterException ex) {
        commander.usage();
        System.err.println(ex.getMessage());
        return false;
      }
      if (help) {
        commander.usage();
        return false;
      } else {
        return true;
      }
    }
  }

  public static void main(String[] args) throws Exception {
    Opts opts = new Opts();
    if (!opts.parseArgs(Framework.class.getName(), args)) {
      return;
    }

    Properties props = new Properties();
    FileInputStream fis =
      new FileInputStream(new File(opts.configDir, opts.configFileName));
    props.load(fis);
    fis.close();

    props.setProperty(Environment.KEY_TEST_ID,
                      (opts.testId != null ? opts.testId :
                       UUID.randomUUID().toString()));

    Environment env = new Environment(props);
    NodeKeeper nodeKeeper = new NodeKeeper(new File(opts.configDir, MODULE_DIR));
    new Framework(nodeKeeper).setConfigDir(opts.configDir).run(opts.graph, env);
  }
}
