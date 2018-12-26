package com.laungcisin.eshop.inventory.service.impl;

import com.laungcisin.eshop.inventory.dao.RedisDAO;
import com.laungcisin.eshop.inventory.mapper.ProductInventoryMapper;
import com.laungcisin.eshop.inventory.model.ProductInventory;
import com.laungcisin.eshop.inventory.service.ProductInventoryService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * 商品库存Service实现类
 */
@Service("productInventoryService")
public class ProductInventoryServiceImpl implements ProductInventoryService {
    @Resource
    private ProductInventoryMapper productInventoryMapper;

    @Resource
    private RedisDAO redisDAO;

    @Override
    public void updateProductInventory(ProductInventory productInventory) {
        productInventoryMapper.updateProductInventory(productInventory);
    }

    @Override
    public void removeProductInventoryCache(ProductInventory productInventory) {
        String key = "product:inventory:" + productInventory.getProductId();
        redisDAO.delete(key);
    }

    @Override
    public ProductInventory findProductInventory(Integer productId) {
        return productInventoryMapper.findProductInventory(productId);
    }

    @Override
    public void setProductInventoryCache(ProductInventory productInventory) {
        String key = "product:inventory:" + productInventory.getProductId();
        redisDAO.set(key, productInventory.getInventoryCnt().toString());
    }

    @Override
    public ProductInventory getProductInventoryCache(Integer productId) {
        long inventoryCnt;

        String key = "product:inventory:" + productId;
        String result = redisDAO.get(key);

        if (!StringUtils.isEmpty(result)) {
            try {
                inventoryCnt = Long.parseLong(result);
                return new ProductInventory(productId, inventoryCnt);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

}
