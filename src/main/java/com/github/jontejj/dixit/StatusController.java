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

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController
{
	public enum Status
	{
		TIMEOUT,
		SHUTTING_DOWN,
		FLUSHED,
		TOO_MANY_FLUSHERS,
		GAME_DOES_NOT_EXIST
	}

	private final Games games;

	public StatusController(@Autowired Games games)
	{
		this.games = games;

	}

	/**
	 * Waits (for the given timeout in {@link TimeUnit}) until the queue of events have been cleared.
	 * 
	 * @param gameId the game to wait for
	 * @param timeout how long to wait
	 * @param timeUnit in what unit to wait
	 * @return {@link Status} as soon as the queue is cleared or until the timeout occurs
	 */
	@GetMapping("dixit/{gameId}/flush-and-wait/{timeout}/{timeUnit}")
	public Status wait(@PathVariable String gameId, @PathVariable Long timeout, @PathVariable TimeUnit timeUnit)
	{
		Optional<Dixit> game = games.get(gameId);
		if(game.isEmpty())
			return Status.GAME_DOES_NOT_EXIST;
		return game.get().flushEvents(timeUnit, timeout);
	}
}
