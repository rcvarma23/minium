/*
 * Copyright (C) 2013 The Minium Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vilt.minium.impl.actions;

import com.vilt.minium.CoreWebElements;
import com.vilt.minium.actions.Interaction;
import com.vilt.minium.actions.InteractionEvent;

/**
 * The Class BeforeInteractionEvent.
 */
public abstract class AfterInteractionEvent extends InteractionEvent {

    private static final long serialVersionUID = 4826758132095640515L;

    public AfterInteractionEvent(CoreWebElements<?> source, Interaction interaction) {
        super(source, interaction);
    }
}
