package com.nekonade.common.utils;

import com.nekonade.common.draw.DrawProb;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DrawUtils {


    /**
     * 权重抽奖
     * <p>
     * 全部权重喂小数且 合计 < 1 时,抽出几率接近权重
     * 其余情况则是以权重分配概率
     *
     * @param gifts 物品列表
     * @param <T>   继承了 DrawProb的类
     * @return 抽出的物品
     */
    public static <T extends DrawProb> T draw(List<T> gifts) {
        try {
            if (null == gifts || gifts.size() == 0) {
                return null;
            }
            gifts.sort(Comparator.comparingDouble(T::getProb));
            //gifts.sort((o1, o2) -> (o1.prob - o2.prob) > 0d ? 1 : -1);

            List<Double> probLists = new ArrayList<>(gifts.size());
            double sumProb = 0D;
            for (T gift : gifts) {
                sumProb += gift.getProb();
            }
            if (sumProb <= 0) {
                return null;
            }

            // 归一化概率端点
            double rate = 0D;
            for (T gift : gifts) {
                rate += gift.getProb();
                probLists.add(rate / sumProb);
            }
            //double random = Math.random();
            //SecureRandom seedRand = SecureRandom.getInstance("SHA1PRNG");
            //linux下可能出现问题，替换为以下暂代
            SecureRandom seedRand = new SecureRandom();
            double random = seedRand.nextDouble();
            probLists.add(random);
            Collections.sort(probLists);
            int index = probLists.indexOf(random);
            if (index >= 0) {
                return gifts.get(index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
