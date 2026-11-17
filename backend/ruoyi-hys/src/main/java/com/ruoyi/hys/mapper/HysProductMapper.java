package com.ruoyi.hys.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.hys.domain.HysProduct;

/**
 * 商品 / 线路数据访问。
 */
public interface HysProductMapper {

    List<HysProduct> selectProductList(HysProduct query);

    HysProduct selectProductById(Long productId);

    int insertProduct(HysProduct product);

    int updateProduct(HysProduct product);

    int deleteProductById(Long productId);

    /**
     * 扣库存 + 加销量（条件更新，库存不足返回 0）。
     */
    int increaseSales(@Param("productId") Long productId, @Param("quantity") int quantity);

    /** 取消/退款时回滚库存 */
    int rollbackSales(@Param("productId") Long productId, @Param("quantity") int quantity);

    long countOnSale(@Param("productType") String productType);
}
