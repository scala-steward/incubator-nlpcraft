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

package org.apache.nlpcraft.common.makro

import com.typesafe.scalalogging.LazyLogging
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.antlr.v4.runtime._
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.makro.antlr4._
import org.apache.nlpcraft.common.makro.antlr4.{NCMacroDslParser ⇒ P}
import scala.collection.mutable

/**
  *
  */
object NCMacroCompiler extends LazyLogging {
    private final val MAX_SYN = 1000
    
    /**
      *
      * @param buffer
      * @param isGroup
      */
    case class StackItem (
        var buffer: mutable.Buffer[String],
        isGroup: Boolean
    )

    /**
      *
      * @param parser
      * @param in
      */
    class FiniteStateMachine(parser: P, in: String) extends NCMacroDslBaseListener {
        private val stack = new mutable.ArrayStack[StackItem]

        // Current min/max quantifier.
        private var min = 1
        private var max = 1

        private var expandedSyns: Set[String] = _

        /**
         *
         * @param optS
         * @param s
         * @return
         */
        private def concat(optS: String, s: String): String = if (optS.isEmpty) s else optS + " " + s

        /**
          *
          * @param errMsg
          * @param ctx
          * @return
          */
        private def compilerError(errMsg: String)(implicit ctx: ParserRuleContext): NCE = {
            val tok = ctx.start
            
            new NCE(mkCompilerError(errMsg, tok.getLine, tok.getCharPositionInLine, in))
        }
    
        /**
          *
          * @param buf
          * @param ctx
          */
        private def checkMaxSyn(buf: mutable.Buffer[String])(implicit ctx: ParserRuleContext): Unit =
            if (buf.size > MAX_SYN)
                throw compilerError(s"Exceeded max number ($MAX_SYN) of macro expansions: ${buf.size}")
    
        override def enterExpr(ctx: NCMacroDslParser.ExprContext): Unit = {
            val buf = mutable.Buffer.empty[String]

            // NOTE: do not allow expression's buffer to be empty.
            // Add harmless empty string.
            buf += ""

            stack.push(StackItem(buf, false))
        }

        override def enterGroup(ctx: P.GroupContext): Unit = {
            // NOTE: group cannot be empty based on the BNF grammar.
            stack.push(StackItem(mutable.Buffer.empty[String], true))
        }

        override def exitExpr(ctx: NCMacroDslParser.ExprContext): Unit = {
            implicit val evidence: ParserRuleContext = ctx
            
            if (stack.size > 1) {
                val expr = stack.pop()
                val prn = stack.top
    
                checkMaxSyn(expr.buffer)

                require(expr.buffer.nonEmpty)

                if (prn.isGroup)
                    prn.buffer ++= expr.buffer
                else
                    prn.buffer = for (z ← expr.buffer; i ← prn.buffer.indices) yield concat(prn.buffer(i), z)
            }
        }

        override def exitGroup(ctx: NCMacroDslParser.GroupContext): Unit = {
            implicit val evidence: ParserRuleContext = ctx
            
            val grp = stack.pop()
            
            // Remove dups.
            grp.buffer = grp.buffer.distinct
    
            checkMaxSyn(grp.buffer)

            require(grp.isGroup)
            
            val prn = stack.top

            prn.buffer = prn.buffer.flatMap {
                s ⇒ (for (z ← grp.buffer; i ← min to max) yield concat(s, s"$z " * i).trim).toSet
            }

            // Reset min max.
            min = 1
            max = 1
        }
    
        override def exitSyn(ctx: P.SynContext): Unit = {
            val syn = (
                if (ctx.TXT() != null) ctx.TXT()
                else if (ctx.INT() != null) ctx.INT()
                else if (ctx.REGEX_TXT() != null) ctx.REGEX_TXT()
                else ctx.DSL_TXT()
            ).getText

            val buf = stack.top.buffer

            require(buf.nonEmpty)

            for (i ← buf.indices) buf.update(i, concat(buf(i), syn))
        }
    
        override def exitList(ctx: P.ListContext): Unit =
            if (ctx.UNDERSCORE() != null) stack.top.buffer += ""

        override def exitMakro(ctx: P.MakroContext): Unit =
            expandedSyns = stack.pop().buffer.map(_.trim).toSet
    
        override def exitMinMax(ctx: P.MinMaxContext): Unit = {
            implicit val evidence: ParserRuleContext = ctx
            
            val minStr = ctx.getChild(1).getText.trim
            val maxStr = ctx.getChild(3).getText.trim
    
            try
                min = java.lang.Integer.parseInt(minStr)
            catch {
                case _: NumberFormatException ⇒ throw compilerError(s"Invalid min quantifier: $minStr")
            }
            try
                max = java.lang.Integer.parseInt(maxStr)
            catch {
                case _: NumberFormatException ⇒ throw compilerError(s"Invalid max quantifier: $maxStr")
            }
            
            if (min < 0 || max < 0 || min > max || max == 0)
                throw compilerError(s"[$min,$max] quantifiers should satisfy 'max >= min, min >= 0, max > 0'.")
        }
    
        /**
         *
         * @return
         */
        def getExpandedMacro: Set[String] = {
            require(expandedSyns != null)
    
            expandedSyns
        }
    }

    /**
     * Custom error handler.
     */
    class CompilerErrorListener(in: String) extends BaseErrorListener {
        /**
         *
         * @param recognizer
         * @param offendingSymbol
         * @param line
         * @param charPos
         * @param msg
         * @param e
         */
        override def syntaxError(
            recognizer: Recognizer[_, _],
            offendingSymbol: scala.Any,
            line: Int,
            charPos: Int,
            msg: String,
            e: RecognitionException): Unit =  throw new NCE(mkCompilerError(msg, line, charPos, in))
    }
    
    /**
      *
      * @param line
      * @param charPos
      * @param in
      * @param msg
      */
    private def mkCompilerError(
        msg: String,
        line: Int,
        charPos: Int,
        in: String
    ): String = {
        val dash = "-" * in.length
        val pos = Math.max(1, charPos)
        val charPosPtr = dash.substring(0, pos - 1) + r("^") + dash.substring(pos)
    
        s"Macro compiler error at line $line:$charPos - $msg\n" +
        s"  |-- ${c("Macro:")} $in\n" +
        s"  +-- ${c("Error:")} $charPosPtr"
    }
    
    /**
     *
     * @param in Macro to expand.
     * @return Expanded macro as a set of finite strings.
     */
    def compile(in: String): Set[String] = {
        // ANTLR4 armature.
        val lexer = new NCMacroDslLexer(CharStreams.fromString(in))
        val stream = new CommonTokenStream(lexer)
        val parser = new NCMacroDslParser(stream)

        // Set custom error handlers.
        lexer.removeErrorListeners()
        parser.removeErrorListeners()
        lexer.addErrorListener(new CompilerErrorListener(in))
        parser.addErrorListener(new CompilerErrorListener(in))

        // State automata.
        val fsm = new FiniteStateMachine(parser, in)

        // Parse the input DSL and walk built AST.
        (new ParseTreeWalker).walk(fsm, parser.makro())

        // Return the expanded macro.
        fsm.getExpandedMacro
    }
}