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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;

public class DownloadCards
{
	public static void main(String[] args) throws IOException
	{
		NetHttpTransport httpTransport = new NetHttpTransport.Builder().build();
		HttpRequestFactory rf = httpTransport.createRequestFactory();
		for(int i = 1; i < 200; i++)
		{
			HttpRequest httpRequest = rf.buildGetRequest(new GenericUrl("http://www.boiteajeux.net/jeux/dix/img/" + i + ".png"));
			try(OutputStream outputStream = new FileOutputStream(new File("src/main/resources/cards/" + i + ".png")))
			{
				HttpResponse response = httpRequest.execute();
				response.download(outputStream);
				System.out.println("Downloaded " + i);
			}
		}
	}
}
