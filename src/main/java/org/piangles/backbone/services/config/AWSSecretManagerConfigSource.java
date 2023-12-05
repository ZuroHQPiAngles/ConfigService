/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
 
package org.piangles.backbone.services.config;

import org.piangles.backbone.services.Locator;
import org.piangles.backbone.services.logging.LoggingService;
import org.piangles.core.util.central.Environment;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

class AWSSecretManagerConfigSource implements ConfigSource
{
	private LoggingService logger = Locator.getInstance().getLoggingService();

	private Environment environment = null;
	private SecretsManagerClient secretsManagerClient = null;
	private String secretName = "_encrypt-token";
	
	AWSSecretManagerConfigSource() throws Exception
	{
		environment = new Environment(); 
		try
		{
			
			secretsManagerClient = SecretsManagerClient
					.builder()
					.region(environment.getRegion())
					.credentialsProvider(ProfileCredentialsProvider.create())
					.build();
		}
		catch (Exception e)
		{
			String message = "AWS SecretsManagerClient could not be created. Reason: " + e.getMessage();
			logger.fatal(message, e);
			throw new Exception(message);
		}
	}
	
	public Configuration retrieveConfiguration(String componentId) throws ConfigException
	{
		Configuration configuration;
		
		try
		{
			secretName = environment.identifyEnvironment() + secretName;

			GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
					.secretId(secretName)
					.build();

			GetSecretValueResponse valueResponse = secretsManagerClient.getSecretValue(valueRequest);
			String secret = valueResponse.secretString();

			configuration = new Configuration();
			configuration.addNameValue(secretName, secret);
		}
		catch (Exception e)
		{
			throw new ConfigException(e.getMessage(), e);
		}
		
		return configuration;
	}
}
