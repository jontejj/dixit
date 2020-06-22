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

import static java.util.Locale.ENGLISH;
import static java.util.ResourceBundle.getBundle;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;
import com.vaadin.flow.i18n.I18NProvider;

@Component
public class InternationalizationProvider implements I18NProvider
{
	private static final Locale SWEDISH = Locale.forLanguageTag("sv");

	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(InternationalizationProvider.class);

	public static final String RESOURCE_BUNDLE_NAME = "vaadinapp";

	private static final ResourceBundle RESOURCE_BUNDLE_EN = getBundle(RESOURCE_BUNDLE_NAME, ENGLISH);
	private static final ResourceBundle RESOURCE_BUNDLE_SV = getBundle(RESOURCE_BUNDLE_NAME, SWEDISH);

	private static final List<Locale> providedLocales = ImmutableList.of(ENGLISH, SWEDISH);

	@Override
	public List<Locale> getProvidedLocales()
	{
		return providedLocales;
	}

	@Override
	public String getTranslation(String key, Locale locale, Object ... params)
	{
		ResourceBundle resourceBundle = RESOURCE_BUNDLE_EN;
		if(SWEDISH.getLanguage().equals(locale.getLanguage()))
		{
			resourceBundle = RESOURCE_BUNDLE_SV;
		}

		if(!resourceBundle.containsKey(key))
		{
			LOG.warn("missing resource key (i18n) " + key);
			return key + " - " + locale + "(" + Arrays.asList(params) + ")";
		}
		return (resourceBundle.containsKey(key)) ? MessageFormat.format(resourceBundle.getString(key), params) : key;
	}

}
