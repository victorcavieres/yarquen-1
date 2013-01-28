package org.yarquen.web.search;

/**
 * 
 * @author Jorge Riquelme Santana
 * @date 11/01/2013
 * @version $Id$
 * 
 */
public class YarquenFacet {
	private boolean applied;
	private String code;
	private int count;
	private String name;
	// TODO: temporal solution
	private String url;
	private String value;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		YarquenFacet other = (YarquenFacet) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		return true;
	}

	public String getCode() {
		return code;
	}

	public int getCount() {
		return count;
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	public String getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		return result;
	}

	public boolean isApplied() {
		return applied;
	}

	public void setApplied(boolean applied) {
		this.applied = applied;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
