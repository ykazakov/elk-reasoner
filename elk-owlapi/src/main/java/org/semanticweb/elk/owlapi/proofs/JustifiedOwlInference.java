package org.semanticweb.elk.owlapi.proofs;

/*-
 * #%L
 * ELK OWL API v.4 Binding
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2011 - 2021 Department of Computer Science, University of Oxford
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

import java.util.Set;

import org.liveontologies.puli.AxiomPinpointingInference;
import org.liveontologies.puli.DelegatingInference;
import org.liveontologies.puli.Inference;
import org.semanticweb.elk.owl.interfaces.ElkAxiom;
import org.semanticweb.elk.owlapi.ElkConverter;
import org.semanticweb.elk.proofs.JustifiedInernalInference;
import org.semanticweb.owlapi.model.OWLAxiom;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

class JustifiedOwlInference<C>
		extends DelegatingInference<C, AxiomPinpointingInference<C, ElkAxiom>>
		implements AxiomPinpointingInference<C, OWLAxiom>,
		Function<ElkAxiom, OWLAxiom> {

	JustifiedOwlInference(AxiomPinpointingInference<C, ElkAxiom> delegate) {
		super(delegate);
	}

	JustifiedOwlInference(Inference<C> delegate) {
		this(new JustifiedInernalInference<>(delegate));
	}

	@Override
	public Set<? extends OWLAxiom> getJustification() {
		return ImmutableSet.copyOf(
				Iterables.transform(getDelegate().getJustification(), this));
	}

	@Override
	public OWLAxiom apply(final ElkAxiom input) {
		return ElkConverter.getInstance().convert(input);
	}

}
