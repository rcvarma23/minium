/*
 * Copyright (C) 2015 The Minium Authors
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
package minium.web.internal;

import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.collect.FluentIterable.from;
import static java.lang.String.format;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import minium.web.DocumentRoots;
import minium.web.DocumentWebDriver;
import minium.web.WebElements;
import minium.web.internal.drivers.DocumentWebElement;
import minium.web.internal.expression.Expression;
import minium.web.internal.expression.NativeWebElementsExpression;

import org.openqa.selenium.WebElement;

import platypus.internal.Casts;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class NativeWebElements<T extends WebElements> extends InternalWebElements.Impl<T> implements ExpressionWebElements {

    private final List<DocumentWebElement> nativeWebElements;

    public NativeWebElements(Collection<DocumentWebElement> nativeWebElements) {
        Preconditions.checkArgument(from(nativeWebElements).allMatch(instanceOf(DocumentWebElement.class)));
        this.nativeWebElements = Lists.newArrayList(nativeWebElements);
    }

    @Override
    public Iterable<DocumentWebDriver> candidateDocumentDrivers() {
        Set<DocumentWebDriver> documentDrivers = from(nativeWebElements).transform(WebElementFunctions.unwrapDocumentDriver()).toSet();
        Preconditions.checkState(documentDrivers.size() <= 1);
        return documentDrivers;
    }

    @Override
    public Iterable<DocumentWebDriver> documentDrivers() {
        return candidateDocumentDrivers();
    }

    @Override
    public DocumentRoots documentRoots() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public WebElements candidateDocumentRoots() {
        return documentRoots();
    }

    @Override
    public boolean isDocumentRoots() {
        return false;
    }

    @Override
    public Iterable<WebElement> computeNativeElements(DocumentWebDriver wd) {
        return Casts.unsafeCast(nativeWebElements);
    }

    @Override
    public Expression getExpression() {
        return new NativeWebElementsExpression(nativeWebElements);
    }

    @Override
    public T freeze() {
        return myself();
    }

    @Override
    public String toString() {
        return format("$(<%d native elements>)", nativeWebElements.size());
    }
}
