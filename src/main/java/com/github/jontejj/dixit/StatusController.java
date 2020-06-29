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

import java.util.concurrent.TimeUnit;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("")
public interface StatusController
{
	public enum Status
	{
		TIMEOUT,
		SHUTTING_DOWN,
		FLUSHED,
		TOO_MANY_FLUSHERS,
		GAME_DOES_NOT_EXIST
	}

	/**
	 * Waits (for the given timeout in {@link TimeUnit}) until the queue of events have been cleared.
	 * 
	 * @param gameId the game to wait for
	 * @param timeout how long to wait
	 * @param timeUnit in what unit to wait
	 * @return {@link Status} as soon as the queue is cleared or until the timeout occurs
	 */
	@GET
	@Path("{gameId}/flush-and-wait/{timeout}/{timeUnit}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Status wait(@PathParam("gameId") String gameId, @PathParam("timeout") Long timeout, @PathParam("timeUnit") TimeUnit timeUnit);
}
