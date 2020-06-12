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

public class PlayerGuessedStoryTellerCard extends GameEvent
{

	private final Player playerThatGuessed;

	public PlayerGuessedStoryTellerCard(Player playerThatGuessed)
	{
		this.playerThatGuessed = playerThatGuessed;
	}

	@Override
	public void execute(DixitView view)
	{
		if(view.me.player.equals(view.currentGame.currentStoryTeller.participant.player))
		{
			if(view.currentGame.currentStoryTeller.hasCollectedAllGuesses())
			{
				view.currentGame.distributeScoresAndPickNewStoryTeller();
			}
		}
	}

	@Override
	public String toString()
	{
		return playerThatGuessed + " guessed on a card";
	}
}
