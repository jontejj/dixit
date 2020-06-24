package com.github.jontejj.dixit;

import static com.github.jontejj.dixit.TranslationKey.CARD_PICKED_BUT_NO_SENTENCE;
import static com.github.jontejj.dixit.TranslationKey.ERROR;
import static com.github.jontejj.dixit.TranslationKey.GUESS_WHICH_CARD;
import static com.github.jontejj.dixit.TranslationKey.MAKE_UP_A_SENTENCE_AND_PICK_A_CARD;
import static com.github.jontejj.dixit.TranslationKey.OTHER_PLAYERS_PICKED_THESE_CARDS;
import static com.github.jontejj.dixit.TranslationKey.ROLL_YOUR_THUMBS_GUESSER;
import static com.github.jontejj.dixit.TranslationKey.ROLL_YOUR_THUMBS_STORY_TELLER;
import static com.github.jontejj.dixit.TranslationKey.SEND_CHAT_MESSAGE;
import static com.github.jontejj.dixit.TranslationKey.SENTENCE_GIVEN_BUT_NO_CARD;
import static com.github.jontejj.dixit.TranslationKey.STORY_TELLER_SAYS;
import static com.github.jontejj.dixit.TranslationKey.WAITING_FOR_OTHER_PLAYERS_TO_PICK_A_CARD;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.jontejj.dixit.events.ChatMessageEvent;
import com.github.jontejj.dixit.events.GameEvent;
import com.github.jontejj.dixit.events.PlayerGuessedStoryTellerCard;
import com.github.jontejj.dixit.events.PlayerPickedMatchingCard;
import com.github.jontejj.dixit.exceptions.AllPlayersAlreadyJoined;
import com.github.jontejj.dixit.exceptions.EmptyDeck;
import com.github.jontejj.dixit.exceptions.EmptyPlayerName;
import com.github.jontejj.dixit.exceptions.GameNotConfiguredYet;
import com.github.jontejj.dixit.exceptions.InvalidCardPicked;
import com.github.jontejj.dixit.exceptions.PlayerAlreadyGaveCard;
import com.github.jontejj.dixit.exceptions.PlayerNameAlreadyTaken;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.shared.communication.PushMode;

@Route("dixit")
@CssImport("./styles/shared-styles.css")
@Push(PushMode.MANUAL)
@PreserveOnRefresh
@PWA(name = "dixit", shortName = "dixit")
public class DixitView extends HorizontalLayout implements HasUrlParameter<String>, DixitCallback
{
	private static final long serialVersionUID = 1L;

	enum Selectable
	{
		YES,
		NO
	}

	private static final String BUCKET_BASEPATH = "https://storage.googleapis.com/com-github-jontejj-dixit/cards/";

	private final Games games;

	Dixit currentGame;

	private String gameId;

	private VerticalLayout messages = new VerticalLayout();

	// private Label gameIdLabel;

	Participant me;

	private FlexLayout gameArea;

	private FlexLayout cardArea;

	private Label status;

	private FlexLayout left;
	private FlexLayout right;

	private HorizontalLayout summarizationView;

	private EventReceiver listener;

	private int statusCounter = 0;

	public DixitView(@Autowired Games games)
	{
		left = new FlexLayout();
		left.setFlexWrap(FlexWrap.WRAP);
		right = new FlexLayout();
		right.setFlexDirection(FlexDirection.ROW_REVERSE);
		SplitLayout splitLayout = new SplitLayout(left, right);
		splitLayout.setSplitterPosition(70);
		splitLayout.setSizeFull();
		add(splitLayout);
		// add(left, right);
		this.games = games;

		// gameIdLabel = new Label(this.gameId);
		// gameIdLabel.setTitle("Game id");

		H1 logo = new H1("Dixit");
		logo.addClassName("logo");
		logo.addClickListener(e -> {
			String url = RouteConfiguration.forRegistry(UI.getCurrent().getRouter().getRegistry()).getUrl(DixitView.class);
			getUI().get().getPage().open(url, "_blank");

		});
		status = statusLabel();
		left.add(logo);
		gameInfoChanged();

		// add(gameLink(this.gameId));
	}

