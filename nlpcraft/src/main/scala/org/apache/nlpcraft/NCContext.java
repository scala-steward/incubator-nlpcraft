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

import java.util.*;

/**
 *
 */
public interface NCContext {
    /**
     * Tests if given entity is part of the query this context is associated with.
     *
     * @param ent Entity to check.
     */
    boolean isOwnerOf(NCEntity ent);

    /**
     * Gets model configuration for this query.
     *
     * @return Model.
     */
    NCModelConfig getModelConfig();

    /**
     * Gets user request.
     *
     * @return User request.
     */
    NCRequest getRequest();

    /**
     * Gets current conversation for this context.
     *
     * @return Current conversation.
     */
    NCConversation getConversation();

    /**
     *
     * @return
     */
    Collection<NCVariant> getVariants();
}
