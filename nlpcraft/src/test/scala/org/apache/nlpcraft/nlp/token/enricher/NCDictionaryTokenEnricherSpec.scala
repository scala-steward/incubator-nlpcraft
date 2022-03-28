/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nlpcraft.nlp.token.enricher

import org.apache.nlpcraft.internal.util.NCResourceReader
import org.apache.nlpcraft.nlp.token.enricher.*
import org.apache.nlpcraft.nlp.util.*
import org.junit.jupiter.api.*

import scala.jdk.CollectionConverters.*

/**
  *
  */
class NCDictionaryTokenEnricherSpec:
    private val dictEnricher = new NCEnDictionaryTokenEnricher()

    @Test
    def test(): Unit =
        val txt = "milk XYZ"
        val toks = EN_TOK_PARSER.tokenize(txt).asScala.toSeq

        require(toks.head.getOpt[Boolean]("dict:en").isEmpty)
        require(toks.last.getOpt[Boolean]("dict:en").isEmpty)

        val req = NCTestRequest(txt)

        EN_TOK_LEMMA_POS_ENRICHER.enrich(req, CFG, toks.asJava)
        dictEnricher.enrich(req, CFG, toks.asJava)
        NCTestUtils.printTokens(toks)

        require(toks.head.get[Boolean]("dict"))
        require(!toks.last.get[Boolean]("dict"))