	private Label statusLabel()
	{
		Label statusLabel = new Label("");
		statusLabel.setId(CssId.STATUS);
		statusLabel.getElement().setProperty(HTLMProperties.STATUS_COUNTER, statusCounter);
		return statusLabel;
	}

	@Override
	protected void onAttach(AttachEvent attachEvent)
	{
		UI ui = attachEvent.getUI();
		if(me != null)
		{
			listener = new EventReceiver(ui);
			currentGame.rejoin(listener);
			status.setText("Rejoined game");
		}
	}

	@Override
	protected void onDetach(DetachEvent detachEvent)
	{
		if(me != null)
		{
			currentGame.unregister(me, listener);
			listener = null;
			// status.setText("Left game");
		}
	}

	private void joinGame(String playerName, Dixit createdGame) throws GameNotConfiguredYet, AllPlayersAlreadyJoined, PlayerNameAlreadyTaken
	{
		listener = new EventReceiver(getUI().orElseThrow());
		me = createdGame.join(listener, new Player(playerName));
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
		else if(me == null)
		{
			addJoinUI();
		}
	}

	private void addCreateGameUI()
	{
		VerticalLayout createGameArea = new VerticalLayout();
		IntegerField desiredAmountOfPlayers = new IntegerField();
		desiredAmountOfPlayers.setId(CssId.DESIRED_AMOUNT_OF_PLAYERS);
		desiredAmountOfPlayers.setLabel(getTranslation(TranslationKey.NR_OF_PLAYERS_PROMPT.key()));
		desiredAmountOfPlayers.setMin(2);
		desiredAmountOfPlayers.setMax(Dixit.CARDS_IN_DECK / Dixit.CARDS_IN_HAND);
		desiredAmountOfPlayers.setValue(5);
		desiredAmountOfPlayers.focus();
		// desiredAmountOfPlayers.addKeyUpListener(key, listener, modifiers)
		Button createButton = new Button(getTranslation(TranslationKey.CREATE_GAME_BUTTON.key()));
		createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		createButton.addClickShortcut(Key.ENTER);
		createButton.addClickListener(createGame(createGameArea, desiredAmountOfPlayers));
		createGameArea.add(desiredAmountOfPlayers, createButton);
		left.add(createGameArea);
	}

	private ComponentEventListener<ClickEvent<Button>> createGame(Component createGameArea, IntegerField desiredAmountOfPlayers)
	{
		return e -> {
			this.setGameId(UUID.randomUUID().toString());
			Dixit createdGame = games.getOrCreate(this.gameId);
			createdGame.desiredAmountOfPlayers = desiredAmountOfPlayers.getValue();
			getUI().get().navigate(DixitView.class, this.gameId);
			left.remove(createGameArea);
		};
	}

