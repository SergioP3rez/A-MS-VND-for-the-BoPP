package experiment;


import algorithm.AlgConstructive;
import algorithm.AlgConstructiveLS;
import constructive.*;
import grafo.optilib.metaheuristics.Algorithm;
import grafo.optilib.metaheuristics.Improvement;
import grafo.optilib.results.Experiment;
import improvement.MoveRectanglesLSFI;
import improvement.VND;
import structure.BPPInstance;
import structure.BPPInstanceFactory;
import structure.BPPSolution;
import utils.Utils;

import java.io.File;

public class ExperimentsAlgorithm {
    public static void main(String[] args) {
        String date = Utils.getDate();
        BPPInstanceFactory factory = new BPPInstanceFactory();
        String dir = ((args.length == 0) ? "instances/whole_set" : ("/tmp/"));
        String outDir = "experiments/" + date;

        File outDirCreator = new File(outDir);
        boolean mkdirs = outDirCreator.mkdirs();

        if (!mkdirs) {
            System.out.println("Directory " + outDir + " was previously created, skipping...");
        }

        String[] extensions = new String[]{".txt"};

        Improvement<BPPSolution>[] vnd = new Improvement[]{
                new MoveRectanglesLSFI(0),
                new MoveRectanglesLSFI(1),
                new MoveRectanglesLSFI(2),
                new MoveRectanglesLSFI(3),
        };


        Algorithm<BPPInstance, BPPSolution>[] execution = new Algorithm[]{
//                If do you want to test some new Algorithm, just add the constructor call here

                new AlgConstructive(new GS(-1.0, 25), 100, 10),
                new AlgConstructiveLS(new GS(-1.0, 25), new VND(vnd), 100, 10),

        };

        Experiment<BPPInstance, BPPInstanceFactory, BPPSolution> experiment = new Experiment<>(execution, factory);
        experiment.launch(outDir, dir, extensions);


    }
}