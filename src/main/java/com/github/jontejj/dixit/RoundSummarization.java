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

import java.util.Collection;
import java.util.function.BiConsumer;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

public final class RoundSummarization
{
	private final Player storyTeller;
	private final Scores scores;
	private final Multimap<PickedCard, Player> guesses = LinkedHashMultimap.create();

	RoundSummarization(Player storyTeller, Scores scores, Multimap<PickedCard, Player> guesses)
	{
		this.storyTeller = storyTeller;
		this.scores = scores;
		this.guesses.putAll(guesses);
	}

	public int getTotalScoreIncreseForPlayer(Player x)
	{
		return scores.getTotalScoreIncrease(x);
	}

	public void forEachPickedCard(BiConsumer<PickedCard, Collection<Player>> consumer)
	{
		guesses.keySet().forEach(card -> consumer.accept(card, guesses.get(card)));
	}

	public Player getStoryTeller()
	{
		return storyTeller;
	}

	public static final class Scores
	{
		private final Multimap<Player, Integer> scores = LinkedHashMultimap.create();

		Scores(Multimap<Player, Integer> scores)
		{
			this.scores.putAll(scores);
		}

		public int getTotalScoreIncrease(Player player)
		{
			return scores.get(player).stream().reduce((l, r) -> l + r).orElse(0);
		}
	}
}
