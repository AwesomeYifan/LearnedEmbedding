package mtree.examples;

import mtree.*;
import mtree.utils.Pair;
import mtree.utils.Utils;

import java.util.Set;
public class MTreeClass extends MTree<Data> {

    private static final PromotionFunction<Data> nonRandomPromotion = new PromotionFunction<Data>() {
        @Override
        public Pair<Data> process(Set<Data> dataSet, DistanceFunction<? super Data> distanceFunction) {
            return Utils.minMax(dataSet);
        }
    };


    MTreeClass() {
        super(2, DistanceFunctions.EUCLIDEAN,
                new ComposedSplitFunction<Data>(
                        nonRandomPromotion,
                        new PartitionFunctions.BalancedPartition<Data>()
                )
        );
    }

    public void add(Data data) {
        super.add(data);
        _check();
    }

    public boolean remove(Data data) {
        boolean result = super.remove(data);
        _check();
        return result;
    }

    DistanceFunction<? super Data> getDistanceFunction() {
        return distanceFunction;
    }
};