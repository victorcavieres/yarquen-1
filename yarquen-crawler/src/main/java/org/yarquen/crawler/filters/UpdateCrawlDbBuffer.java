package org.yarquen.crawler.filters;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yarquen.crawler.CustomFields;
import org.yarquen.crawler.datum.AnalyzedDatum;
import org.yarquen.crawler.datum.CrawlDbDatum;
import org.yarquen.crawler.datum.LinkDatum;

import bixo.datum.StatusDatum;
import bixo.datum.UrlStatus;
import cascading.flow.FlowProcess;
import cascading.flow.hadoop.HadoopFlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.Buffer;
import cascading.operation.BufferCall;
import cascading.operation.OperationCall;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;

import com.bixolabs.cascading.LoggingFlowProcess;
import com.bixolabs.cascading.LoggingFlowReporter;
import com.bixolabs.cascading.NullContext;

/**
 * 
 * @author Jorge Riquelme Santana
 * @date 23/05/2012
 * @version $Id$
 * 
 */
@SuppressWarnings("serial")
public class UpdateCrawlDbBuffer extends BaseOperation<NullContext> implements
		Buffer<NullContext> {
	private static final Fields ANALYZEDDATUM_URL_FIELD = new Fields(
			AnalyzedDatum.URL);
	private static final Fields CRAWLDBDATUM_URL_FIELD = new Fields(
			CrawlDbDatum.URL_FIELD);
	private static final Logger LOGGER = LoggerFactory
			.getLogger(UpdateCrawlDbBuffer.class);
	private static final Fields STATUSDATUM_URL_FIELD = new Fields(
			StatusDatum.URL_FN);
	private LoggingFlowProcess _flowProcess;

	public UpdateCrawlDbBuffer() {
		super(CrawlDbDatum.FIELDS);
	}

	@Override
	public void cleanup(FlowProcess flowProcess,
			OperationCall<NullContext> operationCall) {
		LOGGER.info("cleaning up " + getClass().getName());
		super.cleanup(flowProcess, operationCall);
		_flowProcess.dumpCounters();
	}

	@Override
	public void operate(FlowProcess process, BufferCall<NullContext> bufferCall) {
		Iterator<TupleEntry> iter = bufferCall.getArgumentsIterator();

		// We will end up with 1- n entries of (C)rawlDbDatum, (S)tatusDatum,
		// (A)nalyzedDatum, (L)inkDatum
		// [C | S | A | L] [C | S | A | L] [C | S | A | L] [C | S | A | L]

		CrawlDbDatum crawlDbDatum = null;
		StatusDatum statusDatum = null;
		AnalyzedDatum analyzedDatum = null;
		UrlStatus status = null;
		float pageScore = 0;
		float linkScore = 0;
		String url = bufferCall.getGroup().getTuple().getString(0);
		while (iter.hasNext()) {
			TupleEntry entry = iter.next();

			boolean isCrawlDatum = entry.getString(CRAWLDBDATUM_URL_FIELD) != null;
			boolean isStatus = entry.getString(STATUSDATUM_URL_FIELD) != null;
			boolean isAnalyzed = entry.getString(ANALYZEDDATUM_URL_FIELD) != null;
			if (isCrawlDatum) {
				Tuple crawlDbTuple = TupleEntry.select(CrawlDbDatum.FIELDS,
						entry);
				crawlDbDatum = new CrawlDbDatum(crawlDbTuple);
			}

			if (isStatus) {
				statusDatum = new StatusDatum(entry);
			}

			if (isAnalyzed) {
				Tuple analyzedTuple = TupleEntry.select(AnalyzedDatum.FIELDS,
						entry);
				analyzedDatum = new AnalyzedDatum(analyzedTuple);
			}

			// we could have either status + link or just link tuple entry
			if (entry.getString(new Fields(LinkDatum.URL_FN)) != null) {
				LinkDatum linkDatum = new LinkDatum(TupleEntry.select(
						LinkDatum.FIELDS, entry));

				pageScore = linkDatum.getPageScore();
				// Add up the link scores
				linkScore += linkDatum.getLinkScore();
			}
		}

		long lastFetched = 0;
		if (crawlDbDatum != null) {
			status = crawlDbDatum.getLastStatus();
			pageScore = crawlDbDatum.getPageScore();
			linkScore += crawlDbDatum.getLinksScore();
			lastFetched = crawlDbDatum.getLastFetched();
		} else if (statusDatum != null) {
			status = statusDatum.getStatus();
			if (status != UrlStatus.FETCHED) {
				pageScore = (Float) statusDatum
						.getPayloadValue(CustomFields.PAGE_SCORE_FN);
				linkScore += (Float) statusDatum
						.getPayloadValue(CustomFields.LINKS_SCORE_FN);
			} else {
				if (analyzedDatum != null) {
					pageScore = analyzedDatum.getPageScore();
				}
			}
			lastFetched = statusDatum.getStatusTime();
		} else {
			status = UrlStatus.UNFETCHED;
		}

		CrawlDbDatum updatedDatum = new CrawlDbDatum(url, lastFetched, status,
				pageScore, linkScore);
		bufferCall.getOutputCollector().add(updatedDatum.getTuple());
	}

	@Override
	public void prepare(FlowProcess flowProcess,
			OperationCall<NullContext> operationCall) {
		LOGGER.info("preparing " + getClass().getName());
		super.prepare(flowProcess, operationCall);
		_flowProcess = new LoggingFlowProcess((HadoopFlowProcess) flowProcess);
		_flowProcess.addReporter(new LoggingFlowReporter());
	}

}
