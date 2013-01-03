package org.yarquen.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import org.yarquen.crawler.datum.AnalyzedDatum;
import org.yarquen.crawler.datum.CrawlDbDatum;
import org.yarquen.crawler.datum.LinkDatum;
import org.yarquen.crawler.filters.AnalyzeHtml;
import org.yarquen.crawler.filters.ArticleXmlEmitter;
import org.yarquen.crawler.filters.ArticlesFromAnalyzedDatum;
import org.yarquen.crawler.filters.CreateLinkDatumFromOutlinksFunction;
import org.yarquen.crawler.filters.CreateUrlDatumFromCrawlDbDatum;
import org.yarquen.crawler.filters.SplitFetchedUnfetchedSSCrawlDatums;
import org.yarquen.crawler.filters.UpdateCrawlDbBuffer;
import org.yarquen.crawler.scorer.CompositeLinkScorer;
import org.yarquen.crawler.scorer.dzone.DzoneScorer;
import org.yarquen.crawler.scorer.infoq.InfoqScorer;

import bixo.config.FetcherPolicy;
import bixo.config.ParserPolicy;
import bixo.config.UserAgent;
import bixo.datum.StatusDatum;
import bixo.fetcher.SimpleHttpFetcher;
import bixo.operations.BaseScoreGenerator;
import bixo.parser.SimpleParser;
import bixo.pipes.FetchPipe;
import bixo.pipes.ParsePipe;
import cascading.flow.Flow;
import cascading.flow.FlowConnector;
import cascading.pipe.CoGroup;
import cascading.pipe.Each;
import cascading.pipe.Every;
import cascading.pipe.GroupBy;
import cascading.pipe.Pipe;
import cascading.pipe.cogroup.OuterJoin;
import cascading.scheme.TextDelimited;
import cascading.scheme.TextLine;
import cascading.tap.Hfs;
import cascading.tap.Tap;
import cascading.tuple.Fields;

import com.bixolabs.cascading.HadoopUtils;
import com.bixolabs.cascading.SplitterAssembly;

/**
 * Cascading workflow builder
 * 
 * @author Jorge Riquelme Santana
 * @date 23/05/2012
 * @version $Id$
 * 
 */
@SuppressWarnings("deprecation")
public class WorkflowBuilder
{
	private static final long MAX_DISTRIBUTED_FETCH = 100;
	// Max URLs to fetch in local vs. distributed mode.
	private static final long MAX_LOCAL_FETCH = 100; // was 5

	private FetcherPolicy fetcherPolicy;
	private UserAgent userAgent;

