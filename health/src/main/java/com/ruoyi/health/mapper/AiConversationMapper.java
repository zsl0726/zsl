package com.ruoyi.health.mapper;

import com.ruoyi.health.domain.AiConversation;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

public interface AiConversationMapper {

    /**
     * 根据ID查询对话记录
     */
    AiConversation selectAiConversationById(Long id);

    /**
     * 查询对话记录列表
     */
    List<AiConversation> selectAiConversationList(AiConversation aiConversation);

    /**
     * 根据会话ID查询对话记录
     */
    List<AiConversation> selectBySessionId(@Param("sessionId") String sessionId);

    /**
     * 根据用户ID查询对话记录
     */
    List<AiConversation> selectByUserId(Long userId);

    /**
     * 获取用户的会话ID列表
     */
    List<String> selectUserSessionIds(@Param("userId") Long userId);

    /**
     * 根据会话ID统计消息数量
     */
    int countBySessionId(@Param("sessionId") String sessionId);

    /**
     * 删除会话（逻辑删除）
     */
    int deleteBySessionId(@Param("sessionId") String sessionId, @Param("userId") Long userId);

    /**
     * 更新会话删除状态
     */
    int updateIsDeletedBySessionId(@Param("sessionId") String sessionId,
                                   @Param("userId") Long userId,
                                   @Param("isDeleted") String isDeleted);

    /**
     * 新增对话记录
     */
    int insertAiConversation(AiConversation aiConversation);

    /**
     * 修改对话记录
     */
    int updateAiConversation(AiConversation aiConversation);

    /**
     * 删除对话记录
     */
    int deleteAiConversationById(Long id);

    /**
     * 批量删除对话记录
     */
    int deleteAiConversationByIds(Long[] ids);
}