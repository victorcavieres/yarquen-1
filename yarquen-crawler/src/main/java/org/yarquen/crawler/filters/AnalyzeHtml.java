package org.yarquen.crawler.filters;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yarquen.crawler.datum.AnalyzedDatum;
import org.yarquen.crawler.datum.ArticleDatum;
import org.yarquen.crawler.extractor.ArticleExtractor;
import org.yarquen.crawler.extractor.dzone.DzoneArticleExtractor;
import org.yarquen.crawler.extractor.infoq.InfoqCOExtractor;
import org.yarquen.crawler.scorer.PageScorer;
import org.yarquen.crawler.scorer.dzone.DzoneScorer;
import org.yarquen.crawler.scorer.infoq.InfoqScorer;

import bixo.datum.Outlink;
import bixo.datum.ParsedDatum;
import bixo.parser.DOMParser;
import cascading.flow.FlowProcess;
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
public class AnalyzeHtml extends DOMParser {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(AnalyzeHtml.class);

	private List<ArticleExtractor> extractors;
	private List<PageScorer> pageScorers;

	public AnalyzeHtml() {
		super(AnalyzedDatum.FIELDS);
	}

	@Override
	public void cleanup(FlowProcess flowProcess,
			OperationCall<NullContext> operationCall) {
		LOGGER.info("cleaning up " + getClass().getName());
		super.cleanup(flowProcess, operationCall);
	}

	@Override
	public void prepare(FlowProcess process, OperationCall<NullContext> opCall) {
		LOGGER.info("preparing " + getClass().getName());
		super.prepare(process, opCall);

		extractors = new ArrayList<ArticleExtractor>(2);
		extractors.add(new DzoneArticleExtractor());
		extractors.add(new InfoqCOExtractor());

		pageScorers = new ArrayList<PageScorer>(2);
		pageScorers.add(new DzoneScorer());
		pageScorers.add(new InfoqScorer());
	}

	public void setExtractors(List<ArticleExtractor> extractors) {
		this.extractors = extractors;
	}

	public void setPageScorers(List<PageScorer> pageScorers) {
		this.pageScorers = pageScorers;
	}

	@Override
	protected void handleException(ParsedDatum datum, Exception e,
			TupleEntryCollector collector) {
		LOGGER.error("chaisen!: " + datum.getUrl(), e);
	}

	@Override
	protected void process(ParsedDatum datum, Document doc,
			TupleEntryCollector collector) throws Exception {
		LOGGER.debug("analizing {}", datum.getUrl());

		// extract outlinks
		final List<Outlink> outlinks = getOutlinks(doc);

		// calc score
		float pageScore = 0f;
		for (PageScorer pageScorer : pageScorers) {
			final float ps = pageScorer.getPageScore(datum, doc, outlinks);
			pageScore += ps;
		}

		// extract articles
		final List<ArticleDatum> results = new LinkedList<ArticleDatum>();
		for (ArticleExtractor extractor : extractors) {
			final ArticleDatum article = extractor.extractArticles(datum, doc,
					outlinks);
			if (article != null) {
				results.add(article);
			}
		}
		final ArticleDatum[] pageResults = results
				.toArray(new ArticleDatum[results.size()]);

		// populate result
		final AnalyzedDatum result = new AnalyzedDatum();
		result.setUrl(datum.getUrl());
		result.setPageScore(pageScore);
		result.setOutlinks(outlinks.toArray(new Outlink[outlinks.size()]));
		result.setArticles(pageResults);

		collector.add(result.getTuple());
	}

	private List<Outlink> getOutlinks(Document doc) {
		final List<Outlink> outlinkList = new ArrayList<Outlink>();

		@SuppressWarnings("unchecked")
		final List<Node> nodes = doc.selectNodes("//a");
		if (nodes != null) {
			LOGGER.debug("{} outlinks found", nodes.size());
			for (Node node : nodes) {
				final Element element = (Element) node;
				final String url = element.attributeValue("href");
				final String anchor = element.attributeValue("name");
				final String rel = element.attributeValue("rel");
				final Outlink link = new Outlink(url != null ? url : "",
						anchor != null ? anchor : "", rel != null ? rel : "");
				outlinkList.add(link);
			}
		}
		return outlinkList;
	}
}