	public Flow build(Path crawlDbPath, Path curLoopDirPath)
			throws IOException, InterruptedException
	{
		// Fetch at most 200 pages, max size of 128K, complete mode, from the
		// current dir.
		// HTML only.

		// We want to extract the cleaned up HTML, and pass that to the parser,
		// which will
		// be specified via options.getAnalyzer. From this we'll get outlinks,
		// page score, and
		// any results.

		JobConf conf = HadoopUtils.getDefaultJobConf(Config.CRAWL_STACKSIZE_KB);
		boolean isLocal = HadoopUtils.isJobLocal(conf);
		int numReducers = 1; // we always want to use a single reducer, to avoid
								// contention
		conf.setNumReduceTasks(numReducers);
		conf.setInt("mapred.min.split.size", 64 * 1024 * 1024);
		Properties props = HadoopUtils.getDefaultProperties(
				WorkflowBuilder.class, false, conf);
		FileSystem fs = crawlDbPath.getFileSystem(conf);

		// Input : the crawldb
		if (!fs.exists(crawlDbPath))
		{
			throw new RuntimeException("CrawlDb not found");
		}

		Tap inputSource = new Hfs(new TextDelimited(CrawlDbDatum.FIELDS, "\t",
				CrawlDbDatum.TYPES), crawlDbPath.toString());
		Pipe importPipe = new Pipe("import pipe");

		// Split into tuples that are to be fetched and that have already been
		// fetched
		SplitterAssembly splitter = new SplitterAssembly(importPipe,
				new SplitFetchedUnfetchedSSCrawlDatums());

		Pipe finishedDatumsFromDb = new Pipe("finished datums from db",
				splitter.getRHSPipe());
		Pipe urlsToFetchPipe = splitter.getLHSPipe();

		// Limit to MAX_DISTRIBUTED_FETCH if running in real cluster,
		// or MAX_LOCAL_FETCH if running locally. So first we sort the entries
		// from high to low by links score.
		// TODO add unit test
		urlsToFetchPipe = new GroupBy(urlsToFetchPipe, new Fields(
				CrawlDbDatum.LINKS_SCORE_FIELD), true);
		long maxToFetch = HadoopUtils.isJobLocal(conf) ? MAX_LOCAL_FETCH
				: MAX_DISTRIBUTED_FETCH;
		urlsToFetchPipe = new Each(urlsToFetchPipe,
				new CreateUrlDatumFromCrawlDbDatum(maxToFetch));

		// link scorer
		final CompositeLinkScorer scorer = new CompositeLinkScorer();
		final List<BaseScoreGenerator> scorers = new ArrayList<BaseScoreGenerator>();
		scorers.add(new DzoneScorer());
		scorers.add(new InfoqScorer());
		// TODO: ojo pestaña y ceja aca!, con el isGoodDomain
		// scorers.add(new PotentialCOLinkScorer());
		scorer.setLinkScoreGenerators(scorers);

		// Create the sub-assembly that runs the fetch job
		int maxThreads = isLocal ? Config.DEFAULT_NUM_THREADS_LOCAL
				: Config.DEFAULT_NUM_THREADS_CLUSTER;
		SimpleHttpFetcher fetcher = new SimpleHttpFetcher(maxThreads,
				fetcherPolicy, userAgent);
		fetcher.setMaxRetryCount(Config.MAX_RETRIES);
		fetcher.setSocketTimeout(Config.SOCKET_TIMEOUT);
		fetcher.setConnectionTimeout(Config.CONNECTION_TIMEOUT);

		FetchPipe fetchPipe = new FetchPipe(urlsToFetchPipe, scorer, fetcher,
				numReducers);
		Pipe statusPipe = new Pipe("status pipe", fetchPipe.getStatusTailPipe());
		// Pipe contentPipe = new Pipe("content pipe",
		// fetchPipe.getContentTailPipe());
		// contentPipe = TupleLogger.makePipe(contentPipe, true);

		// Create a parser that returns back the raw HTML (cleaned up by Tika)
		// as the parsed content.
		SimpleParser parser = new SimpleParser(new ParserPolicy(), true);
		ParsePipe parsePipe = new ParsePipe(fetchPipe.getContentTailPipe(),
				parser);

		Pipe analyzerPipe = new Pipe("analyzer pipe");
		analyzerPipe = new Each(parsePipe.getTailPipe(), new AnalyzeHtml());

		Pipe outlinksPipe = new Pipe("outlinks pipe", analyzerPipe);
		outlinksPipe = new Each(outlinksPipe,
				new CreateLinkDatumFromOutlinksFunction());

		// get articles
		Pipe articlesPipe = new Pipe("articles pipe", analyzerPipe);
		articlesPipe = new Each(articlesPipe, new ArticlesFromAnalyzedDatum());

		// write xml
		Pipe articlesXmlPipe = new Pipe("articles xml pipe", articlesPipe);
		articlesXmlPipe = new Each(articlesXmlPipe,
				new ArticleXmlEmitter());

		// Group the finished datums, the skipped datums, status, outlinks
		Pipe updatePipe = new CoGroup("update pipe", Pipe.pipes(
				finishedDatumsFromDb, statusPipe, analyzerPipe, outlinksPipe),
				Fields.fields(new Fields(CrawlDbDatum.URL_FIELD), new Fields(
						StatusDatum.URL_FN), new Fields(AnalyzedDatum.URL),
						new Fields(LinkDatum.URL_FN)), null, new OuterJoin());
		updatePipe = new Every(updatePipe, new UpdateCrawlDbBuffer(),
				Fields.RESULTS);

		// output : loop dir specific crawldb
		Path outCrawlDbPath = new Path(curLoopDirPath,
				Config.CRAWLDB_SUBDIR_NAME);
		Tap crawlDbSink = new Hfs(new TextLine(), outCrawlDbPath.toString());
		// Status,
		Path statusDirPath = new Path(curLoopDirPath, Config.STATUS_SUBDIR_NAME);
		Tap statusSink = new Hfs(new TextLine(), statusDirPath.toString());
		// Content
		// Path contentDirPath = new Path(curLoopDirPath,
		// Config.CONTENT_SUBDIR_NAME);
		// Tap contentSink = new Hfs(new SequenceFile(FetchedDatum.FIELDS),
		// contentDirPath.toString());
		// articles
		Path resultsDirPath = new Path(curLoopDirPath,
				Config.RESULTS_SUBDIR_NAME);
		Tap articlesSink = new Hfs(new TextLine(), resultsDirPath.toString());

		// Create the output map that connects each tail pipe to the appropriate
		// sink.
		Map<String, Tap> sinkMap = new HashMap<String, Tap>();
		sinkMap.put(updatePipe.getName(), crawlDbSink);
		sinkMap.put(statusPipe.getName(), statusSink);
		// sinkMap.put(contentPipe.getName(), contentSink);
		sinkMap.put(articlesXmlPipe.getName(), articlesSink);

		FlowConnector flowConnector = new FlowConnector(props);
		// contentPipe removed
		Flow flow = flowConnector.connect(inputSource, sinkMap, updatePipe,
				statusPipe, articlesXmlPipe);

		flow.writeDOT("/home/totex/daflow.dot");
		return flow;
	}

	private Pipe buildContentPipe()
	{
		return null;
	}

	public void setFetcherPolicy(FetcherPolicy fetcherPolicy)
	{
		this.fetcherPolicy = fetcherPolicy;
	}

	public void setUserAgent(UserAgent userAgent)
	{
		this.userAgent = userAgent;
	}
}
