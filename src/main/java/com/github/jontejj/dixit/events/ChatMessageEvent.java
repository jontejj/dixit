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

import com.github.jontejj.dixit.DixitCallback;
import com.github.jontejj.dixit.Player;

public class ChatMessageEvent extends GameEvent
{
	private final Player sender;
	private final String message;

	public ChatMessageEvent(Player sender, String message)
	{
		this.sender = sender;
		this.message = message;

	}

	public void executeInternally(DixitCallback dixit)
	{
		dixit.addMessage(toString());
		execute(dixit);
	}

	@Override
	public void execute(DixitCallback dixit)
	{

	}

	@Override
	public String toString()
	{
		return sender + ": " + message;
	}
}
