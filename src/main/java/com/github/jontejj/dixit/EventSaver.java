/* Copyright 2018 jonatan.jonsson
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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

class EventSaver
{
	private final List<GameEvent> eventsForLatestGame = Collections.synchronizedList(new LinkedList<>());

	public void handleEvent(GameEvent event)
	{
		if(event instanceof JoinEvent)
		{
			JoinEvent joinEvent = (JoinEvent) event;
			for(GameEvent savedEvent : eventsForLatestGame)
			{
				joinEvent.getListenerThatJoined().accept(savedEvent);
			}
		}
		// else if(event instanceof ResetEvent)
		// {
		// eventsForLatestGame.clear();
		// }
		else
		{
			eventsForLatestGame.add(event);
		}

	}
}
