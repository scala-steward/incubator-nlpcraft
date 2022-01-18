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

package org.apache.nlpcraft;

import org.apache.nlpcraft.internal.impl.NCModelClientImpl;

import java.util.Map;
import java.util.concurrent.*;

/**
 *
 */
public class NCModelClient {
    private final NCModelClientImpl impl;

    /**
     *
     * @param mdl
     */
    public NCModelClient(NCModel mdl) {
        this.impl = new NCModelClientImpl(mdl);
    }

    /**
     *
     * @param txt
     * @param data
     * @param usrId
     * @return
     * @throws NCException
     */
    public CompletableFuture<NCResult> ask(String txt, Map<String, Object> data, String usrId) {
        return impl.ask(txt, data, usrId);
    }

    /**
     *
     * @param txt
     * @param data
     * @param usrId
     * @return
     * @throws NCException
     */
    public NCResult askSync(String txt, Map<String, Object> data, String usrId) {
        return impl.askSync(txt, data, usrId);
    }

    /**
     *
     * @param usrId
     * @throws NCException
     */
    public void clearConversation(String usrId) {
        impl.clearConversation(usrId);
    }

    /**
     *
     * @param usrId
     * @throws NCException
     */
    public void clearDialog(String usrId) {
        impl.clearDialog(usrId);
    }

    /**
     * 
     */
    public void close() {
        impl.close();
    }
}