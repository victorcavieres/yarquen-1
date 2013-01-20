package org.yarquen.topic;
/**
 * Bean that describe a software product
 * @author maliq
 *
 */

public class Product extends Topic{
	private String version;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return super.toString()+"Product [version=" + version + "]";
	}




	
	
	
}
