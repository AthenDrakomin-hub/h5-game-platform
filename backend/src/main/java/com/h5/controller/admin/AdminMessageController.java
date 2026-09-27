package com.h5.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h5.common.Result;
import com.h5.entity.Message;
import com.h5.mapper.MessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/admin/message")
public class AdminMessageController {

    @Autowired private MessageMapper messageMapper;

    @GetMapping("/list")
    public Result<Map<String, Object>> list(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(required = false) String category) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<Message>().orderByDesc(Message::getCreateTime);
        if (category != null && !category.isEmpty()) wrapper.eq(Message::getCategory, category);
        Page<Message> pageResult = messageMapper.selectPage(new Page<>(page, pageSize), wrapper);
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        return Result.success(data);
    }

    @PostMapping("/send")
    public Result<Void> send(@RequestBody Message message) {
        if (message.getUserId() == null) message.setUserId(0L); // 0=全站消息
        message.setIsRead(0);
        messageMapper.insert(message);
        return Result.success();
    }

    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        messageMapper.deleteById(id);
        return Result.success();
    }
}