	private void addJoinUI()
	{
		VerticalLayout joinArea = new VerticalLayout();
		TextField playerName = new TextField();
		playerName.setId(CssId.PLAYER_NAME);
		playerName.setLabel(getTranslation(TranslationKey.MY_NAME_PROMPT.key()));
		playerName.focus();

		Button joinButton = new Button(getTranslation(TranslationKey.JOIN_GAME_BUTTON.key()));
		joinButton.addClickShortcut(Key.ENTER);
		joinButton.addClickListener(e -> {
			try
			{
				// TODO: why is this empty even when the client has finished pushing?
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
		});
		joinArea.add(playerName, joinButton);
		left.add(joinArea);
		status.setText(getTranslation(TranslationKey.SET_NAME.key()));
	}

	public void gameInfoChanged()
	{
		if(gameArea != null)
		{
			left.remove(gameArea);
		}

		gameArea = new FlexLayout();
		gameArea.addClassName("gameArea");
		gameArea.setFlexWrap(FlexWrap.NOWRAP);
		gameArea.setFlexDirection(FlexDirection.COLUMN);

		gameArea.add(status);

		if(currentGame != null)
		{

			if(currentGame.isInPlay())
			{
				gameArea.add(new Label(getTranslation(TranslationKey.CARDS_REMAINING.key(), currentGame.cardsRemaining())));
			}
			if(me != null)
			{
				gameArea.add(new Label(getTranslation(TranslationKey.MY_NAME_IS.key(), me.player.name)));
			}
			gameArea.add(new Label(getTranslation(TranslationKey.SCORES.key())));
			for(Participant player : currentGame.players)
			{
				gameArea.add(new Label(player.player.name + " : " + player.score));
			}
		}
		left.add(gameArea);
	}

	private void addMessagingArea()
	{
		FlexLayout messagingArea = new FlexLayout();
		messagingArea.setFlexDirection(FlexDirection.COLUMN);

		TextField chatMessageSender = new TextField();
		chatMessageSender.addKeyUpListener(Key.ENTER, (e) -> {
			String message = chatMessageSender.getValue();
			if(message.isBlank())
				return;

			currentGame.broadcast(new ChatMessageEvent(me.player, message));
			chatMessageSender.clear();
		});
		chatMessageSender.setLabel(getTranslation(SEND_CHAT_MESSAGE.key()));

		messagingArea.add(chatMessageSender, messages);
		Scroller scroller = new Scroller(messagingArea);
		scroller.addClassName("messagingArea");
		right.add(scroller);
	}

	private class EventReceiver implements Consumer<GameEvent>
	{
		UI ui;

		EventReceiver(UI ui)
		{
			this.ui = ui;
		}

		@Override
		public void accept(GameEvent event)
		{
			// Must lock the session to execute logic safely
			try
			{
				ui.access(() -> {
					event.translationKeyToDescribeEvent().ifPresent(translationKey -> {
						addSystemMessage(getTranslation(translationKey.key(), event.translationKeyParams()));
					});
					event.execute(DixitView.this);
					ui.push();
				}).get();
			}
			catch(InterruptedException | ExecutionException e)
			{
				getUI().orElseThrow().access(() -> Notification.show("Error occured while processing event: " + event + " :" + e));

			}
		}
	}

	@Override
	public void addChatMessage(Player sender, String message)
	{
		String msg = sender + ": " + message;
		Label m = new Label(msg);
		messages.addComponentAsFirst(m);
	}

	public void addMessage(String message)
	{
		Label m = new Label(message);
		messages.addComponentAsFirst(m);
	}

	public void addSystemMessage(String message)
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
		updateStatusText(MAKE_UP_A_SENTENCE_AND_PICK_A_CARD);
		updateRequestedAction(RequestedAction.MAKE_A_SENTENCE);
		VerticalLayout sentenceArea = new VerticalLayout();

		AtomicReference<Card> pickedCard = new AtomicReference<>();
		AtomicReference<String> sentence = new AtomicReference<>();
		TextField sentenceField = new TextField();
		sentenceField.setId(CssId.SENTENCE_PROMPT);
		sentenceField.setLabel(getTranslation(TranslationKey.SENTENCE.key()));
		sentenceField.addKeyUpListener(Key.ENTER, (e) -> {
			if(pickedCard.get() != null)
			{
				pickCardAndSentenceAsStoryTeller(pickedCard.get(), sentenceField.getValue());
				left.remove(sentenceArea);
				return;
			}
			sentence.set(sentenceField.getValue());
			updateStatusText(SENTENCE_GIVEN_BUT_NO_CARD);
		});

		sentenceArea.add(sentenceField);
		left.add(sentenceArea);
		showCardsWithPicker(me.cards, card -> {
			if(sentence.get() != null)
			{
				pickCardAndSentenceAsStoryTeller(card, sentence.get());
				left.remove(sentenceArea);
			}
			else
			{
				pickedCard.set(card);
				updateStatusText(CARD_PICKED_BUT_NO_SENTENCE);
			}
		}, Selectable.YES);
	}

