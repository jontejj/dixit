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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A server side instance of this represents one ongoing game of Dixit.
 * Based on https://boardgamegeek.com/boardgame/39856/dixit
 */
public class Dixit
{
	static final int CARDS_IN_DECK = 84;
	static final int CARDS_IN_HAND = 6;
	private static final int UNDEFINED = -1;
	List<Participant> players = new ArrayList<>();

	private Deque<Card> deck = null;

	int desiredAmountOfPlayers = UNDEFINED;
	int currentStoryTellerIndex = 0;

	// TODO(jontejj): benchmark and test to see if more threads are needed
	static ExecutorService executorService = Executors.newSingleThreadExecutor();

	private List<Consumer<GameEvent>> consumers = new CopyOnWriteArrayList<>();

	private EventSaver saver = new EventSaver();
	StoryTeller currentStoryTeller;

	Dixit()
	{
		this.consumers.add(saver::handleEvent);
	}

	public Participant join(Consumer<GameEvent> listener, Player player) throws GameNotConfiguredYet, AllPlayersAlreadyJoined, PlayerNameAlreadyTaken
	{
		if(desiredAmountOfPlayers == UNDEFINED)
			throw new GameNotConfiguredYet();
		if(players.size() >= desiredAmountOfPlayers)
			throw new AllPlayersAlreadyJoined();

		Participant participant = new Participant(player);
		if(players.contains(participant))
			throw new PlayerNameAlreadyTaken();

		consumers.add(listener);
		players.add(participant);
		// TODO: consider this case
		// if(placements > 0 && playerWhoJoined == Player.X)
		// {
		// // Happens if X disconnects when looking at the game over dialog and then someone reconnects as X
		// resetGame();
		// }
		broadcast(new JoinEvent(participant, listener));
		// This can't simply check for O as players can rejoin
		if(isAllNeededPlayersAssigned())
		{
			// Let the game begin
			play();
		}
		return participant;
	}

	public boolean isAllNeededPlayersAssigned()
	{
		return players.size() == desiredAmountOfPlayers;
	}

	public void leave(Participant participant)
	{
		broadcast(new LeaveEvent(participant));
	}

	public void unregister(Participant participant, Consumer<GameEvent> listener)
	{
		consumers.remove(listener);
		leave(participant);
	}

	public synchronized void broadcast(final GameEvent event)
	{
		for(final Consumer<GameEvent> listener : consumers)
		{
			// TODO: exception handling here and propagate them?
			executorService.execute(() -> listener.accept(event));
		}
	}

	public void play()
	{
		broadcast(new GameStarted());
		List<Card> newDeck = IntStream.range(1, 85).boxed().map(Card::new).collect(Collectors.toCollection(ArrayList::new));
		Collections.shuffle(newDeck);
		deck = new LinkedList<>(newDeck);
		distributeCardsToPlayers();
		currentStoryTeller = pickStoryTeller();
	}

	private void distributeCardsToPlayers()
	{
		for(Participant player : players)
		{
			for(int i = 0; i < Dixit.CARDS_IN_HAND; i++)
			{
				try
				{
					giveOneCardToPlayer(player);
				}
				catch(EmptyDeck empty)
				{
					throw new IllegalStateException("Too many players? A new game should have enough cards", empty);
				}
			}
		}
	}

	// Adds a new card to a player's hand after they have picking a card
	void giveOneCardToPlayer(Participant player) throws EmptyDeck
	{
		try
		{
			player.cards.add(deck.pop());
		}
		catch(NoSuchElementException empty)
		{
			throw new EmptyDeck();
		}
	}

	private boolean outOfTurns()
	{
		return deck.isEmpty();
	}

	private StoryTeller pickStoryTeller()
	{
		currentStoryTellerIndex = currentStoryTellerIndex++ % players.size();
		Participant participant = players.get(currentStoryTellerIndex);
		broadcast(new StoryTellerPicked(participant.player));
		return new StoryTeller(participant);
	}

	public void distributeScoresAndPickNewStoryTeller()
	{
		currentStoryTeller.distributeScores();
		if(outOfTurns())
		{
			Participant winner = null;
			int maxScore = 0;
			for(Participant player : players)
			{
				if(player.score > maxScore)
				{
					maxScore = player.score;
					winner = player;
				}
			}
			broadcast(new GameFinished(winner.player));
			// TODO: restart?
		}
		else
		{
			currentStoryTeller = pickStoryTeller();
		}
	}
}
