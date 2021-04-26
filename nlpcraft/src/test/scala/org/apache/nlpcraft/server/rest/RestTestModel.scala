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

package org.apache.nlpcraft.server.rest

import org.apache.nlpcraft.NCTestElement
import org.apache.nlpcraft.model.{NCElement, NCIntent, NCIntentMatch, NCIntentRef, NCIntentSample, NCModelAdapter, NCResult}

import java.util

/**
  * REST test model.
  */
class RestTestModel extends NCModelAdapter("rest.test.model", "REST test model", "1.0.0") {
    override def getElements: util.Set[NCElement] =
        Set(
            NCTestElement("a"),
            NCTestElement("b"),
            NCTestElement("x", "cat")
        )

    @NCIntent("intent=onA term(t)={tok_id() == 'a'}")
    @NCIntentSample(Array("My A"))
    private def a(ctx: NCIntentMatch): NCResult = NCResult.text("OK")

    @NCIntent("intent=onB term(t)={tok_id() == 'b'}")
    @NCIntentSample(Array("My B"))
    private def b(ctx: NCIntentMatch): NCResult = NCResult.text("OK")
}