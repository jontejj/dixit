package com.github.jontejj.dixit;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.jontejj.dixit.Participant.InvalidCardPicked;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;

@Route("")
@CssImport("styles/shared-styles.css")
@Push
public class DixitView extends HorizontalLayout implements HasUrlParameter<String>
{

	private static final String BUCKET_BASEPATH = "https://storage.googleapis.com/com-github-jontejj-dixit/cards/";

	private final Games games;

	Dixit currentGame;

	private String gameId;

	private VerticalLayout messages = new VerticalLayout();

	// private Label gameIdLabel;

	Participant me;

	private FlexLayout gameArea;

	private FlexLayout cardArea;

	private FlexLayout left;
	private FlexLayout right;

	public DixitView(@Autowired Games games)
	{
		left = new FlexLayout();
		right = new FlexLayout();
		right.setFlexDirection(FlexDirection.ROW_REVERSE);
		add(left, right);
		// setFlexDirection(FlexDirection.COLUMN);
		// setFlexWrap(FlexWrap.WRAP);
		this.games = games;
		// String gameId = firstNonNull(vaadinRequest.getParameter("game"), UUID.randomUUID().toString());
		// Dixit game = games.getOrCreate(gameId);
		// VerticalLayout todosList = new VerticalLayout(); // (1)
		Image img = new Image(BUCKET_BASEPATH + "1.png", "1.png");
		// todosList.add(img);

		// gameIdLabel = new Label(this.gameId);
		// gameIdLabel.setTitle("Game id");

		// waitUntilAllPlayersHaveJoined();
		// TextField taskField = new TextField(); // (2)
		// taskField.setLabel("My label");
		// taskField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
		// Button addButton = new Button("Add"); // (3)
		// addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		// addButton.addClickShortcut(Key.ENTER);
		// addButton.addClickListener(click -> {
		// // (4)
		// Checkbox checkbox = new Checkbox(taskField.getValue());
		// todosList.add(checkbox);
		// });
		// new HorizontalLayout(taskField, addButton)
		H1 logo = new H1("Dixit");
		logo.addClickListener(e -> {
			left.removeAll();
			right.removeAll();
			messages = new VerticalLayout();
			left.add(logo);
			currentGame = null;
			me = null;
			getUI().get().navigate(DixitView.class);
		});
		left.add(logo);

		// add(gameLink(this.gameId));
	}

	private void joinGame(String playerName, Dixit createdGame) throws GameNotConfiguredYet, AllPlayersAlreadyJoined, PlayerNameAlreadyTaken
	{
		me = createdGame.join(this::receiveEvent, new Player(playerName));
	}

	Dixit getGame()
	{
		return currentGame;
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String gameId)
	{
		this.setGameId(gameId == null ? UUID.randomUUID().toString() : gameId);

		currentGame = games.getOrCreate(this.gameId);

		if(gameId == null)
		{
			addCreateGameUI();
		}
		else
		{
			addJoinUI();
		}
	}

	private void addCreateGameUI()
	{
		VerticalLayout createGameArea = new VerticalLayout();
		IntegerField desiredAmountOfPlayers = new IntegerField();
		desiredAmountOfPlayers.setLabel("Nr of players");
		desiredAmountOfPlayers.setMin(2);
		desiredAmountOfPlayers.setMax(Dixit.CARDS_IN_DECK / Dixit.CARDS_IN_HAND);
		desiredAmountOfPlayers.setValue(5);
		Button createButton = new Button("Create Game");
		createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		createButton.addClickListener(e -> {
			this.setGameId(UUID.randomUUID().toString());
			Dixit createdGame = games.getOrCreate(this.gameId);
			createdGame.desiredAmountOfPlayers = desiredAmountOfPlayers.getValue();
			getUI().get().navigate(this.gameId);
			left.remove(createGameArea);
			// addJoinUI();
		});
		createGameArea.add(desiredAmountOfPlayers, createButton);
		left.add(createGameArea);
	}

