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
import org.springframework.stereotype.Component;

@Component
public class StatusControllerImpl implements StatusController
{
	private final Games games;

	public StatusControllerImpl(@Autowired Games games)
	{
		this.games = games;

	}

	@Override
	public Status wait(String gameId, Long timeout, TimeUnit timeUnit)
	{
		Optional<Dixit> game = games.get(gameId);
		if(game.isEmpty())
			return Status.GAME_DOES_NOT_EXIST;
		return game.get().flushEvents(timeUnit, timeout);
	}

}
