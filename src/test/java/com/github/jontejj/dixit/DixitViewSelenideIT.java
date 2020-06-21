/* Copyright 2020 jonatanjonsson
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.github.jontejj.dixit;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

import java.util.logging.Level;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.logging.LogType;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideConfig;
import com.codeborne.selenide.SelenideDriver;
import com.codeborne.selenide.WebDriverRunner;

//@SpringBootTest
//@ExtendWith(SpringExtension.class)
public class DixitViewSelenideIT
{
	@Test
	@Disabled
	public void googleShowsWiktionaryLinkForCheesy()
	{
		open("https://google.com/ncr");
		$(By.name("q")).setValue("cheesy").submit();
		$("div.g>div.rc>div.r>a[href=\"https://en.wiktionary.org/wiki/cheesy\"]>h3").shouldHave(exactText("cheesy - Wiktionary"));
		for(String logEntry : Selenide.getWebDriverLogs(LogType.BROWSER, Level.INFO))
		{
			System.out.println("Browser log: " + logEntry);
		}
	}

	@Test
	@Disabled("until graceful shutdowns can be done")
	public void performanceTestDixit() throws InterruptedException
	{
		// try
		// {
		open("http://localhost/dixit");
		$("#" + CssId.DESIRED_AMOUNT_OF_PLAYERS).sendKeys(Keys.BACK_SPACE + "3" + Keys.ENTER);
		$("#" + CssId.PLAYER_NAME).sendKeys("Dog" + Keys.ENTER);
		String urlToJoin = WebDriverRunner.url();

		// Other players join
		SelenideDriver browser1 = new SelenideDriver(new SelenideConfig());// .timeout(20000));
		SelenideDriver browser2 = new SelenideDriver(new SelenideConfig());// .timeout(20000));

		browser1.open(urlToJoin);
		browser2.open(urlToJoin);

		browser1.$("#" + CssId.PLAYER_NAME).sendKeys("Cat" + Keys.ENTER);
		browser2.$("#" + CssId.PLAYER_NAME).sendKeys("Horse" + Keys.ENTER);
		// browser2.browser().

		// $("div.g>div.rc>div.r>a[href=\"https://en.wiktionary.org/wiki/cheesy\"]>h3").shouldHave(exactText("cheesy - Wiktionary"));
		for(String logEntry : Selenide.getWebDriverLogs(LogType.BROWSER, Level.INFO))
		{
			System.out.println("Browser log: " + logEntry);
		}

		// Thread.sleep(20000);
		// }
		// catch(Exception e)
		// {
		// String methodName = getClass().getEnclosingMethod().getName();
		// try
		// {
		// Selenide.screenshot(methodName);
		// }
		// catch(Exception failedScreenshot)
		// {
		// System.err.println("Failed to take screenshot for " + methodName);
		// failedScreenshot.printStackTrace();
		// }
		// throw e;
		// }
	}

	// @Test
	// @Disabled
	// public void testLocalChrome(ChromeDriver driver) throws IOException
	// {
	// // ChromeOptions opt = new ChromeOptions();
	// // opt.setExperimentalOption("w3c", true);
	// // use local Chrome in this test
	// driver.get("https://google.com/ncr");
	// driver.findElementByName("q").sendKeys("cheese" + Keys.ENTER);
	//
	// System.out.println(driver.manage().ime().getAvailableEngines());
	//
	// driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
	//
	// File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(org.openqa.selenium.OutputType.FILE);
	// Files.copy(screenshotFile, new File("google.jpg"));
	// Logs logs = driver.manage().logs();
	// for(String logType : logs.getAvailableLogTypes())
	// {
	// LogEntries logEntries = logs.get(logType);
	// List<LogEntry> logEntriesList = logEntries.getAll().stream().filter(l -> l.getLevel().intValue() > Level.INFO.intValue())
	// .collect(Collectors.toList());
	// logEntriesList.stream().forEach(logEntry -> {
	// System.out.println("From browser logs (" + logType + "): " + logEntry.toString());
	// });
	//
	// }
	// }

	// @Test
	// public void testLocalFirefox(FirefoxDriver driver)
	// {
	// // use local Firefox in this test
	// }

}
