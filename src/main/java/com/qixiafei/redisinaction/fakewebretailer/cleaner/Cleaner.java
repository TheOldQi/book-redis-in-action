package com.qixiafei.redisinaction.fakewebretailer.cleaner;

import com.qixiafei.redisinaction.RedisKeyConstants;
import com.qixiafei.redisinaction.jedis.RedisClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <P>Description: 随程序启动启动，若登录用户信息超过LIMIT配置，以指定每次最多BATCH_SIZE清除最久不访问的用户. </P>
 * <P>CALLED BY:   齐霞飞 </P>
 * <P>UPDATE BY:    </P>
 * <P>CREATE DATE: 2019/5/6 10:27</P>
 * <P>UPDATE DATE: </P>
 *
 * @author qixiafei
 * @version 1.0
 * @since java 1.8.0
 */
@Slf4j
@Component
public class Cleaner implements ApplicationContextAware {

    private static final long LIMIT = 10000000; // 1000万
    private static final long BATCH_SIZE = 100; //一次最多100个


    @Resource
    private RedisClient redisClient;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        final Thread t = new Thread(new CleanerT(redisClient, applicationContext.getBeansOfType(CustomCleaner.class).entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toSet())));
        t.setDaemon(true);
        t.setName("clean session t");
        t.start();
    }

    private static class CleanerT implements Runnable {

        private RedisClient redisClient;
        private Set<CustomCleaner> cleanerList;

        private CleanerT(final RedisClient redisClient, final Set<CustomCleaner> cleanerList) {
            this.redisClient = redisClient;
            if (cleanerList == null || cleanerList.isEmpty()) {
                log.warn("清理器没有清理执行器，将不产生实际作用");
                this.cleanerList = Collections.emptySet();
            } else {
                this.cleanerList = cleanerList;
                for (CustomCleaner customCleaner : cleanerList) {
                    customCleaner.regLog();
                }
            }
        }


        @Override
        public void run() {
            while (true) {
                final long size = redisClient.zcard(RedisKeyConstants.RECENT_ZSET_KEY);
                if (size > LIMIT) {
                    // 从zset头部移除token，
                    final long end_index = Math.min(BATCH_SIZE, size - LIMIT);
                    final Set<String> tokens = redisClient.zrange(RedisKeyConstants.RECENT_ZSET_KEY, 0, end_index - 1);
                    // 一次完成多个key删除，效率更好
                    final int tokenSize = tokens.size();
                    final String[] tokensArr = new String[tokenSize];
                    tokens.toArray(tokensArr);
                    for (CustomCleaner customCleaner : cleanerList) {
                        customCleaner.clean(tokensArr);
                    }

                    continue;
                }

                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    log.error("clean session thread has bean interrupted!", e);
                }
            }
        }
    }

    private static final int CAPACITY = (1 << 27) - 1;

    public static void main(String[] args) {
        System.out.println("{\"balanceMoney\":156.00,\"batchId\":1,\"batchName\":\"晨鲜送\",\"batchSeq\":0,\"batchStartTime\":1557104400,\"bulkFlag\":0,\"cT\":1557125889,\"channel\":2,\"cityId\":6,\"cityName\":\"重庆市\",\"companyId\":16017715,\"companyName\":\"小姐姐家的烤肉\",\"companyPhone\":\"13628368836\",\"couponDiscount\":0.00,\"customerType\":1,\"customerTypeEnum\":\"NORMAL_CUSTOMER\",\"deliveryAreaId\":43879,\"deliveryAreaName\":\"沙坪坝C05\",\"deliveryDate\":1557158400,\"deliveryType\":10,\"distributeSeq\":0,\"earnestMoney\":0.00,\"expectReceiveTime\":\"7:00-11:00\",\"ext\":\"{\\\"high_quality\\\":1,\\\"deliveryDateType\\\":1,\\\"settlementCompanyType\\\":\\\"0\\\",\\\"backupProdBatchId\\\":650,\\\"chargeSmallMoney\\\":1,\\\"corpOperatorId\\\":\\\"7829230\\\",\\\"backupProdBatchName\\\":\\\"晨鲜送1波次\\\",\\\"isLatePromise\\\":0,\\\"vip_level\\\":0,\\\"salesMan\\\":[{\\\"phone\\\":\\\"18623072280\\\",\\\"name\\\":\\\"蒋勇\\\",\\\"typeName\\\":\\\"非品类\\\",\\\"id\\\":154733,\\\"type\\\":0}],\\\"shipmentType\\\":\\\"1\\\"}\",\"freightFee\":0.00,\"goodsAmount\":262.00,\"id\":80868264,\"latitude\":\"29.61028012580713\",\"longitude\":\"106.30005374582822\",\"mergeOmcOrderId\":0,\"mergeStatus\":1,\"omcBatchConfigId\":1,\"omcOrderId\":74086738700,\"orderItemPacks\":[],\"orderItems\":[{\"biName\":\"[禾凤鸣]冻鸡皮 箱(10公斤)\",\"class1Id\":2,\"class1Name\":\"肉类\",\"class2Id\":130,\"class2Name\":\"鸡副产品\",\"class3Id\":822,\"class3Name\":\"鸡皮\",\"couponDiscount\":0.00,\"distributeNum\":0,\"distributeWeight\":0.000,\"expectNum\":1,\"expectWeight\":1.000,\"ext\":\"{\\\"isReplenished\\\":0,\\\"priceType\\\":\\\"1\\\",\\\"saleMode\\\":2,\\\"isProcessing\\\":0,\\\"highQuality\\\":1,\\\"isWeight\\\":0}\",\"extJson\":{\"isReplenished\":0,\"priceType\":\"1\",\"saleMode\":2,\"isProcessing\":0,\"highQuality\":1,\"isWeight\":0},\"giftType\":0,\"lastDeliveryTime\":0,\"oldOmcOrderId\":74086738700,\"omcOrderId\":74086738700,\"omcOrderItemId\":106397240300,\"orderId\":740867387,\"orderItemId\":1063972403,\"orderItemStatus\":3,\"ownerId\":1,\"ownerName\":\"\",\"price\":82.00,\"processFlag\":0,\"prodwarehouseId\":7,\"promotionDiscount\":0.00,\"returnNum\":0,\"returnWeight\":0.000,\"sellType\":1,\"skuBrand\":\"禾凤鸣\",\"skuFormat\":\"箱(10公斤)\",\"skuId\":1571874,\"skuLevel\":\"冷冻 清真\",\"skuName\":\"禾凤鸣 冻鸡皮 箱(10公斤)\",\"skuPackUnitNum\":0,\"skuPriceUnit\":\"箱\",\"skuUnit\":\"箱\",\"sortNum\":0,\"sortWeight\":0.000,\"sortingcenterId\":7,\"spuId\":1338972,\"spuPackUnitNum\":0,\"ssuFormat\":\"箱(10公斤)\",\"ssuId\":1911855,\"ssuName\":\"[禾凤鸣]冻鸡皮 箱(10公斤)\",\"taxRate\":0,\"totalDiscount\":0.00,\"unitPrice\":82.00,\"unitPriceNum\":1000,\"unitSkuNum\":1,\"unitSpuNum\":1},{\"biName\":\"[华阳]牛仔骨A 冷冻 袋(200g)\",\"class1Id\":2,\"class1Name\":\"肉类\",\"class2Id\":123,\"class2Name\":\"牛肉分割类\",\"class3Id\":880,\"class3Name\":\"牛仔骨\",\"couponDiscount\":0.00,\"distributeNum\":0,\"distributeWeight\":0.000,\"expectNum\":10,\"expectWeight\":10.000,\"ext\":\"{\\\"isReplenished\\\":0,\\\"priceType\\\":\\\"1\\\",\\\"saleMode\\\":3,\\\"isProcessing\\\":0,\\\"highQuality\\\":1,\\\"isWeight\\\":0}\",\"extJson\":{\"isReplenished\":0,\"priceType\":\"1\",\"saleMode\":3,\"isProcessing\":0,\"highQuality\":1,\"isWeight\":0},\"giftType\":0,\"lastDeliveryTime\":0,\"oldOmcOrderId\":74086738700,\"omcOrderId\":74086738700,\"omcOrderItemId\":106397240400,\"orderId\":740867387,\"orderItemId\":1063972404,\"orderItemStatus\":3,\"ownerId\":1,\"ownerName\":\"\",\"price\":18.00,\"processFlag\":0,\"prodwarehouseId\":7,\"promotionDiscount\":0.00,\"returnNum\":0,\"returnWeight\":0.000,\"sellType\":1,\"skuBrand\":\"华阳\",\"skuFormat\":\"袋(200g)\",\"skuId\":1082530,\"skuLevel\":\"山东省滨州市  冷冻 山东省滨州市 是\",\"skuName\":\"华阳 牛仔骨A 冷冻 袋(200g)\",\"skuPackUnitNum\":0,\"skuPriceUnit\":\"袋\",\"skuUnit\":\"袋\",\"sortNum\":0,\"sortWeight\":0.000,\"sortingcenterId\":7,\"spuId\":849640,\"spuPackUnitNum\":0,\"ssuFormat\":\"袋(200g)\",\"ssuId\":1434049,\"ssuName\":\"[华阳]牛仔骨A 冷冻 袋(200g)\",\"taxRate\":0,\"totalDiscount\":0.00,\"unitPrice\":18.00,\"unitPriceNum\":1000,\"unitSkuNum\":1,\"unitSpuNum\":1}],\"orderMark\":\"22100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000003111111\",\"orderPayChannel\":[{\"oldOmcOrderId\":74086738700,\"omcOrderId\":74086738700,\"orderId\":740867387,\"payAmount\":156.00,\"payChannelId\":10,\"paySerialNumber\":\"4615000619050614573116017715EV\",\"payTime\":1557125851},{\"oldOmcOrderId\":74086738700,\"omcOrderId\":74086738700,\"orderId\":740867387,\"payAmount\":106.00,\"payChannelId\":60,\"paySerialNumber\":\"4615000619050614573116017715EV\",\"payTime\":1557125851}],\"orderPerson\":\"16669553\",\"orderRelations\":[{\"balanceMoney\":156.00,\"couponDiscount\":0.00,\"deliveryPriority\":1,\"earnestMoney\":0.00,\"ext\":\"{\\\"high_quality\\\":1,\\\"deliveryDateType\\\":1,\\\"settlementCompanyType\\\":\\\"0\\\",\\\"backupProdBatchId\\\":650,\\\"chargeSmallMoney\\\":1,\\\"corpOperatorId\\\":\\\"7829230\\\",\\\"backupProdBatchName\\\":\\\"晨鲜送1波次\\\",\\\"isLatePromise\\\":0,\\\"vip_level\\\":0,\\\"salesMan\\\":[{\\\"phone\\\":\\\"18623072280\\\",\\\"name\\\":\\\"蒋勇\\\",\\\"typeName\\\":\\\"非品类\\\",\\\"id\\\":154733,\\\"type\\\":0}],\\\"shipmentType\\\":\\\"1\\\"}\",\"freightFee\":0.00,\"oldOmcOrderId\":74086738700,\"omcOrderId\":74086738700,\"orderCreateTime\":1557125831,\"orderId\":740867387,\"orderType\":1,\"orderWay\":2,\"paidMoney\":106.00,\"parentId\":0,\"payableAmount\":0.00,\"paymentType\":2,\"source\":0,\"splitType\":0,\"totalPrice\":262.00}],\"orderStatus\":4,\"packageAmount\":0.00,\"paidMoney\":106.00,\"payableAmount\":0.00,\"paymentType\":2,\"performBatch\":0,\"prodBatchId\":710,\"prodBatchName\":\"夜间配1波次\",\"promotionDiscount\":0.00,\"receiverAddress\":\"大学城西路龙湖U城B馆8-7栋3-B07\",\"receiverId\":0,\"receiverName\":\"沈俊杰\",\"receiverPhone\":\"13628368836\",\"routeId\":0,\"routeName\":\"\",\"saleAreaId\":1211,\"sallerId\":154733,\"sallerName\":\"蒋勇\",\"sallerPhone\":\"18623072280\",\"secondSortingCenterId\":0,\"siteId\":0,\"siteName\":\"\",\"siteType\":0,\"sortingcenterId\":7,\"source\":0,\"splitType\":0,\"stationRegionId\":7647,\"stationRegionName\":\"沙坪坝C\",\"totalDiscount\":0.00,\"totalPrice\":262.00,\"uT\":1557125917,\"version\":0,\"warehouseId\":7,\"warehouseName\":\"重庆仓库1\"}");
        System.out.println(CAPACITY);
        System.out.println(~CAPACITY);
    }
}
