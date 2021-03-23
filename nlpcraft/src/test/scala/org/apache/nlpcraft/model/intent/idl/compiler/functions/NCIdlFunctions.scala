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

package org.apache.nlpcraft.model.intent.idl.compiler.functions

import org.apache.nlpcraft.model.intent.compiler.{NCIdlCompiler, NCIdlCompilerGlobal}
import org.apache.nlpcraft.model.intent.{NCIdlContext, NCIdlFunction, NCIdlStackItem}
import org.apache.nlpcraft.model.{NCCompany, NCModel, NCModelView, NCRequest, NCToken, NCUser}
import org.junit.jupiter.api.BeforeEach

import java.util
import java.util.{Collections, Optional}
import scala.collection.JavaConverters._

/**
  * Tests for IDL functions.
  */
private[functions] trait NCIdlFunctions {
    private final val MODEL_ID = "test.mdl.id"

    private final val MODEL: NCModel = new NCModel {
        override val getId: String = MODEL_ID
        override val getName: String = MODEL_ID
        override val getVersion: String = "1.0.0"

        override def getOrigin: String = "test"
    }

    @BeforeEach
    def before(): Unit = NCIdlCompilerGlobal.clearCache(MODEL_ID)

    case class TrueFunc(
        truth: String,
        token: NCToken = tkn(),
        idlCtx: NCIdlContext = ctx()
    ) {
        val function: NCIdlFunction = {
            val intents = NCIdlCompiler.compileIntents(s"intent=i term(t)={$truth}", MODEL, MODEL_ID)

            require(intents.size == 1)

            val intent = intents.head

            require(intent.terms.size == 1)

            new NCIdlFunction() {
                override def apply(v1: NCToken, v2: NCIdlContext): NCIdlStackItem = intent.terms.head.pred.apply(v1, v2)
                override def toString(): String = s"Function, based on term: $function"
            }
        }

        private def nvl(s: String, name: String): String = if (s != null) s else s"$name (not set)"
        private def t2s(t: NCToken) = s"text=${nvl(t.getOriginalText, "text")} [${nvl(t.getId, "id")}]"

        override def toString: String =
            s"Function [" +
            s"token=${t2s(token)}, " +
            s"function=$truth" +
            s"]"
    }

    protected def ctx(
        usr: NCUser = null,
        comp: NCCompany = null,
        srvReqId: String = null,
        normTxt: String = null,
        recTimestamp: Long = 0,
        remAddress: String = null,
        clientAgent: String = null,
        reqData: Map[String, AnyRef] = Map.empty[String, AnyRef]
    ): NCIdlContext =
        NCIdlContext(
            req =
                new NCRequest() {
                    override def getUser: NCUser = usr
                    override def getCompany: NCCompany = comp
                    override def getServerRequestId: String = srvReqId
                    override def getNormalizedText: String = normTxt
                    override def getReceiveTimestamp: Long = recTimestamp
                    override def getRemoteAddress: Optional[String] = Optional.ofNullable(remAddress)
                    override def getClientAgent: Optional[String] = Optional.ofNullable(clientAgent)
                    override def getRequestData: util.Map[String, AnyRef] = reqData.asJava
                }
        )

    protected def tkn(
        id: String = null,
        srvReqId: String = null,
        parentId: String = null,
        value: String = null,
        txt: String = null,
        start: Int = 0,
        end: Int = 0,
        groups: Seq[String] = Seq.empty,
        ancestors: Seq[String] = Seq.empty,
        aliases: Set[String] = Set.empty,
        partTokens: Seq[NCToken] = Seq.empty,
        meta: Map[String, AnyRef] = Map.empty[String, AnyRef]
    ): NCToken = {
        val map = new util.HashMap[String, AnyRef]

        map.putAll(meta.asJava)

        map.put("nlpcraft:nlp:origtext", txt)
        map.put("nlpcraft:nlp:origtext", txt)

        new NCToken {
            override def getModel: NCModelView = MODEL

            override def getServerRequestId: String = srvReqId
            override def getId: String = id
            override def getParentId: String = parentId
            override def getAncestors: util.List[String] = ancestors.asJava
            override def getPartTokens: util.List[NCToken] = partTokens.asJava
            override def getAliases: util.Set[String] = aliases.asJava
            override def getValue: String = value
            override def getGroups: util.List[String] =
                if (groups.isEmpty && id != null) Collections.singletonList(id) else groups.asJava
            override def getStartCharIndex: Int = start
            override def getEndCharIndex: Int = end
            override def isAbstract: Boolean = false
            override def getMetadata: util.Map[String, AnyRef] = map
        }
    }

    protected def test(funcs: TrueFunc*): Unit =
        for (f ← funcs) {
            val res =
                try
                    f.function.apply(f.token, f.idlCtx).value
                catch {
                    case e: Exception ⇒ throw new Exception(s"Execution error [func=$f]", e)
                }

            res match {
                case b: java.lang.Boolean ⇒
                    require(
                        b,
                        s"Unexpected FALSE result [" +
                            s"testFunc=$f " +
                            s"]"
                    )
                case _ ⇒
                    require(
                        requirement = false,
                        s"Unexpected result type [" +
                            s"resType=${if (res == null) null else res.getClass.getName}, " +
                            s"testFunc='$f', " +
                            s"resValue=$res" +
                            s"]"
                    )
            }
        }
}
