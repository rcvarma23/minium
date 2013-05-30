/*
 * Copyright (C) 2013 VILT Group
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
package com.vilt.minium.impl;

import static com.google.common.collect.FluentIterable.from;
import static java.lang.String.format;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Alert;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.vilt.minium.Async;
import com.vilt.minium.Configuration;
import com.vilt.minium.CoreWebElements;
import com.vilt.minium.Duration;
import com.vilt.minium.MiniumException;
import com.vilt.minium.TargetLocatorWebElements;
import com.vilt.minium.TimeoutException;
import com.vilt.minium.WaitWebElements;
import com.vilt.minium.WebElements;
import com.vilt.minium.WebElementsDriver;
import com.vilt.minium.WebElementsDriverProvider;
import com.vilt.minium.impl.utils.Casts;

public abstract class BaseWebElementsImpl<T extends WebElements> implements WebElements, TargetLocatorWebElements<T>, WaitWebElements<T>, WebElementsDriverProvider<T> {

	final Logger logger = LoggerFactory.getLogger(WebElements.class);
	
	private static class MiniumWait<T> extends FluentWait<T> {

		public MiniumWait(T input, Duration timeout, Duration interval) {
			super(input);
			withTimeout(timeout.getTime(), timeout.getUnit());
			pollingEvery(interval.getTime(), interval.getUnit());
		}
		
		@Override
		protected RuntimeException timeoutException(String message, Throwable lasteException) {
			return new TimeoutException(message, lasteException);
		}
	}
	
	
	private final Function<Object, String> argToStringFunction = new Function<Object, String>() {
		public String apply(Object input) {
			if (input == null) return "null";
			if (input instanceof String) return format("'%s'", StringEscapeUtils.escapeEcmaScript((String) input));
			if (input instanceof Boolean) return input.toString();
			if (input instanceof Number) return input.toString();
			if (input instanceof Date) return format("new Date(%d)", ((Date) input).getTime());
			if (input instanceof BaseWebElementsImpl) return getJQueryWebElementsExpression(input);
			
			return format("'%s'", StringEscapeUtils.escapeEcmaScript(input.toString()));
		}

		@SuppressWarnings("unchecked")
		private String getJQueryWebElementsExpression(Object input) {
			BaseWebElementsImpl<T> elem = (BaseWebElementsImpl<T>) input;

			if (!elem.documentRootWebElements().equals(BaseWebElementsImpl.this.documentRootWebElements())) {
				throw new IllegalArgumentException("WebElements does not belong to the same window / iframe");
			}
			
			return elem.getExpression();
		}
	};
	
	protected WebElementsFactory factory;

	protected abstract String getExpression();
	
	protected abstract Iterable<WebElement> computeElements(final WebElementsDriver<T> wd);
		
	protected abstract Iterable<WebElementsDriver<T>> candidateWebDrivers();

	protected abstract WebElementsDriver<T> rootWebDriver();
	
	protected abstract T documentRootWebElements();
	
	public void init(WebElementsFactory factory) {
		this.factory = factory;
	}
	
	@SuppressWarnings("unchecked")
	public Object invoke(Method method, Object ... args) {
		if (method.isVarArgs()) {
			args = expandVarArgs(args);
		}
		String expression = computeExpression(this, isAsyncMethod(method), method.getName(), args);
		
		if (method.getReturnType() != Object.class && method.getReturnType().isAssignableFrom(this.getClass())) {
			T webElements = (T) WebElementsFactoryHelper.createExpressionWebElements(factory, this, expression);
			return webElements;
		}
		else {
			Object result = null;
			
			boolean async = isAsyncMethod(method);
			
			Iterable<WebElementsDriver<T>> webDrivers = candidateWebDrivers();		
			
			if (method.getReturnType() == Void.TYPE) {
				for (WebElementsDriver<T> wd : webDrivers) {
					factory.getInvoker().invokeExpression(wd, async, expression);
				}
			}
			else {
				if (Iterables.size(webDrivers) == 1) {
					WebElementsDriver<T> wd = Iterables.get(webDrivers, 0);
					result = factory.getInvoker().invokeExpression(wd, async, expression);
				}
				else {
					String sizeExpression = computeExpression(this, false, "size");
					WebElementsDriver<T> webDriverWithResults = null;
					
					for (WebElementsDriver<T> wd : webDrivers) {
						long size = (Long) factory.getInvoker().invokeExpression(wd, async, sizeExpression);
						if (size > 0) {
							if (webDriverWithResults == null) {
								webDriverWithResults = wd;
							}
							else {
								throw new MiniumException("Several frames or windows match the same expression, so value cannot be computed");								
							}
						}
					}
					
					if (webDriverWithResults != null) {
						result = factory.getInvoker().invokeExpression(webDriverWithResults, async, expression);
					}
				}
			}

			if (logger.isDebugEnabled()) {
				String val;
				if (method.getReturnType() == Void.TYPE) {
					val = "void";
				} else {
					val = StringUtils.abbreviate(argToStringFunction.apply(result), 40);
					if (val.startsWith("'") && !val.endsWith("'")) val += "(...)'";
				}
				logger.debug("[Value: {}] {}", argToStringFunction.apply(result), expression);
			}
			
			// let's handle numbers when return type is int
			if (method.getReturnType() == Integer.TYPE) {
				return result == null ? 0 : ((Number) result).intValue();
			}
			else {
				return result;
			}
		}
	}
	
	private Object[] expandVarArgs(Object[] args) {
		if (args == null || args.length == 0) return args;
		Object[] lastArg = (Object[]) args[args.length - 1];
		int size = lastArg == null ? 0 : lastArg.length;
		Object[] expandedArgs = new Object[args.length + size - 1];
		System.arraycopy(args, 0, expandedArgs, 0, args.length - 1);
		if (size > 0) {
			System.arraycopy(lastArg, 0, expandedArgs, args.length - 1, lastArg.length);
		}
		return expandedArgs;
	}

	private boolean isAsyncMethod(Method method) {
		return method.getAnnotation(Async.class) != null;
	}

	protected final Iterable<WebElement> computeElements() {
		return Iterables.concat(Iterables.transform(candidateWebDrivers(), new Function<WebElementsDriver<T>, Iterable<WebElement>>() {

			@Override
			@Nullable
			public Iterable<WebElement> apply(@Nullable final WebElementsDriver<T> wd) {
				return Iterables.transform(computeElements(wd), new Function<WebElement, WebElement>() {

					@Override
					@Nullable
					public WebElement apply(@Nullable WebElement input) {
						return new DelegateWebElement(input, wd);
					}
				});
			}
		}));
	}
	
	protected String computeExpression(BaseWebElementsImpl<T> parent, boolean async, String fnName, Object ... args) {
		List<String> jsArgs = 
				from(Arrays.asList(args)).
				transform(argToStringFunction).
				toList();
		
		if (async) {
			jsArgs = Lists.newArrayList(jsArgs);
			jsArgs.add("callback");
		}
		
		if (parent instanceof DocumentRootWebElementsImpl<?> && "find".equals(fnName)) {
			return format("$(%s)", StringUtils.join(jsArgs, ", "));		
		}
		else {
			return format("%s.%s(%s)", parent.getExpression(), fnName, StringUtils.join(jsArgs, ", "));
		}
	}

	@Override
	public final Iterator<WebElement> iterator() {
		Iterable<WebElement> elements = computeElements();
		if (logger.isDebugEnabled()) {
			logger.debug("[WebElements size: {}] {}", Iterables.size(elements), this);
		}
		
		return elements.iterator();
	}

	@Override
	public WebElement get(int index) {
		return Iterables.get(this, index);
	}
	
	@Override
	public Iterable<WebElementsDriver<T>> webDrivers() {
		return candidateWebDrivers();
	}
	
	@Override
	public WebElementsDriver<T> webDriver() {
		Iterable<WebElementsDriver<T>> webDrivers = webDrivers();
		if (Iterables.size(webDrivers) > 1) {
			throw new IllegalStateException("This web elements must only evaluate to one web driver");
		}
		else if (Iterables.isEmpty(webDrivers)) {
			throw new IllegalStateException("This web elements must evaluate to one web driver");
		}
		
		return Iterables.get(webDrivers, 0);
	}

	@Override
	public T wait(Predicate<? super T> predicate) {		
		return this.wait(null, predicate);
	}

	@Override
	public T wait(long time, TimeUnit unit, Predicate<? super T> predicate) {
		return this.wait(new Duration(time, unit), predicate);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T wait(Duration timeout, Predicate<? super T> predicate) {
		if (timeout == null) {
			timeout = rootWebDriver().configuration().getDefaultTimeout();
		}
		
		Wait<T> wait = getWait(timeout);
		
		Function<? super T, Boolean> function = Functions.forPredicate(predicate);
		wait.until(function);
		
		return (T) this;
	}

	@Override
	public T waitOrTimeout(Predicate<? super T> predicate) {
		return waitOrTimeout(null, predicate);
	}

	@Override
	public T waitOrTimeout(long time, TimeUnit unit, Predicate<? super T> predicate) {
		return waitOrTimeout(new Duration(time, unit), predicate);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T waitOrTimeout(Duration timeout, Predicate<? super T> predicate) {
		if (timeout == null) {
			timeout = rootWebDriver().configuration().getDefaultTimeout();
		}
		
		Wait<T> wait = getWait(timeout);
		
		Function<? super T, Boolean> function = Functions.forPredicate(predicate);
		
		try {
			wait.until(function);
		}
		catch(TimeoutException e) {
			// ignore
		}
		
		return (T) this;
	}

	@Override
	public T frame() {
		CoreWebElements<?> filtered = ((CoreWebElements<?>) this).find("iframe, frame").addBack().filter("iframe, frame");
		return Casts.<T>cast(WebElementsFactoryHelper.createIFrameWebElements(factory, filtered));
	}

	@Override
	public T frame(String selector) {
		CoreWebElements<?> filtered = ((CoreWebElements<?>) this).find(selector).filter("iframe, frame");
		return Casts.<T>cast(WebElementsFactoryHelper.createIFrameWebElements(factory, filtered));
	}

	@Override
	public T frame(T filter) {
		CoreWebElements<?> filtered = ((CoreWebElements<?>) this).filter("iframe, frame");
		return Casts.<T>cast(WebElementsFactoryHelper.createIFrameWebElements(factory, filtered));
	}

	public T frame(T filter, boolean freeze) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public T window(String expr) {
		return window(expr, false);
	}

	@Override
	public T window(String expr, boolean freeze) {
		return Casts.<T>cast(WebElementsFactoryHelper.createWindowWebElements(factory, this, expr, freeze));
	}

	@Override
	public T window(T filter) {
		return window(filter, false);
	}

	@Override
	public T window(T filter, boolean freeze) {
		return Casts.<T>cast(WebElementsFactoryHelper.createWindowWebElements(factory, this, filter, freeze));
	}
	
	@Override
	public T window() {
		return Casts.<T>cast(WebElementsFactoryHelper.createWindowWebElements(factory, this));
	}
	
	@Override
	public T window(boolean newWindow) {
		return Casts.<T>cast(WebElementsFactoryHelper.createWindowWebElements(factory, this, (T) null, newWindow));
	}

	
	@Override
	public T root() {
		return root(false);
	}
	
	@Override
	public T root(boolean freeze) {
		return root(Casts.<T>cast(this), freeze);
	}
	
	public abstract T root(T filter, boolean freeze);
	
	@Override
	public Alert alert() {
		Duration timeout = rootWebDriver().configuration().getDefaultTimeout();
	
		FluentWait<T> wait = getWait(timeout);
		
		return wait.ignoring(NoAlertPresentException.class).until(new Function<T, Alert>() {

			@Override
			@Nullable
			public Alert apply(@Nullable T input) {
				return rootWebDriver().getWrappedWebDriver().switchTo().alert();
			}
		});
	}
	
	protected MiniumWait<T> getWait(Duration timeout) {
		Duration interval = rootWebDriver().configuration().getDefaultInterval();
		return new MiniumWait<T>(Casts.<T>cast(this), timeout, interval);
	}
	
	@Override
	public WebDriver nativeWebDriver() {
		return rootWebDriver().getWrappedWebDriver();
	}
	
	@Override
	public Configuration configuration() {
		return rootWebDriver().configuration();
	}
	
	@Override
	public String toString() {
		return getExpression();
	}
}