/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nlpcraft.examples.solarsystem.intents

import org.apache.nlpcraft.model.{NCIntent, NCIntentSample, NCIntentTerm, NCResult, NCToken}

class SolarSystemDiscoverer extends SolarSystemIntentsAdapter {
    @NCIntentSample(
        Array(
            "What was discovered by Asaph Hall",
            "What was discovered by Hall",
            "Galileo Galilei planets",
            "Galilei planets"
        )
    )
    @NCIntent(
        "intent=discoverer " +
            "    options={" +
            "        'unused_usr_toks': true " +
            "    }" +
            "    term(discoverer)={tok_id() == 'discoverer'}"
    )
    def discoverer(@NCIntentTerm("discoverer") discoverer: NCToken): NCResult =
        NCResult.text(api.bodyRequest().withFilter("discoveredBy", "cs", discoverer.getNormalizedText).execute().toString())
}