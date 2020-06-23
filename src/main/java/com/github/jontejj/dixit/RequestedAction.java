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

public enum RequestedAction
{
	/**
	 * The story teller should make up a sentence and pick a card from their hand
	 */
	MAKE_A_SENTENCE,
	/**
	 * Any kind of wait: wait on other players to pick cards for example
	 */
	WAIT,
	/**
	 * The other players should pick a card that matches the given sentence the best
	 */
	MATCH_CARD_TO_SENTENCE,
	/**
	 * The other players should pick the card they think the story teller picked
	 */
	GUESS_WHICH_CARD,
	/**
	 * Game is finished, all players can go home
	 */
	GAME_FINISHED_GO_HOME;

	public String asAttribute()
	{
		return name();
	}

	public static RequestedAction fromAttribute(String attribute)
	{
		return valueOf(attribute);
	}
}
