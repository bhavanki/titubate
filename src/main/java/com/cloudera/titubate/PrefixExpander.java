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

import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrefixExpander {
    private static final Logger LOG = LoggerFactory.getLogger(PrefixExpander.class);

    private final Map<String,String> expansions;
    private final boolean noExpansions;

    public PrefixExpander(Map<String, String> expansions) {
        if (expansions == null) {
            this.expansions = Collections.emptyMap();
        } else {
            this.expansions = Collections.unmodifiableMap(new java.util.HashMap<String, String>(expansions));
        }
        noExpansions = this.expansions.isEmpty();
    }

    public String expand(String s) {
        if (noExpansions) {
            return s;
        }
        if (s.endsWith(".xml")) {
            return s;
        }
        int index = s.indexOf('.');
        if (index == -1) {
            return s;
        }
        String id = s.substring(0, index);
        if (!expansions.containsKey(id)) {
            LOG.warn("Prefix " + id + " was not found in expansions");
            return s;
        }
        return new StringBuilder(expansions.get(id))
            .append(s.substring(index)).toString();
    }
}
