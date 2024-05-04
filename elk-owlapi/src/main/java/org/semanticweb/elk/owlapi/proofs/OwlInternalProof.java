/*-
 * #%L
 * ELK Reasoner Protege Plug-in
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
package org.semanticweb.elk.owlapi.proofs;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.liveontologies.puli.AxiomPinpointingInference;
import org.liveontologies.puli.Inferences;
import org.liveontologies.puli.Proof;
import org.semanticweb.elk.exceptions.ElkException;
import org.semanticweb.elk.exceptions.ElkRuntimeException;
import org.semanticweb.elk.owl.interfaces.ElkAxiom;
import org.semanticweb.elk.owlapi.ElkConverter;
import org.semanticweb.elk.owlapi.wrapper.OwlConverter;
import org.semanticweb.elk.proofs.InternalProof;
import org.semanticweb.elk.reasoner.Reasoner;
import org.semanticweb.owlapi.model.OWLAxiom;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

public class OwlInternalProof
		implements Proof<AxiomPinpointingInference<?, OWLAxiom>> {

	private final OwlConverter owlConverter_ = OwlConverter.getInstance();
	private final ElkConverter elkConverter_ = ElkConverter.getInstance();

	private final OWLAxiom goal_;
	private final AxiomPinpointingInference<?, OWLAxiom> goalInference_;
	private final Proof<AxiomPinpointingInference<?, ElkAxiom>> proof_;

	public OwlInternalProof(final Reasoner reasoner, final OWLAxiom goal) {
		this.goal_ = goal;
		final ElkAxiom convertedGoal = owlConverter_.convert(goal);
		this.goalInference_ = Inferences.create("Converting inference", goal,
				Arrays.asList(convertedGoal), Collections.emptySet());
		try {
			this.proof_ = new InternalProof(reasoner, convertedGoal);
		} catch (final ElkException e) {
			throw elkConverter_.convert(e);
		} catch (final ElkRuntimeException e) {
			throw elkConverter_.convert(e);
		}
	}

	public OWLAxiom getGoal() {
		return goal_;
	}

	@Override
	public Collection<AxiomPinpointingInference<?, OWLAxiom>> getInferences(
			final Object conclusion) {
		if (goal_.equals(conclusion)) {
			final Collection<AxiomPinpointingInference<?, OWLAxiom>> result = Arrays
					.asList(goalInference_);
			return result;
		}
		// else
		return Collections2.transform(proof_.getInferences(conclusion),
				new Function<AxiomPinpointingInference<?, ElkAxiom>, AxiomPinpointingInference<?, OWLAxiom>>() {

					@Override
					public AxiomPinpointingInference<?, OWLAxiom> apply(
							AxiomPinpointingInference<?, ElkAxiom> inf) {
						return new JustifiedOwlInference<>(inf);
					}

				});
	}

}