	private void updateStatusText(TranslationKey actionRequired, Object ... params)
	{
		String translatedMessage = getTranslation(actionRequired.key(), (Object[]) params);
		addSystemMessage(translatedMessage);
		status.setText(translatedMessage);
	}

	private void updateRequestedAction(RequestedAction requiredAction)
	{
		statusCounter++;
		status.getElement().setProperty(HTLMProperties.REQUESTED_ACTION, requiredAction.asAttribute());
		status.getElement().setProperty(HTLMProperties.STATUS_COUNTER, statusCounter);
	}

	private void pickCardAndSentenceAsStoryTeller(Card chosenCard, String message)
	{
		try
		{
			currentGame.makeUpSentance(message, chosenCard);
		}
		catch(InvalidCardPicked | EmptyDeck invalidCard)
		{
			Notification.show(invalidCard.getMessage());
		}
	}

	public void showCardsWithPicker(List<? extends Card> cardsToPickAmongst, Consumer<Card> pickedCardAction, Selectable selectable)
	{
		removeCardArea();
		cardArea = new FlexLayout();
		cardArea.setId(CssId.CARD_AREA);
		cardArea.setFlexDirection(FlexDirection.ROW);
		cardArea.setFlexWrap(FlexWrap.WRAP);
		for(Card card : cardsToPickAmongst)
		{
			Image cardImage = imageFor(card);
			if(selectable == Selectable.YES)
			{
				cardImage.addClassName(CssClassNames.SELECTABLE);
			}
			cardImage.addClickListener((e) -> {
				pickedCardAction.accept(card);
				left.remove(cardArea);
				cardArea = null;
			});
			cardArea.add(cardImage);
		}
		left.add(cardArea);
	}

	private Image imageFor(Card card)
	{
		Image cardImage = new Image(BUCKET_BASEPATH + card.number + ".png", card.toString());
		cardImage.addClassName("card");
		return cardImage;
	}

	public void showPickedCardsToPlayersAndAskToPickStoryTellersCard()
	{
		List<PickedCard> givenCards = currentGame.currentStoryTeller.getGivenCards();
		if(me.player.equals(currentGame.currentStoryTeller.player))
		{
			updateStatusText(OTHER_PLAYERS_PICKED_THESE_CARDS);
			showCardsWithPicker(givenCards, (card) -> {
			}, Selectable.NO);
		}
		else
		{
			updateStatusText(GUESS_WHICH_CARD);
			updateRequestedAction(RequestedAction.GUESS_WHICH_CARD);
			givenCards.removeIf(card -> card.pickedBy.equals(me.player));
			showCardsWithPicker(givenCards, pickedCard -> {
				try
				{
					currentGame.currentStoryTeller.betOnStoryTellersCard(pickedCard, me.player);
					updateStatusText(WAITING_FOR_OTHER_PLAYERS_TO_PICK_A_CARD);
					updateRequestedAction(RequestedAction.WAIT);
					currentGame.broadcast(new PlayerGuessedStoryTellerCard(me.player));
				}
				catch(InvalidCardPicked e)
				{
					Notification.show(e.getMessage());
				}
			}, Selectable.YES);
		}
	}

	public void removeCardArea()
	{
		if(cardArea != null)
		{
			left.remove(cardArea);
			cardArea = null;
		}
	}

