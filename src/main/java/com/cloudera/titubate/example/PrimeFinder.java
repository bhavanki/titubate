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

public class PrimeFinder {
    public int findNextPrime(int lastPrime) {
        if (lastPrime < 2) {
            throw new IllegalArgumentException("First prime is 2");
        }
        int i = lastPrime + 1;
        while (true) {
            if (isPrime(i)) {
                return i;
            }
            i++;
        }
    }
    /*
     * From http://www.mkyong.com/java/how-to-determine-a-prime-number-in-java/
     */
    public static boolean isPrime(int n) {
        if (n % 2 == 0) {
            return false;
        }
        for (int i = 3; i * i <= n; i += 2) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }
}
