package net.minecraft.server;

public class RecipesTools {

    private String[][] a = new String[][] { { "XXX", " # ", " # "}, { "X", "#", "#"}, { "XX", "X#", " #"}, { "XX", " #", " #"}};
    private Object[][] b;

    public RecipesTools() {
        this.b = new Object[][] { { Blocks.PLANKS, Blocks.COBBLESTONE, Items.IRON_INGOT, Items.DIAMOND, Items.GOLD_INGOT, Items.BAUXITE_INGOT, Items.GRANITE, Items.OPALE, Items.METEOR_FRAGMENT}, 
        		{ Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE, Items.GOLDEN_PICKAXE, Items.BAUXITE_PICKAXE, Items.GRANITE_PICKAXE, Items.OPALE_PICKAXE, Items.METEOR_PICKAXE}, 
        		{ Items.WOODEN_SHOVEL, Items.STONE_SHOVEL, Items.IRON_SHOVEL, Items.DIAMOND_SHOVEL, Items.GOLDEN_SHOVEL, Items.BAUXITE_SPADE, Items.GRANITE_SPADE, Items.OPALE_SPADE, Items.METEOR_SPADE}, 
        		{ Items.WOODEN_AXE, Items.STONE_AXE, Items.IRON_AXE, Items.DIAMOND_AXE, Items.GOLDEN_AXE, Items.BAUXITE_AXE, Items.GRANITE_AXE, Items.OPALE_AXE, Items.METEOR_AXE}, 
        		{ Items.WOODEN_HOE, Items.STONE_HOE, Items.IRON_HOE, Items.DIAMOND_HOE, Items.GOLDEN_HOE, Items.BAUXITE_HOE, Items.GRANITE_HOE, Items.OPALE_HOE, Items.METEOR_HOE}};
    }

    public void a(CraftingManager craftingmanager) {
        for (int i = 0; i < this.b[0].length; ++i) {
            Object object = this.b[0][i];

            for (int j = 0; j < this.b.length - 1; ++j) {
                Item item = (Item) this.b[j + 1][i];

                craftingmanager.registerShapedRecipe(new ItemStack(item), new Object[] { this.a[j], Character.valueOf('#'), Items.STICK, Character.valueOf('X'), object});
            }
        }

        craftingmanager.registerShapedRecipe(new ItemStack(Items.SHEARS), new Object[] { " #", "# ", Character.valueOf('#'), Items.IRON_INGOT});
    }
}