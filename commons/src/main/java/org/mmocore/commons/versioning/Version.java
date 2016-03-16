package org.mmocore.commons.versioning;

import java.io.File;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Version
{
	private static final Logger _log = LoggerFactory.getLogger(Version.class);

	private String _versionNumber;
	private int _versionRevision;
	private String _buildDate = "";
	private String _buildJdk = "";
	private int _buildNumber;

	public Version(Class<?> c)
	{
		File jarName = null;
		try
		{
			jarName = Locator.getClassSource(c);
			JarFile jarFile = new JarFile(jarName);

			Attributes attrs = jarFile.getManifest().getMainAttributes();

			setBuildJdk(attrs);
			setBuildDate(attrs);
			setBuildNumber(attrs);
			setVersionNumber(attrs);
			setVersionRevision(attrs);
		}
		catch(IOException e)
		{
			_log.error("Unable to get soft information\nFile name '" + (jarName == null ? "null" : jarName.getAbsolutePath()) + "' isn't a valid jar", e);
		}

	}

	/**
	 * @param attrs
	 */
	private void setVersionNumber(Attributes attrs)
	{
		String versionNumber = attrs.getValue("Implementation-Version");
		if(versionNumber != null)
			_versionNumber = versionNumber;
		else
			_versionNumber = "-1";
	}

	/**
	 * @param attrs
	 */
	private void setVersionRevision(Attributes attrs)
	{
		String versionRevision = attrs.getValue("Implementation-Revision");
		if(versionRevision != null && !versionRevision.isEmpty())
			_versionRevision = Integer.parseInt(versionRevision);
		else
			_versionRevision = -1;
	}

	/**
	 * @param attrs
	 */
	private void setBuildNumber(Attributes attrs)
	{
		String buildNumber = attrs.getValue("Implementation-Build");
		if(buildNumber != null && !buildNumber.isEmpty())
			_buildNumber = Integer.parseInt(buildNumber);
		else
			_buildNumber = -1;
	}

	/**
	 * @param attrs
	 */
	private void setBuildJdk(Attributes attrs)
	{
		String buildJdk = attrs.getValue("Build-Jdk");
		if(buildJdk != null)
			_buildJdk = buildJdk;
		else
		{
			buildJdk = attrs.getValue("Created-By");
			if(buildJdk != null)
				_buildJdk = buildJdk;
			else
				_buildJdk = "-1";
		}
	}

	/**
	 * @param attrs
	 */
	private void setBuildDate(Attributes attrs)
	{
		String buildDate = attrs.getValue("Build-Date");
		if(buildDate != null)
			_buildDate = buildDate;
		else
			_buildDate = "-1";
	}

	public String getVersionNumber()
	{
		return _versionNumber;
	}

	public int getVersionRevision()
	{
		return _versionRevision;
	}

	public int getBuildNumber()
	{
		return _buildNumber;
	}

	public String getBuildDate()
	{
		return _buildDate;
	}

	public String getBuildJdk()
	{
		return _buildJdk;
	}
}
