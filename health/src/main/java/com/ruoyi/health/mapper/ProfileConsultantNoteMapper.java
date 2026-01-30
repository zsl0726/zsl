package com.ruoyi.health.mapper;


import com.ruoyi.health.domain.ProfileConsultantNote;
import java.util.List;

/**
 * 咨询师档案备注Mapper接口
 */
public interface ProfileConsultantNoteMapper {
    /**
     * 查询咨询师档案备注
     *
     * @param id 咨询师档案备注主键
     * @return 咨询师档案备注
     */
    ProfileConsultantNote selectProfileConsultantNoteById(Long id);

    /**
     * 查询咨询师档案备注列表
     *
     * @param profileConsultantNote 咨询师档案备注
     * @return 咨询师档案备注集合
     */
    List<ProfileConsultantNote> selectProfileConsultantNoteList(ProfileConsultantNote profileConsultantNote);

    /**
     * 根据用户ID查询备注列表
     *
     * @param userId 用户ID
     * @return 咨询师档案备注集合
     */
    List<ProfileConsultantNote> selectNotesByUserId(Long userId);

    /**
     * 新增咨询师档案备注
     *
     * @param profileConsultantNote 咨询师档案备注
     * @return 结果
     */
    int insertProfileConsultantNote(ProfileConsultantNote profileConsultantNote);

    /**
     * 修改咨询师档案备注
     *
     * @param profileConsultantNote 咨询师档案备注
     * @return 结果
     */
    int updateProfileConsultantNote(ProfileConsultantNote profileConsultantNote);

    /**
     * 删除咨询师档案备注
     *
     * @param id 咨询师档案备注主键
     * @return 结果
     */
    int deleteProfileConsultantNoteById(Long id);

    /**
     * 批量删除咨询师档案备注
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteProfileConsultantNoteByIds(Long[] ids);
}
