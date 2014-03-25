package JockeyControl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffLoader;
import weka.classifiers.Classifier;
import weka.clusterers.Clusterer;
import weka.clusterers.EM;
import weka.clusterers.SimpleKMeans;

public class WekaHandler {
	private EM em;
	private SimpleKMeans kmeans;
	private Instances dataSet;

	private Map<Integer, Integer> emClusters = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> kmClusters = new HashMap<Integer, Integer>();;

	// This was loosely based on the following code (but has changed
	// substantially):
	// http://stackoverflow.com/questions/8212980/weka-example-simple-classification-of-lines-of-text
	public WekaHandler(String arfffile) throws FileNotFoundException, Exception {

		// Default weka options for each clusterer (with -N changed to 4)
		String[] emOptions = { "-I", "100", "-N", "4", "-M", "1.0E-6", "-S",
				"100" };
		String[] kmeansOptions = { "-N", "4", "-I", "500", "-S", "10" };

		ArffLoader loader = new ArffLoader();
		loader.setFile(new File(arfffile));
		dataSet = loader.getStructure();

		Instance i;
		while ((i = loader.getNextInstance(dataSet)) != null) {
			dataSet.add(i);
		}

		em = new EM();
		em.setOptions(emOptions);
		em.buildClusterer(dataSet);
		buildClusterOrdering(emClusters, em);

		kmeans = new SimpleKMeans();
		kmeans.setOptions(kmeansOptions);
		kmeans.buildClusterer(dataSet);
		buildClusterOrdering(kmClusters, kmeans);
	}

	public int getEMCluster(int dist) {
		Instance instance = dataSet.firstInstance();
		instance.setValue(dataSet.attribute("pedDist"), dist);

		// PREDICTION
		int cluster = 0;
		try {
			cluster = em.clusterInstance(instance);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return emClusters.get(cluster);
	}

	public int getKmeansCluster(int dist) {
		Instance instance = dataSet.firstInstance();
		instance.setValue(dataSet.attribute("pedDist"), dist);

		// PREDICTION
		int classLabelRet = 0;
		try {
			classLabelRet = kmeans.clusterInstance(instance);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return classLabelRet;
	}

	// I hate Java. This would literally be one line in C#.
	private void buildClusterOrdering(Map<Integer, Integer> map, Clusterer clusterer) {
		try {
			ArrayList<Double> list = new ArrayList<Double>();

			if (clusterer instanceof EM) {
				System.out.println("EM");
				
				for (int i = 0; i < clusterer.numberOfClusters(); ++i) {
					list.add(((EM)clusterer).getClusterModelsNumericAtts()[i][0][0]);
					System.out.println(i + ": " + ((EM)clusterer).getClusterModelsNumericAtts()[i][0][0]);
				}
			}
			else if (clusterer instanceof SimpleKMeans) {
				System.out.println("KM");
				Instances centroids = ((SimpleKMeans)clusterer).getClusterCentroids();
				
				for (int i = 0; i < centroids.numInstances(); ++i) {
					list.add(centroids.instance(i).value(0));
					
					System.out.println(i + ": " + centroids.instance(i).value(0));
				}
			}
			else {
				throw new Exception("WHAT DID YOU DO?!?");
			}

			ArrayList<Double> ordered = new ArrayList<Double>(list);
			Collections.sort(ordered);

			// Once this is done, the map will be a mapping of
			// cluster numbers to the cluster's order in terms of
			// distance (ie. a number from 0 to 3 mapping to
			// ForwardVerySlow to ForwardVeryFast)
			for (int i = 0; i < ordered.size(); ++i) {
				map.put(list.lastIndexOf(ordered.get(i)), i);
			}
			
			for (int i = 0; i < map.size(); ++i) {
				System.out.println(i + "->" + map.get(i));
			}
			
			System.out.println();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
