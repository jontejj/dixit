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

public enum TranslationKey
{
	/**
	 * new story teller
	 */
	STORY_TELLER_PICKED,
	SENTENCE,
	MAKE_UP_A_SENTENCE_AND_PICK_A_CARD,
	CARD_PICKED_BUT_NO_SENTENCE,
	SENTENCE_GIVEN_BUT_NO_CARD,
	OTHER_PLAYERS_PICKED_THESE_CARDS,
	GUESS_WHICH_CARD,
	WAITING_FOR_OTHER_PLAYERS_TO_PICK_A_CARD,
	ROLL_YOUR_THUMBS_STORY_TELLER,
	ROLL_YOUR_THUMBS_GUESSER,
	/**
	 * name, message
	 */
	STORY_TELLER_SAYS,
	/**
	 * message
	 */
	ERROR,
	SEND_CHAT_MESSAGE,
	/**
	 * name
	 */
	MY_NAME_IS,
	/**
	 * nr of cards
	 */
	CARDS_REMAINING,
	SCORES,
	SET_NAME,
	MY_NAME_PROMPT,
	NR_OF_PLAYERS_PROMPT,
	CREATE_GAME_BUTTON,
	JOIN_GAME_BUTTON,
	ROUND_SUMMARIZATION,
	/**
	 * player who played the card
	 */
	CARD_PLAYED_BY,
	/**
	 * collection of playersWhoGuessedIt
	 */
	CARD_PICKED_BY,
	CLOSE_ROUND_SUMMARIZATION,
	ROUND_COMPLETED,
	/**
	 * player that picked a card
	 */
	PLAYER_PICKED_CARD,
	/**
	 * player that guessed
	 */
	PLAYER_GUESSED_CARD,
	/**
	 * player who left
	 */
	PLAYER_LEFT,
	/**
	 * player who joined
	 */
	PLAYER_JOINED,
	/**
	 * Winner
	 */
	GAME_FINISHED,
	SEND_SENTENCE;

	public String key()
	{
		return name();
	}
}
