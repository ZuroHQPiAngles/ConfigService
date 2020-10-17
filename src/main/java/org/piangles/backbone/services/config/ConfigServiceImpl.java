package org.piangles.backbone.services.config;

import org.piangles.backbone.services.config.dao.ConfigDAO;
import org.piangles.backbone.services.config.dao.ConfigDAOImpl;

import org.piangles.backbone.services.config.ConfigException;
import org.piangles.backbone.services.config.Configuration;
import org.piangles.core.dao.DAOException;
import org.piangles.core.services.Context;

public class ConfigServiceImpl
{
	private ConfigDAO configDAO = null;

	public ConfigServiceImpl() throws Exception
	{
		configDAO = new ConfigDAOImpl();
	}

	public Configuration getConfiguration(Context context, String componentId) throws ConfigException
	{
		Configuration configuration = null;
		try
		{
			/**
			 * TODO We should record audit for configuration.
			 * configDAO.recordAudit(context);
			 * 
			 */
			configuration = configDAO.retrieveConfiguration(componentId);
		}
		catch (DAOException e)
		{
			throw new ConfigException(e);
		}

		if (configuration == null)
		{
			throw new ConfigException("Unable to find configuration for componentId : " + componentId);
		}

		return configuration;
	}
}