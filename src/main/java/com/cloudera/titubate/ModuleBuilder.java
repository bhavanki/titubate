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

import com.cloudera.titubate.Module.AdjList;
import java.util.Map;
import java.util.Properties;

public class ModuleBuilder {
  private final String source;
  private Map<String,AdjList> adjMap;
  private Map<String, Properties> nodeProps;
  private Map<String,String> prefixes;
  private Fixture fixture = null;
  private final NodeKeeper nodeKeeper;

  public ModuleBuilder(String source, NodeKeeper nodeKeeper) {
    this.source = source;
    if (nodeKeeper == null) {
        throw new IllegalArgumentException("null nodeKeeper");
    }
    this.nodeKeeper = nodeKeeper;
  }

  public ModuleBuilder adjMap(Map<String,AdjList> adjMap) {
    this.adjMap = adjMap;
    return this;
  }
  public ModuleBuilder nodeProps(Map<String,Properties> nodeProps) {
    this.nodeProps = nodeProps;
    return this;
  }
  public ModuleBuilder prefixes(Map<String,String> prefixes) {
    this.prefixes = prefixes;
    return this;
  }
  public ModuleBuilder fixture(Fixture fixture) {
    this.fixture = fixture;
    return this;
  }

  public Module build(String initNodeId) {
    verify();
    return new Module(source, adjMap, nodeProps, prefixes, initNodeId,
                      fixture, nodeKeeper);
  }

  void verify() {
    // adjMap
    if (adjMap == null) {
        throw new IllegalStateException("null adjMap");
    }
  }
}
