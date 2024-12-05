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

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.piangles.backbone.services.Locator;
import org.piangles.backbone.services.logging.LoggingService;
import org.piangles.core.services.AuditDetails;
import org.piangles.core.util.central.CentralClient;

public class ConfigServiceImpl
{
	private static final String CONFIG_SOURCE = "ConfigSource";

	private ConfigSource configSource = null;

	Properties tier1Config;
	private LoggingService logger = Locator.getInstance().getLoggingService();

	public ConfigServiceImpl() throws Exception
	{
		tier1Config = CentralClient.getInstance().tier1Config(ConfigService.NAME);

		String configSourceValue = tier1Config.getProperty(CONFIG_SOURCE);
		if (StringUtils.isBlank(configSourceValue) || "AWS".equals(configSourceValue))
		{
			configSource = new AWSParamStoreConfigSource(); //Default
		}
		else if ("Database".equals(configSourceValue))
		{
			configSource = new DBBasedConfigSource();
		}
		else if ("File".equals(configSourceValue))
		{
			configSource = new FileBasedConfigSource();
		}
		else
		{
			throw new Exception("Invalid ConfigSource: " + configSourceValue);
		}
	}

	public Configuration getConfiguration(AuditDetails details, String componentId) throws ConfigException
	{
		Configuration configuration = null;

		/**
		 * TODO We should record audit for configuration.
		 * configDAO.recordAudit(context);
		 *
		 */
		configuration = configSource.retrieveConfiguration(componentId);
		if (configuration == null) {
			throw new ConfigException("Unable to find configuration for componentId : " + componentId);
		}
		//add secrets to configuration
        try {
			Properties secretProperties = CentralClient.getInstance().secrets();
			if(secretProperties != null) {
				logger.info("ConfigServiceImpl: adding secret properties with size : " + secretProperties.size());
				for (Object key : secretProperties.keySet()) {
					configuration.addNameValue(key.toString(), secretProperties.getProperty(key.toString()));
				}
			}
        } catch (Exception e) {
			logger.error("ConfigServiceImpl: Unable to retrieve secrets : " + e.getMessage());
        }

		return configuration;
	}
}
