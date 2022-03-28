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

package org.apache.nlpcraft.nlp.util;

import org.apache.nlpcraft.NCModelConfig;
import org.apache.nlpcraft.internal.util.NCResourceReader;
import org.apache.nlpcraft.nlp.token.parser.NCOpenNLPTokenParser;
import org.apache.nlpcraft.nlp.token.enricher.*;

/**
 *
 */
public class NCTestConfigJava {
    public static final NCModelConfig CFG = new NCModelConfig("testId", "test", "1.0", "Test description", "Test origin");
    public static final NCOpenNLPTokenParser EN_TOK_PARSER = new NCOpenNLPTokenParser(NCResourceReader.getPath("opennlp/en-token.bin"));
    public static final NCEnStopWordsTokenEnricher EN_TOK_STOP_ENRICHER = new NCEnStopWordsTokenEnricher();
    public static final NCOpenNLPLemmaPosTokenEnricher EN_TOK_LEMMA_POS_ENRICHER = new NCOpenNLPLemmaPosTokenEnricher(
        NCResourceReader.getPath("opennlp/en-pos-maxent.bin"),
        NCResourceReader.getPath("opennlp/en-lemmatizer.dict")
    );
    public static final NCTestPipeline mkEnPipeline() {
        return new NCTestPipeline(EN_TOK_PARSER);
    }
}