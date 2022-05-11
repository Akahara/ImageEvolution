package fr.wonder.iev;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import fr.wonder.commons.math.Mathf;
import fr.wonder.commons.math.Mathr;
import fr.wonder.commons.math.vectors.Vec2;

public class EvolutionGeneration {

	public static final int BATCH_SIZE = 200; // 200
	private static final int KEEP_COUNT = 15;
	public static final int STEPS = 30;
	
	private static final float MIN_SIZE = .01f, MAX_SIZE = .5f;
	private static final float MIN_X = -1, MIN_Y = -1, MAX_X = 1, MAX_Y = 1;
	private static final float MUTATION_TRANSLATION_MAGNITUDE = (MAX_X-MIN_X)/10f;
	private static final float MUTATION_SCALE_MAGNITUDE = .05f;
	private static final float MUTATION_ROTATION_MAGNITUDE = Mathf.PI/10f;
	
	private final List<Individual> individuals = new ArrayList<>();
	
	public EvolutionGeneration() {}
	
//	public Individual getBestIndividual() {
//		generateInitialPopulation();
//		for(int s = 1; s < STEPS; s++) {
//			scoreIndividuals();
//			rankIndividuals();
//			keepBestIndividuals();
//			reproduceIndividuals();
//			System.out.println("Finished step " + s);
//		}
//		scoreIndividuals();
//		rankIndividuals();
//		return individuals.get(0);
//	}
	
	public Individual getFirstIndividual() {
		return individuals.get(0);
	}
	
	public void generateInitialPopulation(int texturesCount) {
		for(int i = 0; i < BATCH_SIZE; i++) {
			individuals.add(new Individual(Mathr.randRange(0, texturesCount), randomTransform()));
		}
	}
	
	private Transform randomTransform() {
		return new Transform(
				new Vec2(Mathr.randRange(MIN_X, MAX_X),
						 Mathr.randRange(MIN_Y, MAX_Y)),                // position (0,0-1,1)
				Mathr.randRange(MIN_SIZE, MAX_SIZE),  // scale    (0-1)
				Mathr.randAngle()                     // rotation (0-2pi)
		);
	}
	
	public void scoreIndividuals() {
		
	}
	
	public void rankIndividuals() {
		individuals.sort(Comparator.comparingDouble(Individual::getScore).reversed());
	}
	
	public void keepBestIndividuals() {
		while(individuals.size() > KEEP_COUNT)
			individuals.remove(KEEP_COUNT);
//		for(Individual i : individuals)
//			System.out.println("Keeping " + i.getScore());
	}
	
	public void reproduceIndividuals() {
		List<Individual> newborns = new ArrayList<>();
		while(newborns.size() < BATCH_SIZE - KEEP_COUNT) {
			newborns.add(mutateIndividual(Mathr.randIn(individuals)));
		}
		individuals.addAll(newborns);
	}
	
	private Individual mutateIndividual(Individual individual) {
		Transform transform = individual.transform;
		final float d = MUTATION_TRANSLATION_MAGNITUDE/2f;
		final float si = 1-MUTATION_SCALE_MAGNITUDE/2f;
		final float sa = 1+MUTATION_SCALE_MAGNITUDE/2f;
		final float r = MUTATION_ROTATION_MAGNITUDE/2f;
		Transform newTransform = new Transform(
				new Vec2(
						Mathf.clamp(transform.translation.x + Mathr.randRange(-d, +d), MIN_X, MAX_X),
						Mathf.clamp(transform.translation.y + Mathr.randRange(-d, +d), MIN_Y, MAX_Y)),
				Mathf.clamp(transform.scale * Mathr.randRange(si, sa), MIN_SIZE, MAX_SIZE),
				transform.rotation + Mathr.randRange(-r, +r)
		);
		return new Individual(individual.textureIndex, newTransform);
	}

	public List<Individual> getIndividuals() {
		return individuals;
	}
	
}
