package eu.europeana.api.commons.search.util;

import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;

import eu.europeana.api.commons.definitions.search.Query;
import eu.europeana.api.commons.definitions.search.impl.QueryImpl;

public class QueryBuilder {

	private static final String COLON = ":";

	/**
	 * 
	 * @param queryString
	 * @param filters query filters (qf)
	 * @param facets requested facet fields
	 * @param retFields the fields to be returned as response for the Query
	 * @param sortCriteria list of sorting criteria (e.g. ["type+asc", "derived_score+desc"]
	 * @param pageNr the requested results page
	 * @param pageSize the requested pageSize
	 * @param maxPageSize the maximum allowed page size for the given query
	 * @param profile the search profile
	 * @return
	 */
	public Query buildSearchQuery(String queryString, String[] filters, String[] facets, String[] retFields, String[] sortCriteria,
			int pageNr, int pageSize, int maxPageSize, String profile) {

		// TODO: check if needed
		// String[] normalizedFacets =
		// StringArrayUtils.splitWebParameter(facets);
		boolean isFacetsRequested = isFacetsRequest(facets);

		Query searchQuery = new QueryImpl();
		searchQuery.setQuery(queryString);
		if (pageNr < 0)
			searchQuery.setPageNr(Query.DEFAULT_PAGE);
		else
			searchQuery.setPageNr(pageNr);

		int rows = computeValidPageSize(pageSize, maxPageSize);
		searchQuery.setPageSize(rows);
		
		if (isFacetsRequested)
			searchQuery.setFacetFields(facets);

		// translateSearchFilters(filters);
		searchQuery.setFilters(filters);
		if(retFields != null) {
			searchQuery.setViewFields(retFields);
		}
		searchQuery.setSearchProfile(profile);

		if(sortCriteria != null){
			// TODO: EA-941 to implement
		}
		

		return searchQuery;
	}

	protected int computeValidPageSize(int pageSize, int maxPageSize) {
		if (pageSize < 0)
			return Query.DEFAULT_PAGE_SIZE;

		if (pageSize > maxPageSize)
			return maxPageSize;

		return pageSize;
	}

	protected boolean isFacetsRequest(String[] facets) {
		return facets != null && facets.length > 0;
	}

	/**
	 * 
	 * @param filters
	 * @param mappedFilterFields
	 *            the mapping of model (request) fields (map key) to solr fields
	 *            (map value)
	 * @return the equivalent (valid) solr filters           
	 */
	public String[] translateSearchFilters(String[] filters, Map<String, String> mappedFilterFields) {
		if (filters == null)
			return null;
		if (mappedFilterFields == null || mappedFilterFields.isEmpty())
			return filters;

		String[] solrFilters = new String[filters.length];
		String filterField;
		String filterValue;
		String[] filterElem;
		int count = 0;

		for (String filter : filters) {
			if (filter.contains(COLON)) { //TODO think of eliminating invalid filters, e.g. filter doesn't contain colon
				filterElem = filter.split(COLON);
				filterField = filterElem[0];
				filterValue = filterElem[1];

				if (!mappedFilterFields.containsKey(filterField))
					solrFilters[count] = filter;// preserve filter
				else {
					solrFilters[count] = mappedFilterFields.get(filterField) + COLON + filterValue;
				}
			}
			count++;
		}
		
		return solrFilters;
	}
	
	public SolrQuery toSolrQuery(Query searchQuery, String searchHandler) {

		SolrQuery solrQuery = new SolrQuery();
		
		solrQuery.setRequestHandler(searchHandler);
		solrQuery.setQuery(searchQuery.getQuery());

		solrQuery.setRows(searchQuery.getPageSize());
		solrQuery.setStart(searchQuery.getPageNr() * searchQuery.getPageSize());
		
		if (searchQuery.getFilters() != null)
			solrQuery.addFilterQuery(searchQuery.getFilters());

		if (searchQuery.getFacetFields() != null) {
			solrQuery.setFacet(true);
			solrQuery.addFacetField(searchQuery.getFacetFields());
			solrQuery.setFacetLimit(Query.DEFAULT_FACET_LIMIT);
		}

		if (searchQuery.getSortCriteria() != null) {
			//TODO: EA-941
			//foreach
			//StringUtils.split(+)
			//if(length = 1)
			// order=asc //set default ordering if not explicitly stated
			//solrQuery.addSort(field, order)
		}
		if(searchQuery.getViewFields() != null) {
			solrQuery.setFields(searchQuery.getViewFields());
		}

		return solrQuery;
	}
	
}
