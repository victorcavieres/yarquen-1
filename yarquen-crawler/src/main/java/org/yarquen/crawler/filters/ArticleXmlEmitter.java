package org.yarquen.crawler.filters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yarquen.article.Article;
import org.yarquen.crawler.datum.ArticleDatum;

import cascading.flow.FlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.Function;
import cascading.operation.FunctionCall;
import cascading.operation.OperationCall;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntryCollector;

import com.bixolabs.cascading.NullContext;

/**
 * 
 * @author Jorge Riquelme Santana
 * @date 07/08/2012
 * @version $Id$
 * 
 */
@SuppressWarnings("serial")
public class ArticleXmlEmitter extends BaseOperation<NullContext> implements
		Function<NullContext> {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ArticleXmlEmitter.class);

	private JAXBContext context;
	private Marshaller marshaller;

	public ArticleXmlEmitter() {
		super(new Fields("line"));
	}

	@Override
	public void cleanup(FlowProcess flowProcess,
			OperationCall<NullContext> operationCall) {
		LOGGER.info("cleaning up " + getClass().getName());
		super.cleanup(flowProcess, operationCall);
	}

	@Override
	public void prepare(FlowProcess process,
			OperationCall<NullContext> operationCall) {
		LOGGER.info("preparing " + getClass().getName());
		super.prepare(process, operationCall);

		try {
			context = JAXBContext.newInstance("org.yarquen.model");
			marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
					Boolean.TRUE);
		} catch (JAXBException e) {
			throw new RuntimeException("Error while setting up JAXB");
		}
	}

	@Override
	public void operate(FlowProcess process, FunctionCall<NullContext> funcCall) {
		final ArticleDatum datum = new ArticleDatum(funcCall.getArguments()
				.getTuple());
		LOGGER.debug("emitting xml for {}", datum.getUrl());

		// to xml
		try {
			emitXml(datum);
		} catch (JAXBException e) {
			LOGGER.error("Jaxb error while writing xml for " + datum.getUrl(),
					e);
		} catch (IOException e) {
			LOGGER.error("IO error while writing xml for " + datum.getUrl(), e);
		}

		// to control file
		final TupleEntryCollector collector = funcCall.getOutputCollector();
		final String outResult = String.format("%s", datum.getUrl());
		collector.add(new Tuple(outResult));
	}

	private void emitXml(ArticleDatum articleDatum) throws JAXBException,
			IOException {

		final File tempFile = File.createTempFile("art", ".xml", new File(
				"/home/totex/local/tmp/asdf"));
		final Writer writer = new OutputStreamWriter(new FileOutputStream(
				tempFile), "UTF-8");

		final Article article = new Article();
		article.setAuthor(articleDatum.getAuthor());
		article.setDate(articleDatum.getDate());

		final List<String> keywords = new ArrayList<String>(
				articleDatum.getKeywords().length);
		for (String keyword : articleDatum.getKeywords()) {
			keywords.add(keyword);
		}
		article.setKeywords(keywords);

		article.setPlainText(articleDatum.getPlainText());
		article.setSummary(articleDatum.getSummary());
		article.setTitle(articleDatum.getTitle());
		article.setUrl(articleDatum.getUrl());

		// FIXME
		// final ObjectFactory objectFactory = new ObjectFactory();
		// marshaller.marshal(objectFactory.createArticle(article), writer);

		writer.flush();
		writer.close();
	}
}
