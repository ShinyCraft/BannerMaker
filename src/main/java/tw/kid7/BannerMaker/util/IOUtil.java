package tw.kid7.BannerMaker.util;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import tw.kid7.BannerMaker.BannerMaker;
import tw.kid7.BannerMaker.configuration.ConfigManager;
import tw.kid7.BannerMaker.configuration.Language;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class IOUtil {

    //儲存旗幟
    static public void saveBanner(Player player, ItemStack banner) {
        //只處理旗幟
        if (!BannerUtil.isBanner(banner)) {
            player.sendMessage(MessageUtil.format(true, "&c" + Language.get("io.save-failed")));
            return;
        }
        //設定檔
        String fileName = getFileName(player);
        ConfigManager.load(fileName);
        FileConfiguration config = ConfigManager.get(fileName);
        //索引值（時間戳記，不會重複）
        String key = String.valueOf(System.currentTimeMillis());
        //旗幟資訊
        BannerMeta bm = (BannerMeta) banner.getItemMeta();
        //儲存
        config.set(key + ".color", banner.getDurability());
        List<String> patternList = new ArrayList<>();
        for (Pattern pattern : bm.getPatterns()) {
            patternList.add(pattern.getPattern().getIdentifier() + ":" + pattern.getColor().toString());
        }
        if (patternList.size() > 0) {
            config.set(key + ".patterns", patternList);
        }
        ConfigManager.save(fileName);
        //訊息
        player.sendMessage(MessageUtil.format(true, "&a" + Language.get("io.save-success")));
    }

    //讀取旗幟清單
    static public List<ItemStack> loadBannerList(Player player) {
        List<ItemStack> bannerList = new ArrayList<>();
        //設定檔
        String fileName = getFileName(player);
        ConfigManager.load(fileName);
        //強制重新讀取，以避免選單內容未即時更新
        ConfigManager.reload(fileName);
        FileConfiguration config = ConfigManager.get(fileName);
        //當前頁數
        int currentBannerPage = 1;
        if (BannerMaker.getInstance().currentBannerPage.containsKey(player.getName())) {
            currentBannerPage = BannerMaker.getInstance().currentBannerPage.get(player.getName());
        } else {
            BannerMaker.getInstance().currentBannerPage.put(player.getName(), 1);
        }
        //起始索引值
        int startIndex = Math.max(0, (currentBannerPage - 1) * 45);
        //旗幟
        Set<String> keySet = config.getKeys(false);
        List<String> keyList = new ArrayList<>();
        keyList.addAll(keySet);
        for (int i = startIndex; i < keyList.size() && i < startIndex + 45; i++) {
            String key = keyList.get(i);
            //嘗試讀取旗幟
            ItemStack banner = loadBanner(player, key);
            if (banner == null) {
                continue;
            }
            bannerList.add(banner);
        }
        return bannerList;
    }

    //讀取旗幟
    static public ItemStack loadBanner(Player player, String key) {
        //設定檔
        String fileName = getFileName(player);
        ConfigManager.load(fileName);
        FileConfiguration config = ConfigManager.get(fileName);
        //檢查是否為物品
        ItemStack banner = null;
        //檢查是否為正確格式
        if (config.isInt(key + ".color") && (!config.contains(key + ".patterns") || config.isList(key + ".patterns"))) {
            //嘗試以新格式讀取
            try {
                //建立旗幟
                banner = new ItemStack(Material.BANNER, 1, (short) config.getInt(key + ".color"));
                BannerMeta bm = (BannerMeta) banner.getItemMeta();
                //新增Patterns
                if (config.contains(key + ".patterns")) {
                    List<String> patternsList = config.getStringList(key + ".patterns");
                    for (String str : patternsList) {
                        String strPattern = str.split(":")[0];
                        String strColor = str.split(":")[1];
                        Pattern pattern = new Pattern(DyeColor.valueOf(strColor), PatternType.getByIdentifier(strPattern));
                        bm.addPattern(pattern);
                    }
                    banner.setItemMeta(bm);
                }
            } catch (Exception e) {
                banner = null;
            }
        }
        //只處理旗幟
        if (!BannerUtil.isBanner(banner)) {
            return null;
        }
        return banner;
    }

    //刪除旗幟
    static public void removeBanner(Player player, int index) {
        //設定檔
        String fileName = getFileName(player);
        FileConfiguration config = ConfigManager.get(fileName);
        Set<String> keySet = config.getKeys(false);
        List<String> keyList = new ArrayList<>();
        keyList.addAll(keySet);
        //當前頁數
        int currentBannerPage = 1;
        if (BannerMaker.getInstance().currentBannerPage.containsKey(player.getName())) {
            currentBannerPage = BannerMaker.getInstance().currentBannerPage.get(player.getName());
        } else {
            BannerMaker.getInstance().currentBannerPage.put(player.getName(), 1);
        }
        //索引值調整
        index += Math.max(0, (currentBannerPage - 1) * 45);
        //檢查索引值
        if (index >= keySet.size()) {
            return;
        }
        //設定檔路徑
        String path = keyList.get(index);
        //移除
        config.set(path, null);
        //儲存
        ConfigManager.save(fileName);
        //顯示訊息
        player.sendMessage(MessageUtil.format(true, "&a" + Language.get("io.remove-banner", index)));
    }

    //取得旗幟總數
    static public int getBannerCount(Player player) {
        //設定檔
        String fileName = getFileName(player);
        ConfigManager.load(fileName);
        FileConfiguration config = ConfigManager.get(fileName);
        Set<String> keySet = config.getKeys(false);
        List<String> keyList = new ArrayList<>();
        keyList.addAll(keySet);
        int count = 0;
        //載入並計算
        for (String key : keyList) {
            ItemStack banner = null;
            //檢查是否為正確格式
            if (config.isInt(key + ".color") && (!config.contains(key + ".patterns") || config.isList(key + ".patterns"))) {
                //嘗試以新格式讀取
                try {
                    //建立旗幟
                    banner = new ItemStack(Material.BANNER, 1, (short) config.getInt(key + ".color"));
                    BannerMeta bm = (BannerMeta) banner.getItemMeta();
                    //新增Patterns
                    if (config.contains(key + ".patterns")) {
                        List<String> patternsList = config.getStringList(key + ".patterns");
                        for (String str : patternsList) {
                            String strPattern = str.split(":")[0];
                            String strColor = str.split(":")[1];
                            Pattern pattern = new Pattern(DyeColor.valueOf(strColor), PatternType.getByIdentifier(strPattern));
                            bm.addPattern(pattern);
                        }
                        banner.setItemMeta(bm);
                    }
                } catch (Exception e) {
                    banner = null;
                }
            }
            //只計算旗幟
            if (!BannerUtil.isBanner(banner)) {
                continue;
            }
            count++;
        }
        return count;
    }

    //旗幟檔案路徑
    static public String getFileName(Player player) {
        return getFileName(player.getUniqueId().toString());
    }

    static public String getFileName(String configFileName) {
        String fileName = "banner/" + configFileName + ".yml";
        return fileName;
    }

    //更新旗幟資料
    static public void update(String configFileName) {
        //設定檔
        String fileName = getFileName(configFileName);
        ConfigManager.load(fileName);
        FileConfiguration config = ConfigManager.get(fileName);
        Set<String> keySet = config.getKeys(false);
        List<String> keyList = new ArrayList<>();
        keyList.addAll(keySet);
        int change = 0;
        for (String key : keyList) {
            ItemStack banner = config.getItemStack(key);
            if (!BannerUtil.isBanner(banner)) {
                continue;
            }
            //更新資料
            config.set(key, new ArrayList<>());
            //旗幟資訊
            BannerMeta bm = (BannerMeta) banner.getItemMeta();
            //儲存
            config.set(key + ".color", banner.getDurability());
            List<String> patternList = new ArrayList<>();
            for (Pattern pattern : bm.getPatterns()) {
                patternList.add(pattern.getPattern().getIdentifier() + ":" + pattern.getColor().toString());
            }
            if (patternList.size() > 0) {
                config.set(key + ".patterns", patternList);
            }
            //記錄
            change++;
        }
        if (change > 0) {
            //儲存
            ConfigManager.save(fileName);
            //顯示訊息
            BannerMaker.getInstance().getServer().getConsoleSender().sendMessage(MessageUtil.format(true, "&rUpdate &a" + change + " &rbanner(s) for &6" + configFileName));
        }
    }

    //更新檔案名稱
    static public void updateFileNameToUUID(Player player) {
        try {
            File oldFile = new File(BannerMaker.getInstance().getDataFolder(), getFileName(player.getName()));
            File newFile = new File(BannerMaker.getInstance().getDataFolder(), getFileName(player.getUniqueId().toString()));
            //舊檔名不存在，或新檔名存在，則無須更新
            if (!oldFile.exists() || newFile.exists()) {
                return;
            }
            String message = "";
            //嘗試重新命名
            if (oldFile.renameTo(newFile)) {
                message = "&rUpdate file name of&a " + player.getName() + "&r to UUID";
            } else {
                message = "&rUpdate file name of&a " + player.getName() + "&r FAILED";
            }
            BannerMaker.getInstance().getServer().getConsoleSender().sendMessage(MessageUtil.format(true, message));
        } catch (Exception ignored) {
        }
    }
}