	public void summarizationReceived(RoundSummarization summarization)
	{
		if(me.player.equals(currentGame.currentStoryTeller.player))
		{
			// Hide the cards that the players picked
			removeCardArea();
		}
		if(summarizationView != null)
		{
			this.left.remove(summarizationView);
		}
		summarizationView = new HorizontalLayout();
		VerticalLayout scoresView = new VerticalLayout();
		scoresView.add(new H4(getTranslation(TranslationKey.ROUND_SUMMARIZATION.key())));
		for(Participant p : currentGame.players)
		{
			// TODO: change to a table view
			scoresView.add(new Label(p.player.name + ": " + summarization.getTotalScoreIncreseForPlayer(p.player)));
		}
		HorizontalLayout pickedCardsView = new HorizontalLayout();
		summarization.forEachPickedCard((pickedCard, playersWhoGuessedIt) -> {
			VerticalLayout pickedCardView = new VerticalLayout();
			Label playedBy = new Label(getTranslation(TranslationKey.CARD_PLAYED_BY.key(), pickedCard.pickedBy));
			if(pickedCard.pickedBy.equals(summarization.getStoryTeller()))
			{
				playedBy.addClassName("storyteller");
			}
			pickedCardView.add(playedBy);
			pickedCardView.add(new Label(getTranslation(TranslationKey.CARD_PICKED_BY.key(), playersWhoGuessedIt)));
			pickedCardView.add(imageFor(pickedCard));
			pickedCardsView.add(pickedCardView);
		});
		pickedCardsView.add(new Button(getTranslation(TranslationKey.CLOSE_ROUND_SUMMARIZATION.key()), e -> {
			this.left.remove(summarizationView);
			summarizationView = null;
		}));
		summarizationView.add(scoresView, pickedCardsView);
		this.left.add(summarizationView);
	}

	@Override
	public void playerGuessedStoryTellerCard()
	{
		if(me.player.equals(currentGame.currentStoryTeller.player))
		{
			if(currentGame.currentStoryTeller.hasCollectedAllGuesses())
			{
				currentGame.distributeScoresAndPickNewStoryTeller();
			}
		}
	}

	@Override
	public void playerPickedMatchingCard(Player playerThatPicked)
	{
		if(currentGame.currentStoryTeller.getNumberOfGivenCards() == currentGame.players.size())
		{
			// Show all picked cards to all players, story-teller (should only be able to see)
			showPickedCardsToPlayersAndAskToPickStoryTellersCard();
		}
	}

	@Override
	public void sentenceCreated(String message)
	{
		if(me.player.equals(currentGame.currentStoryTeller.player))
		{
			updateStatusText(ROLL_YOUR_THUMBS_STORY_TELLER);
			updateRequestedAction(RequestedAction.WAIT);
		}
		else
		{
			Player storyTeller = currentGame.currentStoryTeller.player;
			updateStatusText(STORY_TELLER_SAYS, storyTeller, message);
			updateRequestedAction(RequestedAction.MATCH_CARD_TO_SENTENCE);
			showCardsWithPicker(me.cards, card -> {
				try
				{
					// TODO: these operations should be done inside of Dixit
					PickedCard pickedCard = me.pickCardThatMatchesTheStoryTellers(card);
					currentGame.currentStoryTeller.givePickedCard(pickedCard);
					updateStatusText(WAITING_FOR_OTHER_PLAYERS_TO_PICK_A_CARD);
					updateRequestedAction(RequestedAction.WAIT);
					currentGame.broadcast(new PlayerPickedMatchingCard(me.player));
					me.cards.remove(card);
					currentGame.giveOneCardToPlayer(me);
				}
				catch(InvalidCardPicked | EmptyDeck | PlayerAlreadyGaveCard error)
				{
					updateStatusText(ERROR, error.getMessage());
					Notification.show(error.getMessage());
				}
			}, Selectable.YES);
		}
	}

	@Override
	public void storyTellerPicked(Player newStoryTeller)
	{
		// TODO: the dixit instance should keep track of who I am?
		if(me.player.equals(newStoryTeller))
		{
			askForSentence();
		}
		else
		{
			updateStatusText(ROLL_YOUR_THUMBS_GUESSER);
			updateRequestedAction(RequestedAction.WAIT);
		}
	}

	@Override
	public void gameFinished(Player winner)
	{
		updateStatusText(TranslationKey.GAME_FINISHED, winner);
		updateRequestedAction(RequestedAction.GAME_FINISHED_GO_HOME);
	}
}
