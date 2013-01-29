package org.yarquen.crawler;

import org.kohsuke.args4j.Option;

/**
 * Crawler configuration
 * 
 * @author Jorge Riquelme Santana
 * @date 23/05/2012
 * @version $Id$
 * 
 */
public class Config {
	public static final int CONNECTION_TIMEOUT = 10 * 1000;
	public static final String CONTENT_SUBDIR_NAME = "content";
	public static final int CRAWL_STACKSIZE_KB = 128;
	public static final String CRAWLDB_SUBDIR_NAME = "crawldb";
	public static final long DEFAULT_CRAWL_DELAY_MS = 10 * 1000L;
	public static final int DEFAULT_NUM_THREADS_CLUSTER = 100;
	public static final int DEFAULT_NUM_THREADS_LOCAL = 2;
	public static final String EMAIL_ADDRESS = "jriquelme@totex.cl";
	public static final int MAX_CONTENT_SIZE = 256 * 1024;
	public static final int MAX_RETRIES = 2;
	public static final long MILLISECONDS_PER_MINUTE = 60 * 1000L;
	public static final String RESULTS_SUBDIR_NAME = "results";
	public static final int SOCKET_TIMEOUT = 10 * 1000;
	public static final String STATUS_SUBDIR_NAME = "status";
	public static final String WEB_ADDRESS = "http://github.com/jriquelme/yarquen";

	private String agentName = "yarquen";
	private int loops = 1;
	private String seedsFile = "seeds.txt";
	private String workingDir = "/home/totex/local/tmp/cc-crawler";

	public String getAgentName() {
		return agentName;
	}

	public int getLoops() {
		return loops;
	}

	public String getSeedsFile() {
		return seedsFile;
	}

	public String getWorkingDir() {
		return workingDir;
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

	@Option(name = "-loops", usage = "crawling loops", required = false)
	public void setLoops(int loops) {
		this.loops = loops;
	}

	@Option(name = "-seeds", usage = "path to seeds file", required = false)
	public void setSeedsFile(String seedsFile) {
		this.seedsFile = seedsFile;
	}

	@Option(name = "-workingdir", usage = "path to directory for fetching", required = false)
	public void setWorkingDir(String workingDir) {
		this.workingDir = workingDir;
	}
}
