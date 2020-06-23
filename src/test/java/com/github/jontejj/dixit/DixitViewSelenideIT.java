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
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.logging.LogType;

import com.codeborne.selenide.Driver;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideConfig;
import com.codeborne.selenide.SelenideDriver;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

//@SpringBootTest
//@ExtendWith(SpringExtension.class)
public class DixitViewSelenideIT
{
	private static final Random random = new Random(1);

	@Test
	@Disabled("Just an example of a test")
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

	static final int nrOfPlayers = 3;

	// TODO: take a screenshot for failures
	@Test
	public void performanceTestDixit() throws InterruptedException, ExecutionException, TimeoutException
	{
		String urlToJoin = createGame();

		ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(nrOfPlayers));
		List<ListenableFuture<?>> futures = new ArrayList<>();
		// Other players join
		for(int i = 0; i < nrOfPlayers; i++)
		{
			futures.add(executor.submit(automaticallyPlayingclient(urlToJoin, randomPlayerName())));
		}

		ListenableFuture<List<Object>> allFuturesList = Futures.allAsList(futures);
		// A game between nrOfPlayers should finish within the allowed timeout
		allFuturesList.get(120, TimeUnit.SECONDS);

		List<String> severeLogEntries = new ArrayList<>();
		for(String logEntry : Selenide.getWebDriverLogs(LogType.BROWSER, Level.SEVERE))
		{
			severeLogEntries.add(logEntry);
		}
		assertThat(severeLogEntries).isEmpty();

	}

	private static String randomPlayerName()
	{
		String newPlayerName = prefixes.get(random.nextInt(prefixes.size() + 1)) + random.nextInt(10000);
		if(!alreadyCreatedNames.add(newPlayerName))
		{
			System.out.println("Duplicate player name generated: " + newPlayerName + ". Generating a new one.");
			return randomPlayerName();
		}
		return newPlayerName;
	}

	private static final Set<String> alreadyCreatedNames = new HashSet<>();

	private static final List<String> prefixes = List.of("Dog", "Cat", "Horse");

	private String createGame()
	{
		// TODO: avoid hardcoding this?
		String initialUrl = "http://localhost/dixit";
		open(initialUrl);
		$("#" + CssId.DESIRED_AMOUNT_OF_PLAYERS).sendKeys(Keys.BACK_SPACE + "" + nrOfPlayers + Keys.ENTER);

		waitForVaadin(WebDriverRunner.driver());
		String urlToJoin = WebDriverRunner.url();
		if(initialUrl.equals(urlToJoin))
			throw new AssertionError(urlToJoin + " should contain a game id");
		return urlToJoin;
	}

	private Callable<Void> automaticallyPlayingclient(String urlToGame, String playerName)
	{
		return () -> {
			SelenideDriver clientBrowser = new SelenideDriver(new SelenideConfig());

			clientBrowser.open(urlToGame);

			clientBrowser.$("#" + CssId.PLAYER_NAME).sendKeys(playerName + Keys.ENTER);

			// int statusCounter = Integer.parseInt(statusWebElement.getAttribute(HTLMProperties.STATUS_COUNTER));

			RequestedAction requestedAction = RequestedAction.WAIT;
			while(requestedAction != RequestedAction.GAME_FINISHED_GO_HOME)
			{
				syncWithOtherPlayers(clientBrowser);
				SelenideElement statusSelenideElement = clientBrowser.$("#" + CssId.STATUS);
				WebElement statusWebElement = statusSelenideElement.toWebElement();
				requestedAction = RequestedAction.fromAttribute(statusWebElement.getAttribute(HTLMProperties.REQUESTED_ACTION));
				System.out.println(playerName + " is requested to: " + requestedAction);
				switch(requestedAction)
				{
				case GUESS_WHICH_CARD:
					clickFirstCard(clientBrowser);
					break;
				case MAKE_A_SENTENCE:
					makeSentenceAndPickCard(clientBrowser);
					break;
				case MATCH_CARD_TO_SENTENCE:
					clickFirstCard(clientBrowser);
					break;
				case WAIT:
					break;
				case GAME_FINISHED_GO_HOME:
				default:
					break;
				}

			}
			return null;
		};
	}

	private static final CyclicBarrier barrier = new CyclicBarrier(nrOfPlayers);

	private void syncWithOtherPlayers(SelenideDriver clientBrowser) throws InterruptedException, BrokenBarrierException, TimeoutException
	{
		waitForVaadin(clientBrowser.driver());
		// Wait until all players have finished responded to the last requested action
		barrier.await(5, TimeUnit.SECONDS);
		waitForVaadin(clientBrowser.driver());
	}

	private void makeSentenceAndPickCard(SelenideDriver clientBrowser)
	{
		// TODO: make funnier sentences
		clientBrowser.$("#" + CssId.SENTENCE_PROMPT).sendKeys("Random" + Keys.ENTER);
		clickFirstCard(clientBrowser);
	}

	private void clickFirstCard(SelenideDriver clientBrowser)
	{
		ElementsCollection availableCards = clientBrowser.$("#" + CssId.CARD_AREA).findAll(By.className(CssClassNames.SELECTABLE));
		availableCards.first().click();
	}

	public static void waitForVaadin(Driver driver)
	{

		long timeoutTime = System.currentTimeMillis() + 20000;
		Boolean finished = false;
		while(System.currentTimeMillis() < timeoutTime && !finished)
		{
			// Must use the wrapped driver here to avoid calling waitForVaadin
			// again
			if(!driver.supportsJavascript())
				throw new IllegalStateException("Javascript not supported. Aborting wait");
			String WAIT_FOR_VAADIN_SCRIPT = "if (!window.Vaadin || !window.Vaadin.Flow) {" + "  return true;" + "}"
					+ "var clients = window.Vaadin.Flow.clients;" + "if (clients) {" + "  for (var client in clients) {"
					+ "    if (clients[client].isActive()) {" + "      return false;" + "    }" + "  }" + "  return true;" + "} else {" +
					// A Vaadin connector was found so this is most likely a Vaadin
					// application. Keep waiting.
					"  return false;" + "}";
			finished = (Boolean) driver.executeJavaScript(WAIT_FOR_VAADIN_SCRIPT);
			if(finished == null)
			{
				// This should never happen but according to
				// https://dev.vaadin.com/ticket/19703, it happens
				System.out.println("waitForVaadin returned null, this should never happen");
				finished = false;
			}
		}
	}
}
