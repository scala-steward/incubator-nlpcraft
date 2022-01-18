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

package org.apache.nlpcraft.nlp.entity.parser.nlp.impl

import org.apache.nlpcraft.*

import java.util
import java.util.List as JList
import java.util.stream.Collectors

/**
  *
  */
object NCNlpImpl:
    private def id = "nlp:token"

import org.apache.nlpcraft.nlp.entity.parser.nlp.impl.NCNlpImpl.*

/**
  *
  */
class NCNlpImpl extends NCEntityParser:
    override def parse(req: NCRequest, cfg: NCModelConfig, toks: JList[NCToken]): JList[NCEntity] =
        toks.stream().map(t =>
            new NCPropertyMapAdapter with NCEntity:
                put(s"$id:lemma", t.getLemma)
                put(s"$id:pos", t.getPos)
                put(s"$id:text", t.getText)
                put(s"$id:index", t.getIndex)
                put(s"$id:startCharIndex", t.getStartCharIndex)
                put(s"$id:endCharIndex", t.getEndCharIndex)

                override val getTokens: JList[NCToken] = util.Collections.singletonList(t)
                override val getRequestId: String = req.getRequestId
                override val getId: String = id
        ).collect(Collectors.toList)