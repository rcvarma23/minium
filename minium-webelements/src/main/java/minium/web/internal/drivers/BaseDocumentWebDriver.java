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
package minium.web.internal.drivers;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.HasInputDevices;
import org.openqa.selenium.interactions.Keyboard;
import org.openqa.selenium.interactions.Mouse;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import minium.web.DocumentWebDriver;

public abstract class BaseDocumentWebDriver implements InternalDocumentWebDriver {

    protected final WebDriver webDriver;

    public BaseDocumentWebDriver(WebDriver webDriver) {
        Preconditions.checkArgument(webDriver != null && !(webDriver instanceof DocumentWebDriver));
        this.webDriver = webDriver;
    }

    @Override
    public void get(String url) {
        ensureSwitch();
        webDriver.get(url);
    }

    @Override
    public String getCurrentUrl() {
        ensureSwitch();
        return webDriver.getCurrentUrl();
    }

    public String getPerformance() {
        ensureSwitch();

        final ObjectMapper mapper = new ObjectMapper();
        Map<?, ?> performance = (Map<?, ?>) ((JavascriptExecutor) webDriver).executeScript("return window.performance");
        String performanceJson = null;

        List<LogEntry> jsErrors = webDriver.manage().logs().get(LogType.BROWSER).filter(Level.SEVERE);
        String jsErrorsJson = null;

        Map<?, ?> stats = (Map<?, ?>) ((JavascriptExecutor) webDriver).executeScript(
                "var numberOfRequests = 0;var pageSize = 0; performance.getEntriesByType('resource').forEach((r) => { numberOfRequests++; pageSize += r.transferSize }); return {pageSize, numberOfRequests}");
        String statsJson = null;

        try {
            performanceJson = mapper.writeValueAsString(performance);
            jsErrorsJson = mapper.writeValueAsString(jsErrors);
            statsJson = mapper.writeValueAsString(stats);
        } catch (JsonProcessingException e) {
        }

        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse response;
        int statusCode = -1;
        try {
            response = client.execute(new HttpGet(getCurrentUrl()));
            statusCode = response.getStatusLine().getStatusCode();
        } catch (IOException e) {
        }

        String output = "{ \"url\": \"" + getCurrentUrl() + "\", \"data\": " + performanceJson + ", \"stats\": " + statsJson + ", \"statusCode\": " + statusCode
                + ", \"jsErrors\": " + jsErrorsJson + " }";

        return output;
    }

    @Override
    public String getTitle() {
        ensureSwitch();
        return webDriver.getTitle();
    }

    @Override
    public List<WebElement> findElements(By by) {
        ensureSwitch();
        return webDriver.findElements(by);
    }

    @Override
    public WebElement findElement(By by) {
        ensureSwitch();
        return webDriver.findElement(by);
    }

    @Override
    public String getPageSource() {
        ensureSwitch();
        return webDriver.getPageSource();
    }

    @Override
    public void close() {
        ensureSwitch();
        webDriver.close();
    }

    @Override
    public void quit() {
        webDriver.quit();
    }

    @Override
    public Set<String> getWindowHandles() {
        ensureSwitch();
        return webDriver.getWindowHandles();
    }

    @Override
    public String getWindowHandle() {
        ensureSwitch();
        return webDriver.getWindowHandle();
    }

    @Override
    public TargetLocator switchTo() {
        ensureSwitch();
        return webDriver.switchTo();
    }

    @Override
    public Navigation navigate() {
        ensureSwitch();
        return webDriver.navigate();
    }

    @Override
    public Options manage() {
        ensureSwitch();
        return webDriver.manage();
    }

    @Override
    public Object executeScript(String script, Object... args) {
        ensureSwitch();
        return ((JavascriptExecutor) webDriver).executeScript(script, args);
    }

    @Override
    public Object executeAsyncScript(String script, Object... args) {
        ensureSwitch();
        return ((JavascriptExecutor) webDriver).executeAsyncScript(script, args);
    }

    @Override
    public Keyboard getKeyboard() {
        return ((HasInputDevices) webDriver).getKeyboard();
    }

    @Override
    public Mouse getMouse() {
        return ((HasInputDevices) webDriver).getMouse();
    }

    @Override
    public <X> X getScreenshotAs(OutputType<X> target) throws WebDriverException {
        Preconditions.checkState(webDriver instanceof TakesScreenshot);
        return ((TakesScreenshot) webDriver).getScreenshotAs(target);
    }

    @Override
    public WebDriver nativeWebDriver() {
        return webDriver;
    }

    @Override
    public int hashCode() {
        return webDriver.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == BaseDocumentWebDriver.class && equalFields((BaseDocumentWebDriver) obj);
    }

    protected boolean equalFields(BaseDocumentWebDriver obj) {
        return Objects.equal(webDriver, obj.webDriver);
    }
}