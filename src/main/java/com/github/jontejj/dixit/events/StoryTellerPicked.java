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
package com.github.jontejj.dixit.events;

import java.util.Optional;

import com.github.jontejj.dixit.DixitCallback;
import com.github.jontejj.dixit.Player;
import com.github.jontejj.dixit.TranslationKey;

public class StoryTellerPicked extends GameEvent
{
	private final Player newStoryTeller;

	public StoryTellerPicked(Player newStoryTeller)
	{
		this.newStoryTeller = newStoryTeller;
	}

	@Override
	public void execute(DixitCallback callback)
	{
		callback.storyTellerPicked(newStoryTeller);
	}

	@Override
	public Optional<TranslationKey> translationKeyToDescribeEvent()
	{
		return Optional.of(TranslationKey.STORY_TELLER_PICKED);
	}

	@Override
	public Object[] translationKeyParams()
	{
		return new Object[]{newStoryTeller};
	}
}
