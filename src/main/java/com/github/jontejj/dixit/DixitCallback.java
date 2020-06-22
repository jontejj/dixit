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

public interface DixitCallback
{
	/**
	 * Adds a message from the system
	 */
	void addSystemMessage(String message);

	/**
	 * Adds a message from a user
	 */
	void addMessage(String message);

	/**
	 * Joined players changed, Scores changed etc
	 */
	void gameInfoChanged();

	void playerGuessedStoryTellerCard();

	void playerPickedMatchingCard(Player playerThatPicked);

	void summarizationReceived(RoundSummarization summarization);

	void sentenceCreated(String message);

	void storyTellerPicked(Player newStoryTeller);

	void addChatMessage(Player sender, String message);

}
