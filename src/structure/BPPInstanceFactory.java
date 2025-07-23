package structure;

import grafo.optilib.structure.InstanceFactory;

public class BPPInstanceFactory extends InstanceFactory<BPPInstance> {
    @Override
    public BPPInstance readInstance(String path) {
        return new BPPInstance(path);
    }
}
