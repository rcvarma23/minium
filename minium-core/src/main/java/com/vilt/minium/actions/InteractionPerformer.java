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
package com.vilt.minium.actions;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Keys;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.vilt.minium.Duration;
import com.vilt.minium.TimeoutException;
import com.vilt.minium.WebElements;
import com.vilt.minium.impl.actions.ButtonReleaseInteraction;
import com.vilt.minium.impl.actions.ClearInteraction;
import com.vilt.minium.impl.actions.ClickAllInteraction;
import com.vilt.minium.impl.actions.ClickAndHoldInteraction;
import com.vilt.minium.impl.actions.ClickInteraction;
import com.vilt.minium.impl.actions.CloseInteraction;
import com.vilt.minium.impl.actions.ContextClickInteraction;
import com.vilt.minium.impl.actions.DeselectAllInteraction;
import com.vilt.minium.impl.actions.DeselectInteraction;
import com.vilt.minium.impl.actions.DeselectValInteraction;
import com.vilt.minium.impl.actions.DoubleClickInteraction;
import com.vilt.minium.impl.actions.DragAndDropByInteraction;
import com.vilt.minium.impl.actions.DragAndDropInteraction;
import com.vilt.minium.impl.actions.FillInteraction;
import com.vilt.minium.impl.actions.GetInteraction;
import com.vilt.minium.impl.actions.KeyDownInteraction;
import com.vilt.minium.impl.actions.KeyUpInteraction;
import com.vilt.minium.impl.actions.MoveByOffsetInteraction;
import com.vilt.minium.impl.actions.MoveMouseInteraction;
import com.vilt.minium.impl.actions.SelectAllInteraction;
import com.vilt.minium.impl.actions.SelectInteraction;
import com.vilt.minium.impl.actions.SelectValInteraction;
import com.vilt.minium.impl.actions.SendKeysInteraction;
import com.vilt.minium.impl.actions.TypeInteraction;
import com.vilt.minium.impl.actions.WaitOrTimeoutForElementsInteraction;
import com.vilt.minium.impl.actions.WaitOrTimeoutWhileElementsInteraction;
import com.vilt.minium.impl.actions.WaitTimeInteraction;
import com.vilt.minium.impl.actions.WaitWhileEmptyInteraction;
import com.vilt.minium.impl.actions.WaitWhileNotEmptyInteraction;
import com.vilt.minium.impl.actions.WaitWindowClosedElementsInteraction;

/**
 * The Class InteractionPerformer.
 */
public class InteractionPerformer {
	
	private List<InteractionListener> listeners = Lists.newArrayList();

	/**
	 * Instantiates a new interaction performer.
	 *
	 * @param listeners the listeners
	 */
	public InteractionPerformer(InteractionListener ... listeners) {
		with(listeners);
	}
	
	/**
	 * With.
	 *
	 * @param listeners the listeners
	 * @return the interaction performer
	 */
	public InteractionPerformer with(InteractionListener ... listeners) {
		if (listeners != null) {
			this.listeners.addAll(Arrays.asList(listeners));
		}
		return this;
	}

	/**
	 * Perform.
	 *
	 * @param interaction the interaction
	 */
	public void perform(Interaction interaction) {
		for (InteractionListener listener : listeners) {
			interaction.registerListener(listener);
		}
		interaction.perform();
	}
	
	public void get(WebElements elements, String url) {
		perform(new GetInteraction(elements, url));
	}
	
	// from org.openqa.selenium.WebElement
	/**
	 * Clear.
	 *
	 * @param elements the elements
	 */
	public void clear(WebElements elements) {
		perform(new ClearInteraction(elements));
	}
	
	// from org.openqa.selenium.interactions.Actions
	/**
	 * Key down.
	 *
	 * @param elements the elements
	 * @param keys the keys
	 */
	public void keyDown(WebElements elements, Keys keys) {
		perform(new KeyDownInteraction(elements, keys));
	}
	
	/**
	 * Key up.
	 *
	 * @param elements the elements
	 * @param keys the keys
	 */
	public void keyUp(WebElements elements, Keys keys) {
		perform(new KeyUpInteraction(elements, keys));
	}
	
	/**
	 * Send keys.
	 *
	 * @param elements the elements
	 * @param keys the keys
	 */
	public void sendKeys(WebElements elements, CharSequence ... keys) {
		perform(new SendKeysInteraction(elements, keys));
	}
	
	/**
	 * Click and hold.
	 *
	 * @param elements the elements
	 */
	public void clickAndHold(WebElements elements) {
		perform(new ClickAndHoldInteraction(elements));
	}
	
	/**
	 * Release.
	 *
	 * @param elements the elements
	 */
	public void release(WebElements elements) {
		perform(new ButtonReleaseInteraction(elements));
	}
	
	/**
	 * Click.
	 *
	 * @param elements the elements
	 */
	public void click(WebElements elements) {
		perform(new ClickInteraction(elements));		
	}
	
	/**
	 * Double click.
	 *
	 * @param elements the elements
	 */
	public void doubleClick(WebElements elements) {
		perform(new DoubleClickInteraction(elements));		
	}
	
	/**
	 * Move to element.
	 *
	 * @param elements the elements
	 */
	public void moveToElement(WebElements elements) {
		perform(new MoveMouseInteraction(elements, 0, 0));
	}
	
