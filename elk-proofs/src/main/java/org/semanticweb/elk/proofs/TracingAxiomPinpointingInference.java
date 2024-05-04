package org.semanticweb.elk.proofs;

import java.util.Collections;

/*-
 * #%L
 * ELK Proofs Package
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2011 - 2017 Department of Computer Science, University of Oxford
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

import java.util.List;
import java.util.Set;

import org.liveontologies.puli.AxiomPinpointingInference;
import org.liveontologies.puli.Delegator;
import org.semanticweb.elk.owl.interfaces.ElkAxiom;
import org.semanticweb.elk.reasoner.indexing.model.IndexedAxiomInference;
import org.semanticweb.elk.reasoner.tracing.TracingInference;
import org.semanticweb.elk.reasoner.tracing.TracingInferenceDummyVisitor;

class TracingAxiomPinpointingInference extends Delegator<TracingInference>
		implements AxiomPinpointingInference<Object, ElkAxiom> {

	private static TracingInference.Visitor<Set<? extends ElkAxiom>> JUSTIFICATION_COLLECTOR_ = new TracingInferenceDummyVisitor<Set<? extends ElkAxiom>>() {

		@Override
		protected Set<? extends ElkAxiom> defaultVisit(
				TracingInference inference) {
			return Collections.emptySet();
		}

		@Override
		protected Set<? extends ElkAxiom> defaultVisit(
				IndexedAxiomInference inference) {
			return Collections.singleton(inference.getOriginalAxiom());
		}
	};
	
	public TracingAxiomPinpointingInference(final TracingInference inference) {
		super(inference);
	}

	@Override
	public String getName() {
		return getDelegate().getName();
	}

	@Override
	public Object getConclusion() {
		return getDelegate().getConclusion();
	}

	@Override
	public List<? extends Object> getPremises() {
		return getDelegate().getPremises();
	}

	@Override
	public Set<? extends ElkAxiom> getJustification() {
		return getDelegate().accept(JUSTIFICATION_COLLECTOR_);
	}
	
}