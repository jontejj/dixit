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

import java.util.function.Consumer;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.textfield.TextField;

public class ChatBox extends TextField
{
	private static final long serialVersionUID = 1L;

	public ChatBox(Consumer<String> onEnter)
	{
		this.addKeyUpListener(Key.ENTER, (e) -> {
			onEnter.accept(getValue());
		});
	}
}
