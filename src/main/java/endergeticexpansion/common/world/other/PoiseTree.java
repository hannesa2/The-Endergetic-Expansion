package endergeticexpansion.common.world.other;

import java.util.Random;

import javax.annotation.Nullable;

import endergeticexpansion.common.world.features.FeaturePoiseTree;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

public class PoiseTree extends EndergeticTree {

	@Override
	@Nullable
	protected Feature<NoFeatureConfig> getTreeFeature(Random random) {
		return new FeaturePoiseTree(NoFeatureConfig::deserialize);
	}

}
