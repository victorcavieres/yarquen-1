package org.yarquen.topic;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/context.xml", "/topics-context.xml" })
public class Topic2MongodbTest {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(Topic2MongodbTest.class);
	@javax.annotation.Resource
	private TopicRepository topicRepository;
	
	
	private Model model=ModelFactory.createDefaultModel();;
	final String namespace="http://www.toeska.cl/ns/contentcompass/cc-ontology/it/v1.0/topic#";
	final String ccItNs="http://www.toeska.cl/ns/contentcompass/ontology/it/v1.0#";
	final String skosNS="http://www.w3.org/2004/02/skos/core#";
	final Property narrowerProperty=model.createProperty("http://www.w3.org/2004/02/skos/core#narrower");
	final String productType=ccItNs+"Product";
	final String categoryType=ccItNs+"Category";
	final String inputFileName="topicos-it.rdf";
	final String[] parentCategoryArray={namespace+"Informatica",
			namespace+"Hardware",
			namespace+"Comunicacion",
			namespace+"Software"
			
	};

	@Before
	public void setUp() throws Exception {
		final InputStream in = FileManager.get().open( inputFileName );
		if (in == null) {
		    throw new IllegalArgumentException(
		                                 "File: " + inputFileName + " not found");
		}
		model.read(in, null);
	}

	@Test
	public void test() {
		final List<Category> parentCategoryList=new ArrayList<Category>();
		for(String parentUri:parentCategoryArray){
			final Resource parent = model.getResource(parentUri);
			final Category parentCategory=new Category();
			parentCategory.setId(parent.getURI());
			parentCategory.setLabel(parent.getProperty(RDFS.label).getLiteral().getString());
			LOGGER.debug("has properties: {}",parent.getProperty(narrowerProperty).getObject());
			parentCategory.getNarrower().addAll(getNarrowerList(parent));
			parentCategoryList.add(parentCategory);
		}
		topicRepository.save(parentCategoryList);
		LOGGER.debug("final List: {}",parentCategoryList.toString());

	}
	
	private List<Topic> getNarrowerList(final Resource parent){
		final List<Topic> narrowerList=new ArrayList<Topic>();
		final NodeIterator nodeIt = model.listObjectsOfProperty(parent, narrowerProperty);
		Topic child = null;
		for(RDFNode node:nodeIt.toList()){
			Resource childResource=(Resource) node;
			if(childResource.getProperty(RDF.type).getResource().getURI().equals(categoryType)){
				child=new Category();
				((Category)child).getNarrower().addAll(getNarrowerList(childResource));
			}else if(childResource.getProperty(RDF.type).getResource().getURI().equals(productType)){
				child=new Product();
			}
			child.setId(childResource.getURI());
			child.setLabel(childResource.getProperty(RDFS.label).getLiteral().getString());
			narrowerList.add(child);
		}
		return narrowerList;
		
	}

}
