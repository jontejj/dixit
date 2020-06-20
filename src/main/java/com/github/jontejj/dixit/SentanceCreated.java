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

import com.github.jontejj.dixit.DixitView.Selectable;
import com.github.jontejj.dixit.Participant.InvalidCardPicked;
import com.vaadin.flow.component.notification.Notification;

public class SentanceCreated extends GameEvent
{
	private final String message;

	SentanceCreated(String message)
	{
		this.message = message;
	}

	@Override
	public void execute(DixitView view)
	{
		if(view.me.player.equals(view.currentGame.currentStoryTeller.player))
		{
			view.addSystemMessage("Roll your thumbs while other players are picking a card to match your sentence");
		}
		else
		{
			Player storyTeller = view.currentGame.currentStoryTeller.player;
			view.addSystemMessage(storyTeller + " says: " + message + ". Pick one of your cards that you think matches");
			view.showCardsWithPicker(view.me.cards, card -> {
				try
				{
					// TODO: these operations should be done inside of Dixit
					PickedCard pickedCard = view.me.pickCardThatMatchesTheStoryTellers(card);
					view.currentGame.currentStoryTeller.givePickedCard(pickedCard);
					view.currentGame.broadcast(new PlayerPickedMatchingCard(view.me.player));
					view.me.cards.remove(card);
					view.currentGame.giveOneCardToPlayer(view.me);
				}
				catch(InvalidCardPicked | EmptyDeck | PlayerAlreadyGaveCard error)
				{
					Notification.show(error.getMessage());
				}
			}, Selectable.YES);
		}
	}

	@Override
	public String toString()
	{
		return "";
	}
}
