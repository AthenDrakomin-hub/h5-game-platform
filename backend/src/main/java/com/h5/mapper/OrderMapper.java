package com.h5.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.h5.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
