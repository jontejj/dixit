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

public class RoundSummarizedEvent extends GameEvent
{

	private final RoundSummarization summarization;

	RoundSummarizedEvent(RoundSummarization summarization)
	{
		this.summarization = summarization;
	}

	@Override
	public void execute(DixitView view)
	{
		// refresh scores view as scores have been updated
		view.repaintGameInfoArea();
		// Show summary of the last story tellers round
		view.showSummarization(summarization);
	}

	@Override
	public String toString()
	{
		return "Round completed";
	}
}
