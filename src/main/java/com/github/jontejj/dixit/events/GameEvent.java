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

import java.util.Arrays;
import java.util.Optional;

import com.github.jontejj.dixit.DixitCallback;
import com.github.jontejj.dixit.TranslationKey;

public abstract class GameEvent
{
	private static final Object[] EMPTY_PARAMETERS = new Object[]{};

	public abstract void execute(DixitCallback callback);

	public void executeInternally(DixitCallback callback)
	{
		execute(callback);
	}

	public Optional<TranslationKey> translationKeyToDescribeEvent()
	{
		return Optional.empty();
	}

	public Object[] translationKeyParams()
	{
		return EMPTY_PARAMETERS;
	}

	public String toString()
	{
		return translationKeyToDescribeEvent() + " (" + Arrays.asList(translationKeyParams());
	}
}
