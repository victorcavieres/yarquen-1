package org.yarquen.category;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

@IfProfileValue(name = "test-groups", values = { "itests" })
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/context.xml", "/category-context.xml" })
public class Topic2CategoryMongodbTest {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(Topic2CategoryMongodbTest.class);
	@javax.annotation.Resource
	private CategoryRepository topicRepository;

	private Model model = ModelFactory.createDefaultModel();;
	final String namespace = "http://www.toeska.cl/ns/contentcompass/cc-ontology/it/v1.0/topic#";
	final String ccItNs = "http://www.toeska.cl/ns/contentcompass/ontology/it/v1.0#";
	final String skosNS = "http://www.w3.org/2004/02/skos/core#";
	final Property narrowerProperty = model
			.createProperty("http://www.w3.org/2004/02/skos/core#narrower");
	final String productType = ccItNs + "Product";
	final String categoryType = ccItNs + "Category";
	final String inputFileName = "topicos-it.rdf";
	final String[] parentCategoryArray = { namespace + "Informatica",
			namespace + "Hardware", namespace + "Comunicacion",
			namespace + "Software" };

	@Before
	public void setUp() throws Exception {
		final InputStream in = FileManager.get().open(inputFileName);
		if (in == null) {
			throw new IllegalArgumentException("File: " + inputFileName
					+ " not found");
		}
		model.read(in, null);
	}

	@Test
	public void test() {
		final List<Category> parentCategoryList = new ArrayList<Category>();
		for (String parentUri : parentCategoryArray) {
			final Resource parent = model.getResource(parentUri);
			final Category parentCategory = new Category();
			String uri = parent.getURI();
			parentCategory.setCode(uri.substring(uri.indexOf("#") + 1));
			parentCategory.setName(parent.getProperty(RDFS.label).getLiteral()
					.getString());
			LOGGER.debug("has properties: {}",
					parent.getProperty(narrowerProperty).getObject());
			parentCategory.setSubCategories(getNarrowerList(parent));
			parentCategoryList.add(parentCategory);
		}
		topicRepository.save(parentCategoryList);
		LOGGER.debug("final List: {}", parentCategoryList.toString());

	}

	private List<SubCategory> getNarrowerList(final Resource parent) {
		final List<SubCategory> narrowerList = new ArrayList<SubCategory>();
		final NodeIterator nodeIt = model.listObjectsOfProperty(parent,
				narrowerProperty);
		for (RDFNode node : nodeIt.toList()) {
			SubCategory child = new SubCategory();
			Resource childResource = (Resource) node;
			child.setCode(childResource.getURI().substring(childResource.getURI().indexOf("#") + 1));
			if (childResource.getProperty(RDF.type).getResource().getURI()
					.equals(categoryType)) {
				List<SubCategory> subCategories = getNarrowerList(childResource);
				child.setSubCategories(subCategories);
			}
			child.setName(childResource.getProperty(RDFS.label).getLiteral()
					.getString());
			narrowerList.add(child);
		}
		return narrowerList;

	}

}
