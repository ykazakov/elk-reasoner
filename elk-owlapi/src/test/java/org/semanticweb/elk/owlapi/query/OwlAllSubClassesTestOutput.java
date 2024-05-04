package org.semanticweb.elk.owlapi.query;

/*-
 * #%L
 * ELK OWL API Binding
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2011 - 2026 Live Ontologies Project
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

import java.util.Collection;

import org.semanticweb.elk.owlapi.ElkReasoner;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public class OwlAllSubClassesTestOutput extends
		OwlRelatedEntitiesTestOutput<OWLClass, OwlAllSubClassesTestOutput> {

	private static OWLDataFactory FACTORY_ = new OWLDataFactoryImpl();

	OwlAllSubClassesTestOutput(OWLClassExpression query,
			Collection<? extends Node<OWLClass>> disjointNodes) {
		super(query, disjointNodes);
	}
	
	OwlAllSubClassesTestOutput(ElkReasoner reasoner,
			OWLClassExpression query) {
		super(query,
				reasoner.computeSubClasses(query, false).map(NodeSet::getNodes));
	}

	@Override
	protected OwlRelatedEntitiesDiffable.Listener<OWLClass> adaptListener(
			Listener<OWLAxiom> listener) {
		return new OwlRelatedEntitiesDiffable.Listener<OWLClass>() {

			@Override
			public void missingCanonical(OWLClass canonical) {
				listener.missing(
						FACTORY_.getOWLSubClassOfAxiom(canonical, getQuery()));
			}

			@Override
			public void missingMember(OWLClass canonical, OWLClass member) {
				listener.missing(FACTORY_
						.getOWLEquivalentClassesAxiom(canonical, member));
			}

		};
	}

}
