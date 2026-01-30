package com.ruoyi.health.service.impl;

import com.ruoyi.health.domain.ProfileConsultantNote;
import com.ruoyi.health.domain.MentalProfile;
import com.ruoyi.health.mapper.ProfileConsultantNoteMapper;
import com.ruoyi.health.mapper.MentalProfileMapper;
import com.ruoyi.health.service.IProfileConsultantNoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProfileConsultantNoteServiceImpl implements IProfileConsultantNoteService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileConsultantNoteServiceImpl.class);

    @Autowired
    private ProfileConsultantNoteMapper profileConsultantNoteMapper;

    @Autowired
    private MentalProfileMapper mentalProfileMapper;

    @Override
    public ProfileConsultantNote selectProfileConsultantNoteById(Long id) {
        return profileConsultantNoteMapper.selectProfileConsultantNoteById(id);
    }

    @Override
    public List<ProfileConsultantNote> selectProfileConsultantNoteList(ProfileConsultantNote profileConsultantNote) {
        return profileConsultantNoteMapper.selectProfileConsultantNoteList(profileConsultantNote);
    }

    @Override
    public List<ProfileConsultantNote> selectNotesByUserId(Long userId) {
        try {
            logger.info("开始查询用户ID为 {} 的备注", userId);

            // 方案1：先通过档案ID查询
            MentalProfile profile = mentalProfileMapper.selectProfileByUserId(userId);
            if (profile == null) {
                logger.warn("用户ID为 {} 的心理档案不存在", userId);
                return new ArrayList<>();
            }

            if (profile.getId() == null) {
                logger.warn("用户ID为 {} 的心理档案没有ID", userId);
                return new ArrayList<>();
            }

            logger.info("找到用户ID为 {} 的心理档案，档案ID为 {}", userId, profile.getId());

            // 根据档案ID查询备注
            ProfileConsultantNote param = new ProfileConsultantNote();
            param.setProfileId(profile.getId());
            List<ProfileConsultantNote> notes = profileConsultantNoteMapper.selectProfileConsultantNoteList(param);

            logger.info("为用户ID {} 查询到 {} 条备注", userId, notes.size());

            // 为每个备注设置userId（如果实体类需要）
            for (ProfileConsultantNote note : notes) {
                note.setUserId(userId);
            }

            return notes;

        } catch (Exception e) {
            logger.error("查询用户ID为 {} 的备注失败", userId, e);

            // 方案2：尝试直接查询（如果Mapper已经修改为JOIN查询）
            try {
                logger.info("尝试通过JOIN查询用户ID为 {} 的备注", userId);
                return profileConsultantNoteMapper.selectNotesByUserId(userId);
            } catch (Exception e2) {
                logger.error("JOIN查询也失败，返回空列表", e2);
                return new ArrayList<>();
            }
        }
    }

    @Override
    public int insertProfileConsultantNote(ProfileConsultantNote profileConsultantNote) {
        try {
            logger.info("插入咨询师备注，档案ID: {}, 咨询师ID: {}",
                    profileConsultantNote.getProfileId(),
                    profileConsultantNote.getConsultantId());

            // 如果提供了userId，尝试查找对应的profileId
            if (profileConsultantNote.getUserId() != null && profileConsultantNote.getProfileId() == null) {
                MentalProfile profile = mentalProfileMapper.selectProfileByUserId(profileConsultantNote.getUserId());
                if (profile != null && profile.getId() != null) {
                    profileConsultantNote.setProfileId(profile.getId());
                    logger.info("根据用户ID {} 找到档案ID {}",
                            profileConsultantNote.getUserId(), profileConsultantNote.getProfileId());
                }
            }

            int result = profileConsultantNoteMapper.insertProfileConsultantNote(profileConsultantNote);
            logger.info("插入咨询师备注成功，影响行数: {}", result);
            return result;

        } catch (Exception e) {
            logger.error("插入咨询师备注失败", e);
            throw new RuntimeException("插入咨询师备注失败: " + e.getMessage());
        }
    }

    @Override
    public int updateProfileConsultantNote(ProfileConsultantNote profileConsultantNote) {
        return profileConsultantNoteMapper.updateProfileConsultantNote(profileConsultantNote);
    }

    @Override
    public int deleteProfileConsultantNoteByIds(Long[] ids) {
        return profileConsultantNoteMapper.deleteProfileConsultantNoteByIds(ids);
    }

    @Override
    public int deleteProfileConsultantNoteById(Long id) {
        return profileConsultantNoteMapper.deleteProfileConsultantNoteById(id);
    }

    /**
     * 根据档案ID查询备注列表
     */
    public List<ProfileConsultantNote> selectNotesByProfileId(Long profileId) {
        try {
            ProfileConsultantNote param = new ProfileConsultantNote();
            param.setProfileId(profileId);
            return profileConsultantNoteMapper.selectProfileConsultantNoteList(param);
        } catch (Exception e) {
            logger.error("根据档案ID {} 查询备注失败", profileId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 根据咨询师ID查询备注列表
     */
    public List<ProfileConsultantNote> selectNotesByConsultantId(Long consultantId) {
        try {
            ProfileConsultantNote param = new ProfileConsultantNote();
            param.setConsultantId(consultantId);
            return profileConsultantNoteMapper.selectProfileConsultantNoteList(param);
        } catch (Exception e) {
            logger.error("根据咨询师ID {} 查询备注失败", consultantId, e);
            return new ArrayList<>();
        }
    }
}