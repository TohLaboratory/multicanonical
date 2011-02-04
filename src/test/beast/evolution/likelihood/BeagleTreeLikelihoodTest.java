package test.beast.evolution.likelihood;


import beast.evolution.likelihood.BeagleTreeLikelihood;
import beast.evolution.likelihood.TreeLikelihood;

/** Same as TreeLikelihoodTest, but for Beagle Tree Likelihood. 
 * **/
public class BeagleTreeLikelihoodTest extends TreeLikelihoodTest {

	protected TreeLikelihood newTreeLikelihood() {
		return new BeagleTreeLikelihood();
	}
	
} // class BeagleTreeLikelihoodTest