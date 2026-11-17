package com.ruoyi.hys.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.hys.domain.HysWalletTxn;

/**
 * 钱包流水数据访问。{@code bizKey} 有唯一索引，重复写入会抛 DuplicateKeyException，
 * 服务层据此判定幂等命中。
 */
public interface HysWalletTxnMapper {

    int insertTxn(HysWalletTxn txn);

    List<HysWalletTxn> selectTxnList(HysWalletTxn query);

    HysWalletTxn selectByBizKey(String bizKey);

    /** 按流水类型汇总某会员的金额（amount 已带符号，收入类传正、支出类传负都以绝对值过滤） */
    long sumByTypes(@Param("memberId") Long memberId, @Param("types") List<String> types,
            @Param("positive") boolean positive);

    int countTransferToday(@Param("memberId") Long memberId, @Param("start") java.time.LocalDateTime start);
}
