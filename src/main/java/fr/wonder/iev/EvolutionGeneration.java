package fr.wonder.iev;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EvolutionGeneration {

	public static final int BATCH_SIZE = 200; // 200
	private static final int KEEP_COUNT = 15;
	public static final int STEPS = 30;
	
	private static final float MIN_SIZE = .01f, MAX_SIZE = .5f;
	private static float MIN_X = -1, MIN_Y = -1, MAX_X = 1, MAX_Y = 1;
	private static final float MUTATION_TRANSLATION_MAGNITUDE = (MAX_X-MIN_X)/10f;
	private static final float MUTATION_SCALE_MAGNITUDE = .05f;
	private static final float MUTATION_ROTATION_MAGNITUDE = Mathr.PI/10f;
	
	private final List<Individual> individuals = new ArrayList<>(BATCH_SIZE);
	private final int texturesCount;
	
	public static void setTargetAspectRatio(int w, int h) {
		if(w > h) {
			MIN_X *= (float)w/h;
			MAX_X *= (float)w/h;
		} else {
			MIN_Y *= (float)h/w;
			MAX_Y *= (float)h/w;
		}
	}
	
	public EvolutionGeneration(int texturesCount) {
		this.texturesCount = texturesCount;
	}
	
	public Individual getFirstIndividual() {
		return individuals.get(0);
	}
	
	public void generateInitialPopulation() {
		for(int i = 0; i < BATCH_SIZE; i++) {
			individuals.add(new Individual(Mathr.randRange(0, texturesCount), randomTransform()));
		}
	}
	
	private Transform randomTransform() {
		return new Transform(
				Mathr.randRange(MIN_X, MAX_X),           // position (0,0-1,1)
				Mathr.randRange(MIN_Y, MAX_Y),
				Mathr.randRange(MIN_SIZE, MAX_SIZE),     // scale    (0-1)
				Mathr.randAngle()                        // rotation (0-2pi)
		);
	}
	
	public void rankIndividuals() {
		individuals.sort(Comparator.comparingDouble(Individual::getScore).reversed());
	}
	
	public void keepBestIndividuals() {
		while(individuals.size() > KEEP_COUNT)
			individuals.remove(KEEP_COUNT);
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
				Mathr.clamp(transform.translationX + Mathr.randRange(-d, +d), MIN_X, MAX_X),
				Mathr.clamp(transform.translationY + Mathr.randRange(-d, +d), MIN_Y, MAX_Y),
				Mathr.clamp(transform.scale * Mathr.randRange(si, sa), MIN_SIZE, MAX_SIZE),
				transform.rotation + Mathr.randRange(-r, +r)
		);
		int textureIndex = Mathr.rand() < .05f ? Mathr.randRange(0, texturesCount) : individual.textureIndex;
		return new Individual(textureIndex, newTransform);
	}

	public List<Individual> getIndividuals() {
		return individuals;
	}
	
}
