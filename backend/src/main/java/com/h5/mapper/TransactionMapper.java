package com.h5.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.h5.entity.Transaction;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TransactionMapper extends BaseMapper<Transaction> {
}
