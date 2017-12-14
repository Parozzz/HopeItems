/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.reflex.utilities;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.lang.reflect.Field;
import java.util.Base64;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.parozzz.reflex.NMS.ReflectionUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

/**
 *
 * @author Paros
 */
public class HeadUtil 
{
    public interface Headable
    {
        public String getUrl();
        public ItemStack getHead();
    }
    
    public static enum MobHead implements Headable
    {
        BAT("http://textures.minecraft.net/texture/382fc3f71b41769376a9e92fe3adbaac3772b999b219c9d6b4680ba9983e527"),
        BLAZE("http://textures.minecraft.net/texture/b78ef2e4cf2c41a2d14bfde9caff10219f5b1bf5b35a49eb51c6467882cb5f0"),
        CAVE_SPIDER("http://textures.minecraft.net/texture/41645dfd77d09923107b3496e94eeb5c30329f97efc96ed76e226e98224"),
        CHICKEN("http://textures.minecraft.net/texture/1638469a599ceef7207537603248a9ab11ff591fd378bea4735b346a7fae893"),
        COW("http://textures.minecraft.net/texture/5d6c6eda942f7f5f71c3161c7306f4aed307d82895f9d2b07ab4525718edc5"),
        CREEPER("http://textures.minecraft.net/texture/f4254838c33ea227ffca223dddaabfe0b0215f70da649e944477f44370ca6952"),
        DONKEY("http://textures.minecraft.net/texture/3b25266d40cecd93d053156e4a4a784140d034255c721cc375d1c3648342b6fd"),
        ELDER_GUARDIAN("http://textures.minecraft.net/texture/1c797482a14bfcb877257cb2cff1b6e6a8b8413336ffb4c29a6139278b436b"),
        ENDER_DRAGON("http://textures.minecraft.net/texture/74ecc040785e54663e855ef0486da72154d69bb4b7424b7381ccf95b095a"),
        ENDERMAN("http://textures.minecraft.net/texture/7a59bb0a7a32965b3d90d8eafa899d1835f424509eadd4e6b709ada50b9cf"),
        ENDERMITE("http://textures.minecraft.net/texture/5a1a0831aa03afb4212adcbb24e5dfaa7f476a1173fce259ef75a85855"),
        GHAST("http://textures.minecraft.net/texture/8b6a72138d69fbbd2fea3fa251cabd87152e4f1c97e5f986bf685571db3cc0"),
        GIANT("http://textures.minecraft.net/texture/5c9fdd79d0a58029f959ccf8643aeae1a34f5a9f4dfe3526f6d14be521d8c6e"),
        GUARDIAN("http://textures.minecraft.net/texture/932c24524c82ab3b3e57c2052c533f13dd8c0beb8bdd06369bb2554da86c123"),
        HORSE("http://textures.minecraft.net/texture/7bb4b288991efb8ca0743beccef31258b31d39f24951efb1c9c18a417ba48f9"),
        HUSK("http://textures.minecraft.net/texture/d674c63c8db5f4ca628d69a3b1f8a36e29d8fd775e1a6bdb6cabb4be4db121"),
        ILLUSIONER("http://textures.minecraft.net/texture/1c678c9f4c6dd4d991930f82e6e7d8b89b2891f35cba48a4b18539bbe7ec927"),
        EVOKER("http://textures.minecraft.net/texture/d954135dc82213978db478778ae1213591b93d228d36dd54f1ea1da48e7cba6"),
        IRON_GOLEM("http://textures.minecraft.net/texture/89091d79ea0f59ef7ef94d7bba6e5f17f2f7d4572c44f90f76c4819a714"),
        LLAMA("http://textures.minecraft.net/texture/cf24e56fd9ffd7133da6d1f3e2f455952b1da462686f753c597ee82299a"),
        MAGMA_CUBE("http://textures.minecraft.net/texture/38957d5023c937c4c41aa2412d43410bda23cf79a9f6ab36b76fef2d7c429"),
        MULE("http://textures.minecraft.net/texture/a0486a742e7dda0bae61ce2f55fa13527f1c3b334c57c034bb4cf132fb5f5f"),
        MUSHROOM_COW("http://textures.minecraft.net/texture/d0bc61b9757a7b83e03cd2507a2157913c2cf016e7c096a4d6cf1fe1b8db"),
        OCELOT("http://textures.minecraft.net/texture/5657cd5c2989ff97570fec4ddcdc6926a68a3393250c1be1f0b114a1db1"),
        PARROT("http://textures.minecraft.net/texture/efe08d511499a247146128e55ab6547ecd967d4dbcf803f7ceea2658737c9fa"),
        PIG("http://textures.minecraft.net/texture/621668ef7cb79dd9c22ce3d1f3f4cb6e2559893b6df4a469514e667c16aa4"),
        PIG_ZOMBIE("http://textures.minecraft.net/texture/74e9c6e98582ffd8ff8feb3322cd1849c43fb16b158abb11ca7b42eda7743eb"),
        POLAR_BEAR("http://textures.minecraft.net/texture/d46d23f04846369fa2a3702c10f759101af7bfe8419966429533cd81a11d2b"),
        RABBIT("http://textures.minecraft.net/texture/cec242e667aee44492413ef461b810cac356b74d8718e5cec1f892a6b43e5e1"),
        SHEEP("http://textures.minecraft.net/texture/f31f9ccc6b3e32ecf13b8a11ac29cd33d18c95fc73db8a66c5d657ccb8be70"),
        SHULKER("http://textures.minecraft.net/texture/1e73832e272f8844c476846bc424a3432fb698c58e6ef2a9871c7d29aeea7"),
        SILVERFISH("http://textures.minecraft.net/texture/da91dab8391af5fda54acd2c0b18fbd819b865e1a8f1d623813fa761e924540"),
        SKELETON("http://textures.minecraft.net/texture/301268e9c492da1f0d88271cb492a4b302395f515a7bbf77f4a20b95fc02eb2"),
        SKELETON_HORSE("http://textures.minecraft.net/texture/47effce35132c86ff72bcae77dfbb1d22587e94df3cbc2570ed17cf8973a"),
        SLIME("http://textures.minecraft.net/texture/895aeec6b842ada8669f846d65bc49762597824ab944f22f45bf3bbb941abe6c"),
        SNOWMAN("http://textures.minecraft.net/texture/8e8d206f61e6de8a79d0cb0bcd98aced464cbfefc921b4160a25282163112a"),
        SPIDER("http://textures.minecraft.net/texture/cd541541daaff50896cd258bdbdd4cf80c3ba816735726078bfe393927e57f1"),
        SQUID("http://textures.minecraft.net/texture/01433be242366af126da434b8735df1eb5b3cb2cede39145974e9c483607bac"),
        STRAY("http://textures.minecraft.net/texture/78ddf76e555dd5c4aa8a0a5fc584520cd63d489c253de969f7f22f85a9a2d56"),
        VEX("http://textures.minecraft.net/texture/c2ec5a516617ff1573cd2f9d5f3969f56d5575c4ff4efefabd2a18dc7ab98cd"),
        VILLAGER("http://textures.minecraft.net/texture/822d8e751c8f2fd4c8942c44bdb2f5ca4d8ae8e575ed3eb34c18a86e93b"),
        VINDICATOR("http://textures.minecraft.net/texture/4f6fb89d1c631bd7e79fe185ba1a6705425f5c31a5ff626521e395d4a6f7e2"),
        WITCH("http://textures.minecraft.net/texture/20e13d18474fc94ed55aeb7069566e4687d773dac16f4c3f8722fc95bf9f2dfa"),
        WITHER_SKELETON("http://textures.minecraft.net/texture/7953b6c68448e7e6b6bf8fb273d7203acd8e1be19e81481ead51f45de59a8"),
        WOLF("http://textures.minecraft.net/texture/1d83731d77f54f5d4f93ddd99b9476e4f1fe5b7e1318f1e1626f7d3fa3aa847"),
        ZOMBIE("http://textures.minecraft.net/texture/56fc854bb84cf4b7697297973e02b79bc10698460b51a639c60e5e417734e11"),
        ZOMBIE_HORSE("http://textures.minecraft.net/texture/d22950f2d3efddb18de86f8f55ac518dce73f12a6e0f8636d551d8eb480ceec"),
        ZOMBIE_VILLAGER("http://textures.minecraft.net/texture/37e838ccc26776a217c678386f6a65791fe8cdab8ce9ca4ac6b28397a4d81c22"),
        WITHER("http://textures.minecraft.net/texture/cdf74e323ed41436965f5c57ddf2815d5332fe999e68fbb9d6cf5c8bd4139f");
            
        private final ItemStack head;
        private final String url;
        private MobHead(final String url) 
        {
            this.url = url;
            head = createHead(url); 
        }
        
        @Override
        public String getUrl()
        {
            return url;
        }
        
        @Override
        public ItemStack getHead()
        {
            return head.clone(); 
        }
    }
    
    public static ItemStack createHead(final String url)
    {
        return addTexture(new ItemStack(Material.SKULL_ITEM, 1, (short)3), url);
    }
    
    public static ItemStack addTexture(final ItemStack item, final String url)
    {
        ItemMeta meta = item.getItemMeta();
        if(!SkullMeta.class.isInstance(meta))
        {
            throw new UnsupportedOperationException("Trying to add a skull texture to a non-playerhead item");
        }
        
        addTexture((SkullMeta)meta, url);
        item.setItemMeta(meta);
        return item;
    }
    
    public static void addTexture(final SkullMeta meta, final String url)
    {
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", new String(Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", new Object[] { url }).getBytes()))));
 
        try {
            Field profileField = ReflectionUtil.getField(meta.getClass(), "profile");
            profileField.set(meta, profile);
            profileField.setAccessible(false);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(HeadUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
