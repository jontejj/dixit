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
package com.github.jontejj.dixit.events;

import java.util.function.Consumer;

import com.github.jontejj.dixit.DixitCallback;
import com.github.jontejj.dixit.Participant;

public class JoinEvent extends GameEvent
{
	private final Participant playerWhoJoined;
	private final Consumer<GameEvent> listenerThatJoined;

	public JoinEvent(Participant playerWhoJoined, Consumer<GameEvent> listenerThatJoined)
	{
		this.playerWhoJoined = playerWhoJoined;
		this.listenerThatJoined = listenerThatJoined;
	}

	@Override
	public void execute(DixitCallback dixitView)
	{
		// TODO: optimize and only redraw updates scores/new players
		dixitView.gameInfoChanged();
	}

	@Override
	public String toString()
	{
		return playerWhoJoined + " joined";
	}

	public Consumer<GameEvent> getListenerThatJoined()
	{
		return listenerThatJoined;
	}
}