	private void addJoinUI()
	{
		VerticalLayout joinArea = new VerticalLayout();
		TextField playerName = new TextField();
		playerName.setLabel("My player name");

		Button joinButton = new Button("Join");
		joinButton.addClickShortcut(Key.ENTER);
		joinButton.addClickListener(e -> {
			try
			{
				if(playerName.isEmpty())
					throw new EmptyPlayerName();
				joinGame(playerName.getValue(), currentGame);
			}
			catch(GameNotConfiguredYet | AllPlayersAlreadyJoined | PlayerNameAlreadyTaken | EmptyPlayerName error)
			{
				Notification.show(error.getMessage());
				return;
			}
			left.remove(joinArea);
			playerName.setEnabled(false);
			addMessagingArea();
			repaintGameInfoArea();
		});
		joinArea.add(playerName, joinButton);
		left.add(joinArea);
	}

	void repaintGameInfoArea()
	{
		if(gameArea != null)
		{
			left.remove(gameArea);
		}

		gameArea = new FlexLayout();
		gameArea.setFlexDirection(FlexDirection.COLUMN);

		gameArea.add(new Label("My name: " + me.player.name));
		gameArea.add(new Label("Scores"));
		for(Participant player : currentGame.players)
		{
			gameArea.add(new Label(player.player.name + " : " + player.score));
		}
		// Players - score
		left.add(gameArea);
	}

	private void addMessagingArea()
	{
		FlexLayout messagingArea = new FlexLayout();
		messagingArea.setFlexDirection(FlexDirection.COLUMN);

		// Input chatMessage = new Input();
		// chatMessage.addShortcut(Key.ENTER);
		TextField chatMessageSender = new TextField();
		chatMessageSender.addKeyUpListener(Key.ENTER, (e) -> {
			currentGame.broadcast(new ChatMessageEvent(me.player, chatMessageSender.getValue()));
		});
		// (message) -> currentGame.broadcast(new ChatMessageEvent(message)));
		// Shortcut.add(chatMessageSender, Key.ENTER, () -> {
		// currentGame.broadcast(new ChatMessageEvent(chatMessageSender.getValue()));
		// });
		chatMessageSender.setLabel("Send message");

		// Button sendChatMessage = new Button("Send chat message");
		// sendChatMessage.addClickListener(e -> {
		// ;
		// });
		messagingArea.add(chatMessageSender, messages);
		right.add(messagingArea);
	}

	public void receiveEvent(GameEvent event)
	{
		// String message = event.toString();
		// if(getUI().map(UI::i) isAttached())
		{
			// Must lock the session to execute logic safely
			try
			{
				getUI().orElseThrow().access(() -> {
					// addSystemMessage(message);
					event.executeInternally(DixitView.this);
				}).get();
			}
			catch(InterruptedException | ExecutionException e)
			{
				getUI().orElseThrow().access(() -> Notification.show("Error occured while processing event: " + event + " :" + e));

			}
		}
	}

	void addMessage(String message)
	{
		Label m = new Label(message);
		messages.addComponentAsFirst(m);
	}

	void addSystemMessage(String message)
	{
		if(message.isEmpty())
			return;

		Label m = new Label(message);
		m.getStyle().set("font-weight", "bold");
		messages.addComponentAsFirst(m);
	}

	// private Component gameLink(String gameId)
	// {
	// URI linkToGame = getLink(gameId);
	//
	// HorizontalLayout linkLayout = new HorizontalLayout();
	// Label gameLinkDescription = new Label("Share:");
	// Label label = new Label(linkToGame.toString());
	// label.setId("linkForGame");
	//
	// Button clipboardButton = new Button("Copies the link: " + linkToGame + " into your clipboard");
	// clipboardButton.addClickListener((e) -> {
	// Notification.show("Copy to clipboard successful");
	// });
	// ClipboardHelper clipboardHelper = new ClipboardHelper("some stuff", clipboardButton);
	// linkLayout.add(gameLinkDescription, label, clipboardButton);
	// return linkLayout;
	// }