	/**
	 * Move to element.
	 *
	 * @param elements the elements
	 * @param xOffset the x offset
	 * @param yOffset the y offset
	 */
	public void moveToElement(WebElements elements, int xOffset, int yOffset) {
		perform(new MoveMouseInteraction(elements, xOffset, yOffset));
	}
	
	/**
	 * Move by offset.
	 *
	 * @param elements the elements
	 * @param xOffset the x offset
	 * @param yOffset the y offset
	 */
	public void moveByOffset(WebElements elements, int xOffset, int yOffset) {
		perform(new MoveByOffsetInteraction(elements, xOffset, yOffset));		
	}
	
	/**
	 * Context click.
	 *
	 * @param elements the elements
	 */
	public void contextClick(WebElements elements) {
		perform(new ContextClickInteraction(elements));
	}
	
	/**
	 * Drag and drop.
	 *
	 * @param source the source
	 * @param target the target
	 */
	public void dragAndDrop(WebElements source, WebElements target) {
		perform(new DragAndDropInteraction(source, target));
	}
	
	/**
	 * Drag and drop by.
	 *
	 * @param source the source
	 * @param xOffset the x offset
	 * @param yOffset the y offset
	 */
	public void dragAndDropBy(WebElements source, int xOffset, int yOffset) {
		perform(new DragAndDropByInteraction(source, xOffset, yOffset));
	}
	
	// additional methods
	/**
	 * Click all.
	 *
	 * @param elements the elements
	 */
	public void clickAll(WebElements elements) {
		perform(new ClickAllInteraction(elements));
	}
	
	/**
	 * Type.
	 *
	 * @param elements the elements
	 * @param text the text
	 */
	public void type(WebElements elements, CharSequence text) {
		perform(new TypeInteraction(elements, text));
	}
		
	/**
	 * Clear and type.
	 *
	 * @param elements the elements
	 * @param text the text
	 */
	public void fill(WebElements elements, CharSequence text) {
		perform(new FillInteraction(elements, text));
	}
	
	// select
	/**
	 * Select.
	 *
	 * @param elems the elems
	 * @param text the text
	 */
	public void select(WebElements elems, String text) {
		perform(new SelectInteraction(elems, text));
	}
	
	/**
	 * Deselect.
	 *
	 * @param elems the elems
	 * @param text the text
	 */
	public void deselect(WebElements elems, String text) {
		perform(new DeselectInteraction(elems, text));		
	}
	
	/**
	 * Select val.
	 *
	 * @param elems the elems
	 * @param val the val
	 */
	public void selectVal(WebElements elems, String val) {
		perform(new SelectValInteraction(elems, val));		
	}
	
	/**
	 * Deselect val.
	 *
	 * @param elems the elems
	 * @param val the val
	 */
	public void deselectVal(WebElements elems, String val) {
		perform(new DeselectValInteraction(elems, val));				
	}
	
	/**
	 * Select all.
	 *
	 * @param elems the elems
	 */
	public void selectAll(WebElements elems) {
		perform(new SelectAllInteraction(elems));						
	}
	
	/**
	 * Deselect all.
	 *
	 * @param elems the elems
	 */
	public void deselectAll(WebElements elems) {
		perform(new DeselectAllInteraction(elems));						
	}
	
	/**
	 * Wait for elements.
	 *
	 * @param elems the elems
	 * @deprecated use {@link InteractionPerformer#waitWhileEmpty(WebElements)} instead
	 */
	@Deprecated
	public void waitForElements(WebElements elems) throws TimeoutException {
		waitWhileEmpty(elems);
	}
	
	public void waitWhileEmpty(WebElements elems) throws TimeoutException {
		perform(new WaitWhileEmptyInteraction(elems));
	}
	
	/**
	 * Wait while elements.
	 *
	 * @param elems the elems
	 * @deprecated use {@link InteractionPerformer#waitWhileNotEmpty(WebElements)} instead
	 */
	@Deprecated
	public void waitWhileElements(WebElements elems) throws TimeoutException {
		waitWhileNotEmpty(elems);
	}

	public void waitWhileNotEmpty(WebElements elems) throws TimeoutException {
		perform(new WaitWhileNotEmptyInteraction(elems));
	}
	
	/**
	 * Check not empty.
	 *
	 * @param elems the elems
	 * @return true, if successful
	 */
	public boolean checkNotEmpty(WebElements elems) {
		perform(new WaitOrTimeoutForElementsInteraction(elems));
		return !Iterables.isEmpty(elems);
	}
	
	/**
	 * Check empty.
	 *
	 * @param elems the elems
	 * @return true, if successful
	 */
	public boolean checkEmpty(WebElements elems) {
		perform(new WaitOrTimeoutWhileElementsInteraction(elems));
		return Iterables.isEmpty(elems);
	}
	
	/**
	 * Wait until closed.
	 *
	 * @param elems the elems
	 */
	public void waitUntilClosed(WebElements elems) throws TimeoutException {
		perform(new WaitWindowClosedElementsInteraction(elems));
	}

	/**
	 * Wait time.
	 *
	 * @param time the time
	 * @param unit the unit
	 */
	public void waitTime(long time, TimeUnit unit) {
		perform(new WaitTimeInteraction(new Duration(time, unit)));
	}

	public void close(WebElements elements) {
		perform(new CloseInteraction(elements));
	}
}
