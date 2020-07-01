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

import javax.ws.rs.core.UriBuilder;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Driver;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideConfig;
import com.codeborne.selenide.SelenideDriver;
import com.codeborne.selenide.SelenideElement;
import com.github.jontejj.dixit.StatusController.Status;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

//@SpringBootTest
//@ExtendWith(SpringExtension.class)
@Testcontainers
public class DixitViewSelenideIT
{
	private final String BASE_GAME_URL;
	private static final Random random = new Random(1);

	private final StatusController statusController;

	@Container public BrowserWebDriverContainer playerOne = new BrowserWebDriverContainer().withCapabilities(new ChromeOptions());
	//@Container public BrowserWebDriverContainer playerTwo = new BrowserWebDriverContainer().withCapabilities(new ChromeOptions());
	//@Container public BrowserWebDriverContainer playerThree = new BrowserWebDriverContainer().withCapabilities(new ChromeOptions());

	public DixitViewSelenideIT()
	{
		// chrome.getTestHostIpAddress()
		// String sutUrl = System.getProperty("sut.url");
		// if(sutUrl != null)
		// {
		// BASE_GAME_URL = sutUrl;
		// }
		// else
		// {
		// playerOne.getTestHostIpAddress()
		BASE_GAME_URL = "http://host.docker.internal/dixit";
		// }
		System.out.println("SUT: " + BASE_GAME_URL);
		ResteasyClient client = new ResteasyClientBuilderImpl().build();
		ResteasyWebTarget target = client.target(UriBuilder.fromPath(BASE_GAME_URL + "-rest"));
		statusController = target.proxy(StatusController.class);
	}

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
		System.out.println(System.getProperty("selenide.remote"));
		SelenideDriver clientBrowser = createBrowser(playerOne);
		String gameId = createGame(clientBrowser);

		ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(nrOfPlayers));
		List<ListenableFuture<?>> futures = new ArrayList<>();
		// Other players join
		//futures.add(executor.submit(automaticallyPlayingclient(playerTwo, gameId, randomPlayerName())));
		//futures.add(executor.submit(automaticallyPlayingclient(playerThree, gameId, randomPlayerName())));
		// for(int i = 1; i < nrOfPlayers; i++)
		// {
		// futures.add(executor.submit(automaticallyPlayingclient(gameId, randomPlayerName())));
		// }
		futures.add(executor.submit(new Callable<Void>(){
			@Override
			public Void call() throws Exception
			{
				joinAsPlayerAndPlayUntilEnd(gameId, randomPlayerName(), clientBrowser);
				return null;
			}
		}));

		ListenableFuture<List<Object>> allFuturesList = Futures.allAsList(futures);
		// A game between nrOfPlayers should finish within the allowed timeout
		allFuturesList.get(500, TimeUnit.SECONDS);
	}

	private SelenideDriver createBrowser(BrowserWebDriverContainer container)
	{
		SelenideConfig selenideConfig = new SelenideConfig();
		selenideConfig.browserCapabilities().setCapability("enableVideo", true);
		selenideConfig.browserCapabilities().setCapability("enableLog", true);
		// RemoteWebDriver driver = container.getWebDriver();
		// WebDriverRunner.setWebDriver(driver);
		SelenideDriver clientBrowser = new SelenideDriver(selenideConfig, container.getWebDriver(), null);
		// clientBrowser.browser().
		return clientBrowser;
	}

	private String urlToJoinFromGameId(String gameId)
	{
		return BASE_GAME_URL + "/" + gameId;
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

	private String createGame(SelenideDriver clientBrowser)
	{
		String initialUrl = BASE_GAME_URL;
		clientBrowser.open(initialUrl);
		waitForVaadin(clientBrowser.driver());
		clientBrowser.$("#" + CssId.DESIRED_AMOUNT_OF_PLAYERS).sendKeys(Keys.BACK_SPACE + "" + nrOfPlayers);
		waitForVaadin(clientBrowser.driver());
		clientBrowser.$("#" + CssId.CREATE_GAME_BUTTON).click();

		waitForVaadin(clientBrowser.driver());
		String urlToJoin = clientBrowser.url();
		if(initialUrl.equals(urlToJoin))
			throw new AssertionError(urlToJoin + " should contain a game id");

		String gameId = urlToJoin.replace(BASE_GAME_URL + "/", "");

		return gameId;
	}

	private Callable<Void> automaticallyPlayingclient(BrowserWebDriverContainer container, String gameId, String playerName)
	{
		return () -> {
			SelenideDriver clientBrowser = createBrowser(container);
			String urlToGame = urlToJoinFromGameId(gameId);
			clientBrowser.open(urlToGame);

			joinAsPlayerAndPlayUntilEnd(gameId, playerName, clientBrowser);
			return null;
		};
	}

	private void joinAsPlayerAndPlayUntilEnd(String gameId, String playerName, SelenideDriver clientBrowser)
			throws InterruptedException, TimeoutException, BrokenBarrierException
	{
		SelenideElement playerNamePrompt = clientBrowser.$("#" + CssId.PLAYER_NAME);
		waitForVaadin(clientBrowser.driver());
		playerNamePrompt.sendKeys(playerName);
		waitForVaadin(clientBrowser.driver());
		clientBrowser.$("#" + CssId.JOIN_GAME_BUTTON).click();
		waitForVaadin(clientBrowser.driver());
		// playerNamePrompt.pressEnter();

		// int statusCounter = Integer.parseInt(statusWebElement.getAttribute(HTLMProperties.STATUS_COUNTER));

		RequestedAction requestedAction = RequestedAction.WAIT;
		while(requestedAction != RequestedAction.GAME_FINISHED_GO_HOME)
		{
			syncWithOtherPlayers(gameId, clientBrowser);
			SelenideElement statusSelenideElement = clientBrowser.$("#" + CssId.STATUS);
			WebElement statusWebElement = statusSelenideElement.toWebElement();
			String attribute = statusWebElement.getAttribute(HTLMProperties.REQUESTED_ACTION);
			if(attribute == null)
			{
				// Happens before the game starts
				Thread.sleep(10);
				continue;
			}
			// throw new AssertionError("how can this happen? Attribute is null");
			// TODO: how can this happen?
			// Thread.sleep(10000);
			// continue;
			requestedAction = RequestedAction.fromAttribute(attribute);
			System.out.println(playerName + " is requested to: " + requestedAction);
			switch(requestedAction)
			{
			case GUESS_WHICH_CARD:
				clickFirstCard(clientBrowser);
				// signalThatActionWasTaken(gameId);
				break;
			case MAKE_A_SENTENCE:
				closeRoundSummarizationIfItExists(clientBrowser);
				// TODO: how to avoid repeated calls?
				makeSentenceAndPickCard(clientBrowser);
				// signalThatActionWasTaken(gameId);
				break;
			case MATCH_CARD_TO_SENTENCE:
				closeRoundSummarizationIfItExists(clientBrowser);
				clickFirstCard(clientBrowser);
				// signalThatActionWasTaken(gameId);
				break;
			case WAIT:
				break;
			case GAME_FINISHED_GO_HOME:
			default:
				break;
			}
			if(requestedAction != RequestedAction.GAME_FINISHED_GO_HOME && requestedAction != RequestedAction.WAIT)
			{
				statusSelenideElement.shouldNotHave(Condition.attribute(HTLMProperties.REQUESTED_ACTION, requestedAction.asAttribute()));
				// statusSelenideElement.waitWhile(Condition.attribute(HTLMProperties.REQUESTED_ACTION, requestedAction.asAttribute()),
				// 10000);
			}
		}
		List<String> severeLogEntries = new ArrayList<>();
		for(String logEntry : clientBrowser.getWebDriverLogs().logs(LogType.BROWSER, Level.SEVERE))
		{
			severeLogEntries.add(logEntry);
		}
		assertThat(severeLogEntries).isEmpty();
	}

	private void closeRoundSummarizationIfItExists(SelenideDriver clientBrowser)
	{
		SelenideElement closeButton = clientBrowser.$("#" + CssId.CLOSE_SUMMARIZATION);
		if(closeButton.exists())
		{
			closeButton.click();
		}
	}

	private void signalThatActionWasTaken(String gameId) throws AssertionError, TimeoutException
	{
		flushServerSideEvents(gameId);
	}

	private static final CyclicBarrier barrier = new CyclicBarrier(nrOfPlayers);

	private void syncWithOtherPlayers(String gameId, SelenideDriver clientBrowser)
			throws InterruptedException, TimeoutException, BrokenBarrierException
	{
		waitForVaadin(clientBrowser.driver());
		// Wait until all players have finished responded to the last requested action
		barrier.await(60, TimeUnit.SECONDS);
		// flushServerSideEvents(gameId);
		waitForVaadin(clientBrowser.driver());
	}

	private void flushServerSideEvents(String gameId) throws AssertionError, TimeoutException
	{
		Status status = statusController.wait(gameId, 10L, TimeUnit.SECONDS);
		switch(status)
		{
		case FLUSHED:
			// Server side events are cleared, good
			break;
		case GAME_DOES_NOT_EXIST:
			throw new AssertionError(gameId + " does not exist");
		case SHUTTING_DOWN:
			throw new IllegalStateException("Server is shutting down before players disconnected?");
		case TIMEOUT:
			throw new TimeoutException("Flushing events took more than the allowed timeout");
		case TOO_MANY_FLUSHERS:
			throw new IllegalStateException("Tests are stacking up flush calls? Not allowed.");
		default:
			break;
		}
	}

	private void makeSentenceAndPickCard(SelenideDriver clientBrowser)
	{
		// TODO: make funnier sentences
		waitForVaadin(clientBrowser.driver());
		SelenideElement sentencePrompt = clientBrowser.$("#" + CssId.SENTENCE_PROMPT);
		sentencePrompt.sendKeys("Random" + Keys.ENTER);
		clientBrowser.$("#" + CssId.SEND_SENTENCE).click();
		// sentencePrompt.pressEnter();
		waitForVaadin(clientBrowser.driver());
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
