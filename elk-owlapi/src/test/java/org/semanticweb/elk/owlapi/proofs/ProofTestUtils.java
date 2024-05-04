/*
 * #%L
 * ELK Proofs Package
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2011 - 2014 Department of Computer Science, University of Oxford
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.liveontologies.puli.AxiomPinpointingInference;
import org.liveontologies.puli.Inference;
import org.liveontologies.puli.Inferences;
import org.liveontologies.puli.Proof;
import org.liveontologies.puli.Proofs;
import org.liveontologies.puli.Prover;
import org.liveontologies.puli.pinpointing.AxiomPinpointingCollector;
import org.liveontologies.puli.pinpointing.AxiomPinpointingInterruptMonitor;
import org.liveontologies.puli.pinpointing.TopDownRepairComputation;
import org.semanticweb.elk.owl.inferences.TestUtils;
import org.semanticweb.elk.owlapi.ElkProver;
import org.semanticweb.elk.owlapi.ElkReasoner;
import org.semanticweb.elk.reasoner.completeness.TestIncompleteness;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.Node;

/**
 * TODO this is adapted from {@link TestUtils}, see if we can get rid of
 * copy-paste.
 * 
 * @author Pavel Klinov
 * 
 *         pavel.klinov@uni-ulm.de
 * @author Peter Skocovsky
 * 
 * @author Yevgeny Kazakov
 * 
 */
public class ProofTestUtils {

	public static void provabilityTest(final Proof<?> proof,
			final Object conclusion) {
		assertTrue(String.format("Conclusion %s not derivable!", conclusion),
				isDerivable(proof, conclusion));
	}

	public static boolean isDerivable(final Proof<?> proof,
			final Object conclusion) {
		return Proofs.isDerivable(proof, conclusion);
	}

	public static <Q, I extends Inference<?>> void provabilityTest(
			Prover<Q, I> prover, final Q query) {
		assertTrue(String.format("Entailment %s not derivable!", query),
				isDerivable(prover.getProof(query), query));
	}

	public static void visitAllSubsumptionsForProofTests(
			final ElkReasoner reasoner, final OWLDataFactory factory,
			final ProofTestVisitor visitor) {

		if (!TestIncompleteness.getValue(reasoner.checkIsConsistent())) {
			visitor.visit(factory.getOWLThing(), factory.getOWLNothing());
			return;
		}

		Set<Node<OWLClass>> visited = new HashSet<Node<OWLClass>>();
		Queue<Node<OWLClass>> toDo = new LinkedList<Node<OWLClass>>();

		toDo.add(TestIncompleteness.getValue(reasoner.computeTopClassNode()));
		visited.add(
				TestIncompleteness.getValue(reasoner.computeTopClassNode()));

		for (;;) {
			Node<OWLClass> nextNode = toDo.poll();

			if (nextNode == null) {
				break;
			}

			List<OWLClass> membersList = new ArrayList<OWLClass>(
					nextNode.getEntities());

			if (nextNode.isBottomNode()) {
				// do not check inconsistent concepts for now
				continue;
			}

			// else visit all subsumptions within the node

			for (int i = 0; i < membersList.size() - 1; i++) {
				for (int j = i + 1; j < membersList.size(); j++) {
					OWLClass sub = membersList.get(i);
					OWLClass sup = membersList.get(j);

					if (!sub.equals(sup)) {
						if (!sup.equals(factory.getOWLThing())
								&& !sub.equals(factory.getOWLNothing())) {
							visitor.visit(sub, sup);
						}

						if (!sub.equals(factory.getOWLThing())
								&& !sup.equals(factory.getOWLNothing())) {
							visitor.visit(sup, sub);
						}
					}
				}
			}
			// go one level down
			for (Node<OWLClass> subNode : TestIncompleteness
					.getValue(reasoner.computeSubClasses(
							nextNode.getRepresentativeElement(), true))) {
				if (visited.add(subNode)) {
					toDo.add(subNode);
				}
			}

			for (OWLClass sup : nextNode.getEntities()) {
				for (Node<OWLClass> subNode : TestIncompleteness
						.getValue(reasoner.computeSubClasses(sup, true))) {
					if (subNode.isBottomNode())
						continue;
					for (OWLClass sub : subNode.getEntitiesMinusBottom()) {
						if (!sup.equals(factory.getOWLThing())) {
							visitor.visit(sub, sup);
						}
					}
				}
			}
		}
	}

	public static void proofCompletenessTest(final ElkProver prover,
			final OWLAxiom conclusion) {
		proofCompletenessTest(prover, conclusion, false);
	}

	public static void proofCompletenessTest(final ElkProver elk,
			final OWLAxiom conclusion, final boolean mustNotBeATautology) {
		Set<OWLAxiom> asserted = elk.getRootOntology()
				.getAxioms(Imports.INCLUDED);
		proofCompletenessTest(elk.getDelegate(),
				query -> Proofs.justifyAsserted(Proofs.filter(
						elk.getProof(query),
						inf -> !Inferences.isAsserted(inf)
								|| asserted.contains(inf.getConclusion()))),
				conclusion, mustNotBeATautology);
	}

	public static void proofCompletenessTest(final ElkReasoner reasoner,
			final Prover<OWLAxiom, AxiomPinpointingInference<?, OWLAxiom>> prover,
			final OWLAxiom entailment) {
		proofCompletenessTest(reasoner, prover, entailment, false);
	}

	public static void proofCompletenessTest(final ElkReasoner reasoner,
			final Prover<OWLAxiom, AxiomPinpointingInference<?, OWLAxiom>> prover,
			final OWLAxiom entailment, final boolean mustNotBeATautology) {

		final OWLOntology ontology = reasoner.getRootOntology();
		final OWLOntologyManager manager = ontology.getOWLOntologyManager();

		// compute repairs

		AxiomPinpointingCollector<OWLAxiom> collector = new AxiomPinpointingCollector<OWLAxiom>();
		TopDownRepairComputation.<OWLAxiom, OWLAxiom> getFactory()
				.create(prover, AxiomPinpointingInterruptMonitor.DUMMY)
				.enumerate(entailment, collector);

		if (mustNotBeATautology) {
			assertFalse("Entailment is a tautology; there are no repairs!",
					collector.getRepairs().isEmpty());
		}

		for (final Set<? extends OWLAxiom> repair : collector.getRepairs()) {

			final List<OWLOntologyChange> deletions = new ArrayList<OWLOntologyChange>();
			final List<OWLOntologyChange> additions = new ArrayList<OWLOntologyChange>();
			for (final OWLAxiom axiom : repair) {
				deletions.add(new RemoveAxiom(ontology, axiom));
				additions.add(new AddAxiom(ontology, axiom));
			}

			manager.applyChanges(deletions);

			final boolean conclusionDerived = TestIncompleteness
					.getValue(reasoner.checkEntailment(entailment));

			manager.applyChanges(additions);

			assertFalse("Not all proofs were found!\n" + "Conclusion: "
					+ entailment + "\n" + "Repair: " + repair,
					conclusionDerived);
		}

	}

}
