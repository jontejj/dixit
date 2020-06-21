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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.github.jontejj.dixit.RoundSummarization.Scores;
import com.github.jontejj.dixit.exceptions.InvalidCardPicked;
import com.github.jontejj.dixit.exceptions.PlayerAlreadyGaveCard;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

/**
 * Looks at 6 cards in her hand
 */
public class StoryTeller
{
	Player player;
	private List<PickedCard> givenCards = Lists.newCopyOnWriteArrayList();
	String sentence;
	private Card chosenCard;
	private Map<Player, PickedCard> guessedCards = Maps.newConcurrentMap();

	StoryTeller(Player player)
	{
		this.player = player;
	}

	public void makeUpSentance(String aSentence, Card aChosenCard)
	{
		givenCards.add(new PickedCard(aChosenCard, player));
		this.sentence = aSentence;
		this.chosenCard = aChosenCard;
	}

	public void givePickedCard(PickedCard card) throws PlayerAlreadyGaveCard
	{
		Optional<PickedCard> alreadyGivenBySamePlayer = givenCards.stream()
				.filter(alreadyGivenCard -> alreadyGivenCard.pickedBy.equals(card.pickedBy)).findFirst();
		if(alreadyGivenBySamePlayer.isPresent())
			throw new PlayerAlreadyGaveCard();
		givenCards.add(card);
	}

	public int getNumberOfGivenCards()
	{
		return givenCards.size();
	}

	public List<PickedCard> getGivenCards()
	{
		ArrayList<PickedCard> copy = new ArrayList<>(givenCards);
		Collections.shuffle(copy);
		return copy;
	}

	public void betOnStoryTellersCard(Card cardToBetOn, Player byPlayer) throws InvalidCardPicked
	{
		PickedCard pickedCardToBetOn = givenCards.stream().filter(p -> p.equals(cardToBetOn)).findFirst()
				.orElseThrow(() -> new InvalidCardPicked(cardToBetOn + " was not among the picked cards"));
		guessedCards.put(byPlayer, pickedCardToBetOn);
	}

	public boolean hasCollectedAllGuesses()
	{
		// -1 because the story tellers card is also in the list
		return guessedCards.size() == givenCards.size() - 1;
	}

	/**
	 * If nobody or everybody finds the correct card, the storyteller scores 0, and each of the other players scores 2.
	 * Otherwise the storyteller and whoever found the correct answer score 3.
	 * Players score 1 point for every vote for their own card.
	 */
	public RoundSummarization summarizeRound()
	{
		Multimap<PickedCard, Player> guesses = LinkedHashMultimap.create();

		guessedCards.forEach((playerThatGuessed, card) -> {
			guesses.put(card, playerThatGuessed);
		});

		Multimap<Player, Integer> scores = ArrayListMultimap.create();

		int playersThatPickedTheRightCard = 0;
		for(Entry<Player, PickedCard> playerPick : guessedCards.entrySet())
		{
			if(playerPick.getValue().equals(chosenCard))
			{
				playersThatPickedTheRightCard++;
			}
		}
		if(playersThatPickedTheRightCard != 0 && playersThatPickedTheRightCard != guessedCards.size())
		{
			scores.put(player, 3);
			for(Entry<Player, PickedCard> playerPick : guessedCards.entrySet())
			{
				if(playerPick.getValue().equals(chosenCard))
				{
					scores.put(playerPick.getKey(), 3);
				}
			}
		}
		else
		{
			guessedCards.keySet().forEach(otherPlayer -> scores.put(otherPlayer, 2));
		}
		for(PickedCard guessedCard : guessedCards.values())
		{
			if(!guessedCard.equals(chosenCard))
			{
				for(PickedCard givenCard : givenCards)
				{
					if(guessedCard.equals(givenCard))
					{
						scores.put(givenCard.pickedBy, 1);
					}
				}
			}
		}
		return new RoundSummarization(player, new Scores(scores), guesses);
	}
}
