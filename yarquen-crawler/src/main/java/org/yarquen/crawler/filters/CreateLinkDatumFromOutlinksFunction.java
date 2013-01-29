package org.yarquen.crawler.filters;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yarquen.crawler.datum.AnalyzedDatum;
import org.yarquen.crawler.datum.LinkDatum;

import bixo.datum.Outlink;
import bixo.urls.SimpleUrlNormalizer;
import cascading.flow.FlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.Function;
import cascading.operation.FunctionCall;
import cascading.operation.OperationCall;
import cascading.tuple.TupleEntryCollector;

import com.bixolabs.cascading.NullContext;

/**
 * 
 * @author Jorge Riquelme Santana
 * @date 23/05/2012
 * @version $Id$
 * 
 */
@SuppressWarnings("serial")
public class CreateLinkDatumFromOutlinksFunction extends
		BaseOperation<NullContext> implements Function<NullContext> {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(CreateLinkDatumFromOutlinksFunction.class);
	private transient SimpleUrlNormalizer _normalizer;

	public CreateLinkDatumFromOutlinksFunction() {
		super(LinkDatum.FIELDS);
	}

	@Override
	public void prepare(FlowProcess process,
			OperationCall<NullContext> operationCall) {
		LOGGER.info("preparing " + getClass().getName());
		_normalizer = new SimpleUrlNormalizer();
	}

	@Override
	public void cleanup(FlowProcess process,
			OperationCall<NullContext> operationCall) {
		LOGGER.info("cleaning up " + getClass().getName());
	}

	@Override
	public void operate(FlowProcess process, FunctionCall<NullContext> funcCall) {
		final AnalyzedDatum datum = new AnalyzedDatum(funcCall.getArguments()
				.getTuple());
		final Outlink outlinks[] = datum.getOutlinks();
		LOGGER.debug("extracting {} outlinks from analyzed datum {}",
				outlinks.length, datum.getUrl());

		final TupleEntryCollector collector = funcCall.getOutputCollector();

		if (outlinks.length > 0) {
			float pageScore = datum.getPageScore();

			// Give each outlink 1/N th the page score.
			float outlinkScore = pageScore / outlinks.length;

			final Set<String> links = new HashSet<String>(outlinks.length);
			for (Outlink outlink : outlinks) {
				final String url = outlink.getToUrl().replaceAll("[\n\r]", "");
				final String normalizedUrl = _normalizer.normalize(url);
				links.add(normalizedUrl);
			}
			links.remove(datum.getUrl());

			for (String url : links) {
				final LinkDatum linkDatum = new LinkDatum();
				linkDatum.setUrl(url);
				linkDatum.setPageScore(pageScore);
				linkDatum.setLinkScore(outlinkScore);
				collector.add(linkDatum.getTuple());
			}
		}
	}
}
