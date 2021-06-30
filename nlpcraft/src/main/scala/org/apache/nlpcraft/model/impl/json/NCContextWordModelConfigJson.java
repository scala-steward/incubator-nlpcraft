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

package org.apache.nlpcraft.model.impl.json;

import java.util.Map;

/**
 * TODO:
 */
public class NCContextWordModelConfigJson {
    private String[] samples = new String[0];
    private boolean useIntentsSamples;
    private Map<String, NCContextWordElementScoreJson> supportedElements;

    public String[] getSamples() {
        return samples;
    }
    public void setSamples(String[] samples) {
        this.samples = samples;
    }
    public boolean isUseIntentsSamples() {
        return useIntentsSamples;
    }
    public void setUseIntentsSamples(boolean useIntentsSamples) {
        this.useIntentsSamples = useIntentsSamples;
    }
    public Map<String, NCContextWordElementScoreJson> getSupportedElements() {
        return supportedElements;
    }
    public void setSupportedElements(Map<String, NCContextWordElementScoreJson> supportedElements) {
        this.supportedElements = supportedElements;
    }
}
