package com.laungcisin.eshop.inventory.controller;

import com.laungcisin.eshop.inventory.model.ProductInventory;
import com.laungcisin.eshop.inventory.request.ProductInventoryCacheRefreshRequest;
import com.laungcisin.eshop.inventory.request.ProductInventoryDBUpdateRequest;
import com.laungcisin.eshop.inventory.request.Request;
import com.laungcisin.eshop.inventory.service.ProductInventoryService;
import com.laungcisin.eshop.inventory.service.RequestAsyncProcessService;
import com.laungcisin.eshop.inventory.vo.Response;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * 商品库存Controller
 */
@Controller
public class ProductInventoryController {

    @Resource
    private RequestAsyncProcessService requestAsyncProcessService;

    @Resource
    private ProductInventoryService productInventoryService;

    /**
     * 更新商品库存
     *
     * @param productInventory
     * @return
     */
    @RequestMapping("/updateProductInventory")
    @ResponseBody
    public Response updateProductInventory(ProductInventory productInventory) {
        Response response;
        try {
            System.out.println("----------------------------: updateProductInventory更新商品库存， 商品id：" + productInventory.getProductId()
                    + ", 商品库存：" + productInventory.getInventoryCnt());
            Request request = new ProductInventoryDBUpdateRequest(productInventory, productInventoryService);
            requestAsyncProcessService.process(request);
            response = new Response(Response.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            response = new Response(Response.FAILURE);
        }

        return response;
    }

    /**
     * 获取商品库存
     *
     * @param productId
     * @return
     */
    @RequestMapping("/getProductInventory")
    @ResponseBody
    public ProductInventory getProductInventory(Integer productId) {
        ProductInventory productInventory;
        try {
            Request cacheRefreshRequest = new ProductInventoryCacheRefreshRequest(productId, productInventoryService, false);
            requestAsyncProcessService.process(cacheRefreshRequest);

            long startTime = System.currentTimeMillis();
            long endTime = 0L;
            long waitTime = 0L;

            // 1.尝试在200ms内从缓存中读取数据
            while (waitTime < 200) {

                Thread.sleep(10000);

                // 尝试去redis中读取一次商品库存的缓存数据
                productInventory = productInventoryService.getProductInventoryCache(productId);

                // 如果读取到了结果，那么就返回
                if (productInventory != null) {
                    return productInventory;
                } else {// 等待一段时间
                    Thread.sleep(20);
                    endTime = System.currentTimeMillis();
                    waitTime = endTime - startTime;
                }
            }

            // 2.尝试从数据库中读取数据
            productInventory = productInventoryService.findProductInventory(productId);
            if (productInventory != null) {
                // 运行到这里的情况：
                // 1.上一次读请求，数据刷入redis，但是redis LRU算法清理了内容，标志位还是false
                // 此时下一个请求是从redis中读不到数据的，再放一个请求到队列中，刷新下数据
                // 2.可能在200ms内，读请求，在队列中一直积压着
                // 3.数据库中没有此数据---缓存穿透
                cacheRefreshRequest = new ProductInventoryCacheRefreshRequest(productId, productInventoryService, true);
                requestAsyncProcessService.process(cacheRefreshRequest);
                return productInventory;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ProductInventory(productId, -1L);
    }
}
