package com.laungcisin.eshop.inventory.request;

import com.laungcisin.eshop.inventory.model.ProductInventory;
import com.laungcisin.eshop.inventory.service.ProductInventoryService;

/**
 * 更新结果：
 * cache aside pattern
 * 1.删除缓存
 * 2.更新数据库
 */
public class ProductInventoryDBUpdateRequest implements Request {
    /**
     * 商品库存
     */
    private ProductInventory productInventory;

    private ProductInventoryService productInventoryService;

    public ProductInventoryDBUpdateRequest(ProductInventory productInventory, ProductInventoryService productInventoryService) {
        this.productInventory = productInventory;
        this.productInventoryService = productInventoryService;
    }

    @Override
    public void process() {
        System.out.println("----------------------------: ProductInventoryDBUpdateRequest，进入process方法，商品id：" + productInventory.getProductId());

        // 1.删除缓存
        productInventoryService.removeProductInventoryCache(productInventory);
        System.out.println("----------------------------: ProductInventoryDBUpdateRequest，删除缓存，商品id：" + productInventory.getProductId());

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 2.更新数据库
        productInventoryService.updateProductInventory(productInventory);
        System.out.println("----------------------------: ProductInventoryDBUpdateRequest，更新数据库，商品id：" + productInventory.getProductId());
    }

    @Override
    public Integer getProductId() {
        return productInventory.getProductId();
    }

    @Override
    public boolean isForceRefresh() {
        return false;
    }
}
