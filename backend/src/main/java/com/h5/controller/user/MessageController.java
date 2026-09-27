package com.h5.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.h5.common.Result;
import com.h5.common.UserContext;
import com.h5.entity.Message;
import com.h5.mapper.MessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wap/message")
public class MessageController {

    @Autowired
    private MessageMapper messageMapper;

    /**
     * 消息列表
     * GET /api/wap/message/all/list?page=1&pageSize=10
     */
    @GetMapping("/all/list")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String category) {
        Long userId = UserContext.getUserId();

        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<Message>()
                .and(w -> w.eq(Message::getUserId, userId).or().eq(Message::getUserId, 0L))
                .orderByDesc(Message::getCreateTime);
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Message::getCategory, category);
        }

        Page<Message> pageResult = messageMapper.selectPage(new Page<>(page, pageSize), wrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        data.put("page", page);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }

    /**
     * 未读消息统计
     * GET /api/wap/message/all/unread-counts
     */
    @GetMapping("/all/unread-counts")
    public Result<Map<String, Object>> unreadCounts() {
        Long userId = UserContext.getUserId();

        List<Message> unread = messageMapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .and(w -> w.eq(Message::getUserId, userId).or().eq(Message::getUserId, 0L))
                        .eq(Message::getIsRead, 0)
        );

        Map<String, Integer> categoryCounts = new HashMap<>();
        int total = 0;
        for (Message msg : unread) {
            String cat = msg.getCategory() != null ? msg.getCategory() : "system";
            categoryCounts.put(cat, categoryCounts.getOrDefault(cat, 0) + 1);
            total++;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("total", total);
        data.put("categories", categoryCounts);
        return Result.success(data);
    }

    /**
     * 标记消息已读
     * POST /api/wap/message/all/read
     */
    @PostMapping("/all/read")
    public Result<Void> read(@RequestBody Map<String, Object> params) {
        Long userId = UserContext.getUserId();
        Long id = params.get("id") != null ? Long.valueOf(params.get("id").toString()) : null;

        if (id != null) {
            Message msg = messageMapper.selectById(id);
            if (msg != null && (msg.getUserId().equals(userId) || msg.getUserId() == 0L)) {
                msg.setIsRead(1);
                messageMapper.updateById(msg);
            }
        } else {
            // 全部已读
            List<Message> unread = messageMapper.selectList(
                    new LambdaQueryWrapper<Message>()
                            .and(w -> w.eq(Message::getUserId, userId).or().eq(Message::getUserId, 0L))
                            .eq(Message::getIsRead, 0)
            );
            for (Message msg : unread) {
                msg.setIsRead(1);
                messageMapper.updateById(msg);
            }
        }
        return Result.success();
    }

    /**
     * 消息详情
     * GET /api/wap/message/detail/{id}
     */
    @GetMapping("/detail/{id}")
    public Result<Message> detail(@PathVariable Long id) {
        Message msg = messageMapper.selectById(id);
        if (msg != null && msg.getIsRead() == 0) {
            msg.setIsRead(1);
            messageMapper.updateById(msg);
        }
        return Result.success(msg);
    }
}