	private URI getLink(String aGameId)
	{
		String url = UI.getCurrent().getRouter().getUrl(DixitView.class, aGameId);
		// URI linkToGame;
		// getUI().get().getPage().setLocation(uri);
		// try
		// {
		// Map<String, String> query = UriComponent.decodeQuery(uri.getQuery(), false);
		// query.put("game", gameId);
		// String newQuery = Joiner.on("&").withKeyValueSeparator("=").join(query);
		// linkToGame = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), newQuery,
		// uri.getFragment());
		// }
		// catch(URISyntaxException e)
		// {
		// throw new IllegalStateException("Could not construct game link for: " + uri);
		// }
		try
		{
			return new URI(url);
		}
		catch(URISyntaxException e)
		{
			throw new IllegalStateException("Impossible!", e);
		}
	}

	public void setGameId(String gameId)
	{
		this.gameId = gameId;
		// this.gameIdLabel.setText(this.gameId);
	}

	public void askForSentence()
	{
		addSystemMessage("Make up a sentence to describe your card");
		VerticalLayout sentenceArea = new VerticalLayout();

		AtomicReference<Card> pickedCard = new AtomicReference<>();
		AtomicReference<String> sentence = new AtomicReference<>();
		TextField sentenceField = new TextField();
		sentenceField.setLabel("Sentence");
		sentenceField.addKeyUpListener(Key.ENTER, (e) -> {
			if(pickedCard.get() != null)
			{
				try
				{
					currentGame.currentStoryTeller.makeUpSentance(sentenceField.getValue(), pickedCard.get());
				}
				catch(InvalidCardPicked invalidCard)
				{
					Notification.show(invalidCard.getMessage());
					return;
				}
				currentGame.broadcast(new SentanceCreated(sentenceField.getValue()));
				left.remove(sentenceArea);
			}
			else
			{
				sentence.set(sentenceField.getValue());
			}
		});

		sentenceArea.add(sentenceField);
		left.add(sentenceArea);
		showCardsWithPicker(me.cards, card -> {
			if(sentence.get() != null)
			{
				try
				{
					currentGame.currentStoryTeller.makeUpSentance(sentenceField.getValue(), card);
				}
				catch(InvalidCardPicked invalidCard)
				{
					Notification.show(invalidCard.getMessage());
					return;
				}
				currentGame.broadcast(new SentanceCreated(sentenceField.getValue()));
				left.remove(sentenceArea);
			}
			else
			{
				pickedCard.set(card);
			}
		});
	}

	public void showCardsWithPicker(List<? extends Card> cardsToPickAmongst, Consumer<Card> pickedCardAction)
	{
		removeCardArea();
		cardArea = new FlexLayout();
		cardArea.setFlexDirection(FlexDirection.ROW);
		cardArea.setFlexWrap(FlexWrap.WRAP);
		for(Card card : cardsToPickAmongst)
		{
			Image cardImage = new Image(BUCKET_BASEPATH + card.number + ".png", card.toString());
			cardImage.setMaxWidth("200px");
			cardImage.setMaxHeight("293px");
			cardImage.addClickListener((e) -> {
				pickedCardAction.accept(card);
				DixitView.this.remove(cardArea);
			});
			cardArea.add(cardImage);
		}
		left.add(cardArea);
	}

	public void showPickedCardsToPlayersAndAskToPickStoryTellersCard()
	{
		List<PickedCard> givenCards = currentGame.currentStoryTeller.getGivenCards();
		if(me.equals(currentGame.currentStoryTeller.participant))
		{
			addSystemMessage("Here are the cards the other players picked");
			showCardsWithPicker(givenCards, (card) -> {
			});
		}
		else
		{
			addSystemMessage("Guess which card the story teller played");
			givenCards.removeIf(card -> card.pickedBy.player.equals(me.player));
			showCardsWithPicker(givenCards, pickedCard -> {
				try
				{
					currentGame.currentStoryTeller.betOnStoryTellersCard(pickedCard, me);
					currentGame.broadcast(new PlayerGuessedStoryTellerCard(me.player));
				}
				catch(InvalidCardPicked e)
				{
					Notification.show(e.getMessage());
				}
			});
		}
	}

	public void removeCardArea()
	{
		if(cardArea != null)
		{
			left.remove(cardArea);
		}
	}
}
