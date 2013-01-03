package org.yarquen.crawler;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yarquen.crawler.datum.CrawlDbDatum;

import bixo.urls.SimpleUrlNormalizer;
import bixo.utils.CrawlDirUtils;
import cascading.scheme.TextLine;
import cascading.tap.Hfs;
import cascading.tap.Tap;
import cascading.tuple.TupleEntryCollector;


import com.bixolabs.cascading.HadoopUtils;

/**
 * Environment bootstrapper
 * 
 * @author Jorge Riquelme Santana
 * @date 23/05/2012
 * @version $Id$
 * 
 */
@SuppressWarnings("deprecation")
public class EnvironmentBootstrapper
{
	private static final Logger LOGGER = LoggerFactory
			.getLogger(EnvironmentBootstrapper.class);

	private FileSystem fileSystem;
	private List<String> seedUrls;
	private Path workingDirectory;

	public void bootstrap() throws IOException, InterruptedException
	{
		Path loopDirPath = CrawlDirUtils.findLatestLoopDir(fileSystem,
				workingDirectory);
		if (loopDirPath == null)
		{
			// Create 0-loop directory
			loopDirPath = CrawlDirUtils.makeLoopDir(fileSystem,
					workingDirectory, 0);
			final Path crawlDbPath = new Path(loopDirPath,
					Config.CRAWLDB_SUBDIR_NAME);

			// write seeds
			writeSeedUrls(crawlDbPath);
		}
		else
		{
			LOGGER.info("starting from {}", loopDirPath);
		}
	}

	public void setFileSystem(FileSystem fileSystem)
	{
		this.fileSystem = fileSystem;
	}

	public void setSeedUrls(List<String> seedUrls)
	{
		this.seedUrls = seedUrls;
	}

	public void setWorkingDirectory(Path workingDirectory)
	{
		this.workingDirectory = workingDirectory;
	}

	private void writeSeedUrls(Path crawlDbPath) throws IOException,
			InterruptedException
	{
		final SimpleUrlNormalizer normalizer = new SimpleUrlNormalizer();
		final JobConf defaultJobConf = HadoopUtils.getDefaultJobConf();

		TupleEntryCollector writer = null;
		try
		{
			final Tap urlSink = new Hfs(new TextLine(), crawlDbPath.toString(),
					true);
			writer = urlSink.openForWrite(defaultJobConf);

			LOGGER.info("writing seeds...");
			for (String seedUrl : seedUrls)
			{
				final String normalizedUrl = normalizer.normalize(seedUrl);
				LOGGER.trace("writing seed {}", normalizedUrl);
				final CrawlDbDatum datum = new CrawlDbDatum(normalizedUrl);
				writer.add(datum.getTuple());
			}

			writer.close();
		}
		catch (IOException e)
		{
			HadoopUtils.safeRemove(crawlDbPath.getFileSystem(defaultJobConf),
					crawlDbPath);
			throw e;
		}
		finally
		{
			if (writer != null)
			{
				writer.close();
			}
		}
	}
}
