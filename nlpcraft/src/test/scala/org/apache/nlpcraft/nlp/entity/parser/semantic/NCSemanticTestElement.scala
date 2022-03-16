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

package org.apache.nlpcraft.nlp.entity.parser.semantic

import org.apache.nlpcraft.nlp.entity.parser.*
import org.apache.nlpcraft.*

import java.util
import java.util.{List as JList, Map as JMap, Set as JSet}
import scala.jdk.CollectionConverters.*

/**
  *
  * @param id
  * @param synonyms
  * @param values
  * @param groups
  */
case class NCSemanticTestElement(
    id: String,
    synonyms: Set[String] = Set.empty,
    values: Map[String, Set[String]] = Map.empty,
    groups: Seq[String] = Seq.empty,
    props: Map[String, AnyRef] = Map.empty
) extends NCSemanticElement:
    override def getId: String = id
    override def getGroups: JSet[String] = groups.toSet.asJava
    override def getValues: JMap[String, JSet[String]] = values.map { (k, v) => k -> v.asJava }.asJava
    override def getSynonyms: JSet[String] = synonyms.asJava
    override def getProperties: JMap[String, Object] = props.asJava

/**
  *
  */
object NCSemanticTestElement:
    def apply(id: String, synonyms: String*) = new NCSemanticTestElement(id, synonyms = synonyms.toSet)
