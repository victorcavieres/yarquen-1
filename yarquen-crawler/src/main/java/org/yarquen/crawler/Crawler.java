package org.yarquen.crawler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bixo.config.FetcherPolicy;
import bixo.config.FetcherPolicy.FetcherMode;
import bixo.config.UserAgent;
import bixo.utils.CrawlDirUtils;
import cascading.flow.Flow;

/**
 * Crawler entry point
 * 
 * @author Jorge Riquelme Santana
 * @date 23/05/2012
 * @version $Id$
 * 
 */
public class Crawler implements Callable<Void> {
	private static final Logger LOGGER = LoggerFactory.getLogger(Crawler.class);

	public static void main(String... args) throws Exception {
		final Crawler crawler = new Crawler();

		// parse args
		final CmdLineParser parser = new CmdLineParser(crawler.getConfig());

		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			LOGGER.error("Command line parsing error", e);
			printHelp(parser);
			System.exit(-1);
		}

		crawler.call();
		System.exit(0);
	}

	private static void printHelp(CmdLineParser parser) {
		parser.printUsage(System.err);
		System.exit(-1);
	}

	private Config config = new Config();

	@Override
	public Void call() throws Exception {
		final Path workingDirPath = new Path(config.getWorkingDir());

		final Configuration conf = new Configuration();
		final FileSystem fs = workingDirPath.getFileSystem(conf);

		// bootstrapping
		final EnvironmentBootstrapper envBootstrapper = new EnvironmentBootstrapper();
		envBootstrapper.setFileSystem(fs);
		envBootstrapper.setWorkingDirectory(workingDirPath);
		// seeds
		final List<String> seedUrls = readSeeds();
		envBootstrapper.setSeedUrls(seedUrls);
		// go
		envBootstrapper.bootstrap();

		final Path latestDirPath = CrawlDirUtils.findLatestLoopDir(fs,
				workingDirPath);
		if (latestDirPath == null) {
			throw new RuntimeException("wtf! ¬¬");
		}

		final UserAgent userAgent = new UserAgent(config.getAgentName(),
				Config.EMAIL_ADDRESS, Config.WEB_ADDRESS);

		final FetcherPolicy fetcherPolicy = getFetcherPolicy();

		final WorkflowBuilder workflowBuilder = new WorkflowBuilder();
		workflowBuilder.setFetcherPolicy(fetcherPolicy);
		workflowBuilder.setUserAgent(userAgent);

		// go!
		final int lastLoop = getLastLoopNumber(latestDirPath);
		LOGGER.debug("lastLoop number was {}", lastLoop);
		Path crawlDbPath = new Path(latestDirPath, Config.CRAWLDB_SUBDIR_NAME);
		for (int loop = lastLoop; loop < lastLoop + config.getLoops(); loop++) {
			final Path curLoopDirPath = CrawlDirUtils.makeLoopDir(fs,
					workingDirPath, loop + 1);
			final Flow flow = workflowBuilder
					.build(crawlDbPath, curLoopDirPath);
			flow.complete();

			// Update crawlDbPath to point to the latest crawl db
			crawlDbPath = new Path(curLoopDirPath, Config.CRAWLDB_SUBDIR_NAME);
		}

		return null;
	}

	public Config getConfig() {
		return config;
	}

	@SuppressWarnings("deprecation")
	private FetcherPolicy getFetcherPolicy() {
		final FetcherPolicy fetcherPolicy = new FetcherPolicy();
		fetcherPolicy.setCrawlDelay(Config.DEFAULT_CRAWL_DELAY_MS);
		fetcherPolicy.setMaxContentSize(Config.MAX_CONTENT_SIZE);
		fetcherPolicy.setFetcherMode(FetcherMode.IMPOLITE);

		// We only care about mime types that the Tika HTML parser can handle
		final Set<String> validMimeTypes = new HashSet<String>();
		final Set<MediaType> supportedTypes = new HtmlParser()
				.getSupportedTypes(new ParseContext());
		for (MediaType supportedType : supportedTypes) {
			final String mediaType = String.format("%s/%s",
					supportedType.getType(), supportedType.getSubtype());
			LOGGER.info("valid mediaType: {}", mediaType);
			validMimeTypes.add(mediaType);
		}
		fetcherPolicy.setValidMimeTypes(validMimeTypes);

		return fetcherPolicy;
	}

	private int getLastLoopNumber(Path latestDirPath) {
		final String sz = latestDirPath.getName().toString();
		final int div = sz.indexOf('-');
		final String lastLoopSz = sz.substring(0, div);
		return Integer.valueOf(lastLoopSz);
	}

	private List<String> readSeeds() throws IOException {
		final List<String> seeds = new ArrayList<String>();
		final LineIterator it = FileUtils.lineIterator(
				new File(config.getSeedsFile()), "UTF-8");
		try {
			while (it.hasNext()) {
				final String seed = it.nextLine();
				seeds.add(seed);
			}
			return seeds;
		} finally {
			LineIterator.closeQuietly(it);
		}
	}
}
