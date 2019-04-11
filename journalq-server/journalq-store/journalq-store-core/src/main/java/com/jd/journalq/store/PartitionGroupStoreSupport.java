package com.jd.journalq.store;

import java.io.File;

/**
 * @author liyue25
 * Date: 2018/10/31
 */
public class PartitionGroupStoreSupport {
    public static void init(File base, short[] partitions) {
        if (!base.exists()) {
            try {
                if (base.mkdirs()) {

                    // 保存partition配置
                    configPartitions(base, partitions);
                } else {
                    throw new StoreInitializeException(String.format("Create directory %s failed!", base.getAbsolutePath()));
                }
            } catch (Throwable t) {
                if (base.exists()) {
                    if (base.exists()) deleteFolder(base);
                }
                throw t;
            }
        } else {
            throw new StoreInitializeException(String.format("Directory %s exists, remove it before init!", base.getAbsolutePath()));
        }
    }

    private static void configPartitions(File base, short[] partitions) {
        File indexBase = new File(base, "index");

        if (indexBase.mkdirs()) {
            if (null != partitions) {
                for (short partition : partitions) {
                    File partitionBase = new File(indexBase, String.valueOf(partition));
                    if (!partitionBase.mkdirs()) {
                        throw new StoreInitializeException(String.format("Create directory %s failed! ", partitionBase.getAbsolutePath()));
                    }
                }
            }
        } else {
            throw new StoreInitializeException(String.format("Create directory %s failed! ", indexBase.getAbsolutePath()));
        }
    }

    private static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

}
