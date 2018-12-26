package com.laungcisin.eshop.inventory.service;

import com.laungcisin.eshop.inventory.model.ProductInventory;

/**
 * 商品库存Service接口
 */
public interface ProductInventoryService {
    /**
     * 更新商品库存
     *
     * @param productInventory 商品库存
     */
    void updateProductInventory(ProductInventory productInventory);

    /**
     * 删除redis中的商品库存的缓存
     *
     * @param productInventory 商品库存
     */
    void removeProductInventoryCache(ProductInventory productInventory);

    /**
     * 根据商品id查询商品库存信息
     *
     * @param productId 商品id
     * @return
     */
    ProductInventory findProductInventory(Integer productId);

    /**
     * 设置商品库存缓存
     *
     * @param productInventory 商品库存
     */
    void setProductInventoryCache(ProductInventory productInventory);

    /**
     * 尝试获取商品库存的缓存
     * @param productId
     * @return
     */
    ProductInventory getProductInventoryCache(Integer productId);
}
