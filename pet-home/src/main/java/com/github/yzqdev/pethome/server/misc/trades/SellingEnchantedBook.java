//package ab.yzq.mod.pet_home.server.misc.trades;
//
//import net.minecraft.util.RandomSource;
//import net.minecraft.world.entity.Entity;
//import net.minecraft.world.entity.npc.VillagerTrades;
//import net.minecraft.world.item.EnchantedBookItem;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.Items;
//import net.minecraft.world.item.enchantment.Enchantment;
//import net.minecraft.world.item.enchantment.EnchantmentInstance;
//import net.minecraft.world.item.trading.MerchantOffer;
//
//public class SellingEnchantedBook implements VillagerTrades.ItemListing {
//    private final Enchantment enchantment;
//    private final int emeraldCount;
//    private final int maxLevels;
//    private final int maxUses;
//    private final int xpValue;
//    private final float priceMultiplier;
//
//    public SellingEnchantedBook(Enchantment enchantment, int maxLevels, int emeraldCount, int maxUses, int xpValue, float priceMultiplier) {
//        this.enchantment = enchantment;
//        this.maxLevels = maxLevels;
//        this.emeraldCount = emeraldCount;
//        this.maxUses = maxUses;
//        this.xpValue = xpValue;
//        this.priceMultiplier = priceMultiplier;
//    }
//
//    public MerchantOffer getOffer(Entity trader, RandomSource rand) {
//        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
//        EnchantedBookItem.createForEnchantment(book, new EnchantmentInstance(enchantment, maxLevels > 1 ? 1 + rand.nextInt(maxLevels - 1) : 1));
//        return new MerchantOffer(new ItemStack(Items.EMERALD, this.emeraldCount), book, this.maxUses, this.xpValue, this.priceMultiplier);
//    }
//}
