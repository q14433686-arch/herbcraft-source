package com.herbcraft.knowledge;

/** Classification heuristics for core herbal Codex sections. */
public final class HerbalChapterClassifier {
   private HerbalChapterClassifier() {
   }

   public static String classify(String path) {
      if (path.contains("crimson") || path.contains("warped") || path.contains("nether") || path.contains("weeping") || path.contains("twisting")) {
         return "nether";
      } else if (path.contains("snow") || path.contains("ice")) {
         return "ice";
      } else if (path.contains("kelp") || path.contains("seagrass") || path.contains("sea_pickle") || path.contains("lily_pad")) {
         return "sea";
      } else if (!path.contains("leaves")
         && !path.contains("sapling")
         && !path.contains("propagule")
         && !path.contains("fern")
         && !path.contains("grass")
         && !path.contains("vine")
         && !path.contains("bamboo")
         && !path.contains("sugar_cane")
         && !path.contains("cocoa")
         && !path.contains("seeds")
         && !path.contains("moss")
         && !path.contains("mushroom")
         && !path.contains("fungus")) {
         return !path.contains("dandelion")
               && !path.contains("poppy")
               && !path.contains("orchid")
               && !path.contains("bluet")
               && !path.contains("daisy")
               && !path.contains("cornflower")
               && !path.contains("lily_of_the_valley")
               && !path.contains("wither_rose")
               && !path.contains("sunflower")
               && !path.contains("lilac")
               && !path.contains("rose_bush")
               && !path.contains("peony")
               && !path.contains("pitcher")
               && !path.contains("torchflower")
               && !path.contains("eyeblossom")
               && !path.contains("allium")
               && !path.contains("azalea")
               && !path.contains("tulip")
               && !path.contains("petals")
               && !path.contains("spore_blossom")
               && !path.contains("cactus_flower")
               && !path.contains("flower")
            ? "curios"
            : "flowers";
      } else {
         return "foliage";
      }
   }
}
