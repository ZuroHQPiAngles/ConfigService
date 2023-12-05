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

import org.apache.commons.lang3.StringUtils;
import org.piangles.backbone.services.Locator;
import org.piangles.backbone.services.logging.LoggingService;
import org.piangles.core.util.central.Environment;

import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;

class AWSParamStoreConfigSource implements ConfigSource
{
	private LoggingService logger = Locator.getInstance().getLoggingService();

	private Environment environment = null;
	private SsmClient ssmClient = null;
	
	AWSParamStoreConfigSource() throws Exception
	{
		environment = new Environment(); 
		try
		{
			ssmClient = SsmClient.builder().region(environment.getRegion()).build();
		}
		catch (Exception e)
		{
			String message = "AWS SSMClient could not be created. Reason: " + e.getMessage();
			logger.fatal(message, e);
			throw new Exception(message);
		}
	}
	
	public Configuration retrieveConfiguration(String componentId) throws ConfigException
	{
		Configuration configuration = null;
		
		GetParametersByPathResponse parametersByPathResponse = null;
		GetParametersByPathRequest parametersByPathRequest = null;

		try
		{
			String pathPrefix = "/" + environment.identifyEnvironment() + "/" + componentId;
			
			logger.info("ConfigService:Searching for pathPrefix : " + pathPrefix);

			

			/**GetParametersByPath is a paged operation. 
			 *After each call you must retrieve NextToken from the result object, and if it's 
			 *not null and not empty you must make another call with it added to the request.
			**/
			do
			{
				parametersByPathRequest = GetParametersByPathRequest.builder()
																	.path(pathPrefix)
																	.recursive(true)
																	.withDecryption(true)
																	.maxResults(10)
																	.nextToken((parametersByPathResponse != null)?parametersByPathResponse.nextToken():null)
																	.build();
				
				parametersByPathResponse = ssmClient.getParametersByPath(parametersByPathRequest);

				if (parametersByPathResponse.parameters().size() != 0)
				{
					if (configuration == null)
					{
						configuration = new Configuration();
					}
					for (int i = 0; i < parametersByPathResponse.parameters().size(); ++i)
					{
						Parameter parameter = parametersByPathResponse.parameters().get(i);

						String[] uriSegments = parameter.name().split("/");

						String parameterName = uriSegments[uriSegments.length - 1];

						configuration.addNameValue(parameterName, parameter.value());
					}
				}
			} while (StringUtils.isNotBlank(parametersByPathResponse.nextToken()));
		}
		catch (Exception e)
		{
			throw new ConfigException(e.getMessage(), e);
		}
		
		return configuration;
	}
}
