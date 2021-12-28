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

package org.apache.nlpcraft.internal.nlp.token.enricher

import org.apache.nlpcraft.NCToken
import org.apache.nlpcraft.internal.nlp.token.parser.opennlp.NCEnOpenNlpTokenParser
import org.apache.nlpcraft.internal.nlp.util.{NCTestRequest, NCTestToken, NCTestUtils}
import org.junit.jupiter.api.{BeforeEach, Test}

import scala.jdk.CollectionConverters.*

/**
  *
  */
class NCEnQuotesTokenEnricherSpec:
    private var parser: NCEnOpenNlpTokenParser = _
    private var enricher: NCEnQuotesTokenEnricher = _

    @BeforeEach
    def start(): Unit =
        parser = NCTestUtils.makeAndStart(
            new NCEnOpenNlpTokenParser(
                "opennlp/en-token.bin",
                "opennlp/en-pos-maxent.bin",
                "opennlp/en-lemmatizer.dict"
            )
        )
        enricher = NCTestUtils.makeAndStart(new NCEnQuotesTokenEnricher)

    /**
      *
      * @param txt
      * @param quotes
      */
    private def check(txt: String, quotes: Set[Integer]): Unit =
        val toks = parser.parse(NCTestRequest(txt), null)
        val toksSeq = toks.asScala.toSeq
        enricher.enrich(NCTestRequest(txt), null, toks)
        NCTestUtils.printTokens(toksSeq, "quoted:en")
        toksSeq.zipWithIndex.foreach { case (tok, idx) =>
            require(!(tok.get[Boolean]("quoted:en") ^ quotes.contains(idx)))
        }

    @Test
    def test(): Unit =
        check("It called ' test data '", Set(3, 4))
        check("It called ' test data ' , ' test data '", Set(3, 4, 8, 9))