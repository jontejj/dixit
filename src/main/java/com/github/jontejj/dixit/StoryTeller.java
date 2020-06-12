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

import com.github.jontejj.dixit.Participant.InvalidCardPicked;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Looks at 6 cards in her hand
 */
public class StoryTeller
{
	Participant participant;
	private List<PickedCard> givenCards = Lists.newCopyOnWriteArrayList();
	String sentence;
	private Card chosenCard;
	private Map<Participant, PickedCard> guessedCards = Maps.newConcurrentMap();

	StoryTeller(Participant participant)
	{
		this.participant = participant;
	}

	public void makeUpSentance(String aSentence, Card aChosenCard)
	{
		givenCards.add(new PickedCard(aChosenCard, participant));
		this.sentence = aSentence;
		this.chosenCard = aChosenCard;
	}

	public void givePickedCard(PickedCard card)
	{
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

	public void betOnStoryTellersCard(Card cardToBetOn, Participant byPlayer) throws InvalidCardPicked
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
	public void distributeScores()
	{
		int playersThatPickedTheRightCard = 0;
		for(Entry<Participant, PickedCard> playerPick : guessedCards.entrySet())
		{
			if(playerPick.getValue().equals(chosenCard))
			{
				playersThatPickedTheRightCard++;
			}
		}
		if(playersThatPickedTheRightCard != 0 && playersThatPickedTheRightCard != guessedCards.size())
		{
			participant.score += 3;
			for(Entry<Participant, PickedCard> playerPick : guessedCards.entrySet())
			{
				if(playerPick.getValue().equals(chosenCard))
				{
					playerPick.getKey().score += 3;
				}
			}
		}
		else
		{
			guessedCards.keySet().forEach(otherPlayer -> otherPlayer.score += 2);
		}
		for(PickedCard guessedCard : guessedCards.values())
		{
			if(!guessedCard.equals(chosenCard))
			{
				for(PickedCard givenCard : givenCards)
				{
					if(guessedCard.equals(givenCard))
					{
						givenCard.pickedBy.score += 1;
					}
				}
			}
		}
	}
}
