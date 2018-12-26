package com.laungcisin.eshop.inventory.request;

import com.laungcisin.eshop.inventory.model.ProductInventory;
import com.laungcisin.eshop.inventory.service.ProductInventoryService;

public class ProductInventoryCacheRefreshRequest implements Request {
    /**
     * 商品库存
     */
    private Integer productId;

    private ProductInventoryService productInventoryService;

    /**
     * 是否强制刷新缓存
     */
    private boolean forceRefresh;

    public ProductInventoryCacheRefreshRequest(Integer productId,
                                               ProductInventoryService productInventoryService,
                                               boolean forceRefresh) {
        this.productId = productId;
        this.productInventoryService = productInventoryService;
        this.forceRefresh = forceRefresh;
    }

    @Override
    public void process() {
        System.out.println("----------------------------: ProductInventoryCacheRefreshRequest，进入process方法，商品id：" + productId);

        // 从数据库中查询最新记录
        ProductInventory productInventory = productInventoryService.findProductInventory(productId);
        System.out.println("----------------------------: ProductInventoryCacheRefreshRequest，从数据库中查询最新记录，商品id：" + productInventory.getProductId());

        // 刷新到redis缓存中
        productInventoryService.setProductInventoryCache(productInventory);
        System.out.println("----------------------------: ProductInventoryCacheRefreshRequest，刷新到redis缓存中，商品id：" + productInventory.getProductId());
    }

    @Override
    public Integer getProductId() {
        return productId;
    }

    @Override
    public boolean isForceRefresh() {
        return forceRefresh;
    }

    public void setForceRefresh(boolean forceRefresh) {
        this.forceRefresh = forceRefresh;
    }
}
