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
import java.util.List;
import java.util.Objects;

import com.vaadin.flow.server.ServiceException;

public class Participant
{
	public static class InvalidCardPicked extends ServiceException
	{
		public InvalidCardPicked(String message)
		{
			super(message);
		}
	}

	final Player player;
	final List<Card> cards = new ArrayList<>();

	int score = 0;

	Participant(Player player)
	{
		this.player = player;
	}

	public PickedCard pickCardThatMatchesTheStoryTellers(Card chosenCard) throws InvalidCardPicked
	{
		if(!cards.contains(chosenCard))
			throw new InvalidCardPicked(chosenCard + " was not among this player's cards.");
		cards.remove(chosenCard);
		return new PickedCard(chosenCard, this);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(player);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(!(obj instanceof Participant))
			return false;
		Participant other = (Participant) obj;
		return Objects.equals(player, other.player);
	}

	@Override
	public String toString()
	{
		return player.name;
	}
}
