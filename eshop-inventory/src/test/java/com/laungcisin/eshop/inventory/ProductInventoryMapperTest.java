package com.laungcisin.eshop.inventory;

import com.laungcisin.eshop.inventory.mapper.ProductInventoryMapper;
import com.laungcisin.eshop.inventory.model.ProductInventory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ProductInventoryMapperTest {

    @Autowired
    private ProductInventoryMapper productInventoryMapper;

    @Test
    public void testUpdate() throws Exception {
        ProductInventory productInventory = new ProductInventory(1, 11L);
        productInventoryMapper.updateProductInventory(productInventory);
    }

    @Test
    public void testQuery() throws Exception {
        ProductInventory productInventory = productInventoryMapper.findProductInventory(1);
        if (productInventory == null) {
            System.out.println("is null");
        } else {
            System.out.println(productInventory.toString());
        }
    }

}