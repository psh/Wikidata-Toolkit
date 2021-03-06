package org.wikidata.wdtk.datamodel.implementation;

import java.util.*;

/*
 * #%L
 * Wikidata Toolkit Data Model
 * %%
 * Copyright (C) 2014 Wikidata Toolkit Developers
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.stream.Collectors;

import org.wikidata.wdtk.datamodel.helpers.Equality;
import org.wikidata.wdtk.datamodel.helpers.Hash;
import org.wikidata.wdtk.datamodel.helpers.ToString;
import org.wikidata.wdtk.datamodel.interfaces.Reference;
import org.wikidata.wdtk.datamodel.interfaces.Snak;
import org.wikidata.wdtk.datamodel.interfaces.SnakGroup;
import org.wikidata.wdtk.util.NestedIterator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Jackson implementation of {@link Reference}.
 *
 * @author Fredo Erxleben
 * @author Markus Kroetzsch
 * @author Antonin Delpeuch
 * 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReferenceImpl implements Reference {

	private List<SnakGroup> snakGroups;

	/**
	 * Map of property id strings to snaks, as used to encode snaks in JSON.
	 */
	private final Map<String, List<Snak>> snaks;

	/**
	 * List of property string ids that encodes the desired order of snaks,
	 * which is not specified by the map.
	 */
	private final List<String> propertyOrder;
	
	/**
	 * Constructor.
	 * <p>
	 * The order of the snaks groups provided will be respected.
	 * the properties used by the snak groups should be distinct.
	 * 
	 * @param groups
	 * 		the snaks group which form the reference
	 */
	public ReferenceImpl(List<SnakGroup> groups) {
		this.propertyOrder = groups.stream()
				.map(g -> g.getProperty().getId())
				.collect(Collectors.toList());
		this.snaks = groups.stream()
				.collect(Collectors.toMap(g -> g.getProperty().getId(), SnakGroup::getSnaks));
	}
	
	/**
	 * Constructor for deserialization from JSON.
	 */
	@JsonCreator
	protected ReferenceImpl(
			@JsonProperty("snaks") Map<String, List<SnakImpl>> snaks,
			@JsonProperty("snaks-order") List<String> propertyOrder) {
		this.snaks = new HashMap<>(snaks.size());
		for(Map.Entry<String, List<SnakImpl>> entry : snaks.entrySet()) {
			this.snaks.put(entry.getKey(), new ArrayList<>(entry.getValue()));
		}
		this.propertyOrder = propertyOrder;
	}

	@JsonIgnore
	@Override
	public List<SnakGroup> getSnakGroups() {
		if (this.snakGroups == null) {
			this.snakGroups = SnakGroupImpl.makeSnakGroups(this.snaks,
					this.propertyOrder);
		}
		return this.snakGroups;
	}

	/**
	 * Returns the map of snaks as found in JSON. Only for use by Jackson during
	 * serialization.
	 *
	 * @return the map of snaks
	 */
	@JsonProperty("snaks")
	public Map<String, List<Snak>> getSnaks() {
		return Collections.unmodifiableMap(this.snaks);
	}

	/**
	 * Returns the list of property ids used to order snaks as found in JSON.
	 * Only for use by Jackson during serialization.
	 *
	 * @return the list of property ids
	 */
	@JsonProperty("snaks-order")
	public List<String> getPropertyOrder() {
		return Collections.unmodifiableList(this.propertyOrder);
	}

	@Override
	@JsonIgnore
	public Iterator<Snak> getAllSnaks() {
		return new NestedIterator<>(getSnakGroups());
	}

	@Override
	public int hashCode() {
		return Hash.hashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return Equality.equalsReference(this, obj);
	}

	@Override
	public String toString() {
		return ToString.toString(this);
	}
}
