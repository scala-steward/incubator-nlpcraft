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

package org.apache.nlpcraft.model.dialog

import java.util
import java.util.Collections

import org.apache.nlpcraft.model.{NCElement, NCIntent, NCModel, NCResult}
import org.apache.nlpcraft.{NCTestContext, NCTestEnvironment}
import org.junit.jupiter.api.Assertions.{assertFalse, assertTrue}
import org.junit.jupiter.api.Test

import scala.collection.JavaConverters._

/**
  * Test model.
  */
class NCDialogSpecModel extends NCModel {
    override def getId: String = this.getClass.getSimpleName
    override def getName: String = this.getClass.getSimpleName
    override def getVersion: String = "1.0.0"

    private def mkElement(id: String): NCElement = new NCElement {
        override def getId: String = id
        override def getSynonyms: util.List[String] = Collections.singletonList(id)
    }

    override def getElements: util.Set[NCElement] = Set(mkElement("test1"), mkElement("test2")).asJava

    @NCIntent("intent=test1 term~{id == 'test1'}")
    def onTest1(): NCResult = NCResult.text("ok")

    /**
     * 'test2' requires one and only one 'test1' immediately before in history.
     */
    @NCIntent("intent=test2 flow='^(?:test1)(^:test1)*$' term~{id == 'test2'}")
    def onTest2(): NCResult = NCResult.text("ok")

    /**
      * 'test3' requires 'test1' immediately before in history.
      */
    @NCIntent("intent=test3 flow='test1$' term={id == 'test3'}")
    def onTest3(): NCResult = NCResult.text("ok")
}

/**
  * @see NCDialogSpecModel
  */
@NCTestEnvironment(model = classOf[NCDialogSpecModel], startClient = true)
class NCDialogSpec extends NCTestContext {
    @Test
    @throws[Exception]
    private[dialog] def test2(): Unit = {
        val cli = getClient

        def flow(): Unit = {
            // FAIL: there isn't 'test1' before for 'test2' to match.
            assertFalse(cli.ask("test2").isOk)

            // OK: 'test1' is always ok.
            assertTrue(cli.ask("test1").isOk)

            // OK: 'test2' matches as there is one 'test1' before.
            assertTrue(cli.ask("test2").isOk)

            // OK: 'test1' is always ok.
            assertTrue(cli.ask("test1").isOk)
            assertTrue(cli.ask("test1").isOk)

            // FAIL: there are too many (2) 'test1' before in history,
            // 'test2' requires one and only one 'test1' to match.
            assertFalse(cli.ask("test2").isOk)
        }

        flow()

        cli.clearConversation()
        cli.clearDialog()

        flow()
    }

    @Test
    @throws[Exception]
    private[dialog] def test3(): Unit = {
        val cli = getClient

        def flow(): Unit = {
            // No required history.
            assertFalse(cli.ask("test3").isOk)
            // Always OK.
            assertTrue(cli.ask("test1").isOk)
            // OK, required history.
            assertFalse(cli.ask("test3").isOk)
            // Always OK.
            assertTrue(cli.ask("test1").isOk)
            // Too much history.
            assertFalse(cli.ask("test3").isOk)
        }

        flow()

        cli.clearConversation()
        cli.clearDialog()

        flow()
    }

}