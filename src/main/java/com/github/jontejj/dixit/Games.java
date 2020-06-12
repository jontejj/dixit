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

import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;

@Service
public class Games
{
	// TODO(jontejj): clean up map for old boards
	private static ConcurrentMap<String, Dixit> ongoingGames = Maps.newConcurrentMap();

	public Dixit getOrCreate(String gameId)
	{
		return ongoingGames.computeIfAbsent(gameId, (game) -> new Dixit());
	}
}
