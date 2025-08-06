package com.easen.aicode.controller;

import com.easen.aicode.model.entity.ChatHistory;
import com.easen.aicode.service.ChatHistoryService;
import com.mybatisflex.core.paginate.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * 对话历史 控制层。
 *
 * @author <a>easen</a>
 */
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    @Autowired
    private ChatHistoryService chatHistoryService;

    /**
     * 保存对话历史。
     *
     * @param chatHistory 对话历史
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @PostMapping("save")
    public boolean save(@RequestBody ChatHistory chatHistory) {
        return chatHistoryService.save(chatHistory);
    }

    /**
     * 根据主键删除对话历史。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    public boolean remove(@PathVariable Long id) {
        return chatHistoryService.removeById(id);
    }

    /**
     * 根据主键更新对话历史。
     *
     * @param chatHistory 对话历史
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    public boolean update(@RequestBody ChatHistory chatHistory) {
        return chatHistoryService.updateById(chatHistory);
    }

    /**
     * 查询所有对话历史。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    public List<ChatHistory> list() {
        return chatHistoryService.list();
    }

    /**
     * 根据主键获取对话历史。
     *
     * @param id 对话历史主键
     * @return 对话历史详情
     */
    @GetMapping("getInfo/{id}")
    public ChatHistory getInfo(@PathVariable Long id) {
        return chatHistoryService.getById(id);
    }

    /**
     * 分页查询对话历史。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    public Page<ChatHistory> page(Page<ChatHistory> page) {
        return chatHistoryService.page(page);
    }

}
