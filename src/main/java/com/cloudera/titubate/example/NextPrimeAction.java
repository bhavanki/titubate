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
package com.cloudera.titubate.example;

import com.cloudera.titubate.CallableAction;

public class NextPrimeAction extends CallableAction {
    @Override
    public Void call() {
        int lastPrime = state().getInt("lastPrime");
        int nextPrime = new PrimeFinder().findNextPrime(lastPrime);
        state().set("lastPrime", nextPrime);
        if (state().has("count")) {
            state().set("count", state().getInt("count") + 1);
        }
        System.out.println("prime = " + nextPrime);
        return null;
    }
}
